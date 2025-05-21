package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Passenger;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PassengerAdapter;
import java.util.Comparator;
import java.util.List;

public class PassengerController {
    private final JsonRepository<Passenger> repository;

    public PassengerController() {
        this.repository = new JsonRepository<>("json/passengers.json", new PassengerAdapter());
    }

    public Response create(Passenger p) {
        if (repository.find(existing -> existing.getId() == p.getId()).isPresent()) {
            return new Response(Status.BAD_REQUEST, "ID ya existe");
        }
        repository.add(p);
        return new Response(Status.CREATED, "Pasajero creado");
    }

    public Response update(Passenger updated) {
        repository.update(list -> {
            for (Passenger p : list) {
                if (p.getId() == updated.getId()) {
                    p.setFirstname(updated.getFirstname());
                    p.setLastname(updated.getLastname());
                    p.setBirthDate(updated.getBirthDate());
                    p.setCountryPhoneCode(updated.getCountryPhoneCode());
                    p.setPhone(updated.getPhone());
                    p.setCountry(updated.getCountry());
                    return true;
                }
            }
            return false;
        });
        return new Response(Status.OK, "Pasajero actualizado");
    }

    public Response delete(long id) {
        boolean removed = repository.remove(p -> p.getId() == id);
        return removed
            ? new Response(Status.OK, "Pasajero eliminado")
            : new Response(Status.NOT_FOUND, "No se encontró el pasajero");
    }

    public List<Passenger> getAllSorted() {
        List<Passenger> sorted = repository.getAll();
        sorted.sort(Comparator.comparingLong(Passenger::getId));
        return sorted;
    }

    public Passenger findById(long id) {
        return repository.find(p -> p.getId() == id).orElse(null);
    }
}