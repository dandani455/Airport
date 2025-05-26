package airport.core.controllers;

import airport.Flight;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Passenger;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PassengerAdapter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PassengerController extends BaseController {

    private final JsonRepository<Passenger> repo;

    public PassengerController(List<Flight> flights) {
        this.repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter(flights));
    }

    public Response<List<Passenger>> getAllPassengers() {
        try {
            List<Passenger> list = repo.getAll();
            list.sort(Comparator.comparingLong(Passenger::getId));
            return new Response<>(Status.OK, "Pasajeros obtenidos", list);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo pasajeros", Collections.emptyList());
        }
    }

    public Response<Passenger> getPassengerById(long id) {
        return repo.find(p -> p.getId() == id)
                .map(p -> new Response<>(Status.OK, "Pasajero encontrado", p))
                .orElse(new Response<>(Status.NOT_FOUND, "Pasajero no encontrado"));
    }

    public Response<Void> createPassenger(long id, String firstname, String lastname, String birthYear, String birthMonth, String birthDay,
            String countryCode, String phone, String country) {
        if (repo.find(p -> p.getId() == id).isPresent()) {
            return new Response<>(Status.BAD_REQUEST, "Ya existe un pasajero con ese ID");
        }

        Response<Passenger> valid = validatePassengerFields(id, firstname, lastname, birthYear, birthMonth, birthDay, countryCode, phone, country);
        if (valid.getStatus() != Status.OK) {
            return new Response<>(valid.getStatus(), valid.getMessage());
        }

        repo.add(valid.getObject());
        notifyObservers();
        return new Response<>(Status.CREATED, "Pasajero creado exitosamente");
    }

    public Response<Void> updatePassenger(long id, String firstname, String lastname, String birthYear, String birthMonth, String birthDay, String countryCode, String phone, String country) {
        Optional<Passenger> optional = repo.find(p -> p.getId() == id);
        if (!optional.isPresent()) {
            return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
        }

        Response<Passenger> valid = validatePassengerFields(id, firstname, lastname, birthYear, birthMonth, birthDay, countryCode, phone, country);
        if (valid.getStatus() != Status.OK) {
            return new Response<>(valid.getStatus(), valid.getMessage());
        }

        Passenger existing = optional.get();
        Passenger updated = valid.getObject();

        boolean changed = false;

        if (!existing.getFirstname().equals(updated.getFirstname())) {
            existing.setFirstname(updated.getFirstname());
            changed = true;
        }
        if (!existing.getLastname().equals(updated.getLastname())) {
            existing.setLastname(updated.getLastname());
            changed = true;
        }
        if (!existing.getBirthDate().equals(updated.getBirthDate())) {
            existing.setBirthDate(updated.getBirthDate());
            changed = true;
        }
        if (!existing.getCountry().equals(updated.getCountry())) {
            existing.setCountry(updated.getCountry());
            changed = true;
        }
        if (existing.getCountryPhoneCode() != updated.getCountryPhoneCode()) {
            existing.setCountryPhoneCode(updated.getCountryPhoneCode());
            changed = true;
        }
        if (existing.getPhone() != updated.getPhone()) {
            existing.setPhone(updated.getPhone());
            changed = true;
        }

        if (!changed) {
            return new Response<>(Status.NO_CONTENT, "No hubo cambios en el pasajero");
        }

        repo.update(list -> {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == id) {
                    list.set(i, existing);
                    return true;
                }
            }
            return false;
        });

        notifyObservers();
        return new Response<>(Status.OK, "Pasajero actualizado exitosamente");
    }

    private Response<Passenger> validatePassengerFields(long id, String firstname, String lastname, String birthYear, String birthMonth,
            String birthDay, String countryCode, String phone, String country) {
        try {
            if (firstname.isEmpty() || lastname.isEmpty() || birthYear.isEmpty() || birthMonth.isEmpty() || birthDay.isEmpty()
                    || countryCode.isEmpty() || phone.isEmpty() || country.isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Ningún campo puede estar vacío");
            }

            if (id < 0 || String.valueOf(id).length() > 15) {
                return new Response<>(Status.BAD_REQUEST, "ID inválido: debe ser ≥ 0 y ≤ 15 dígitos");
            }

            int year = Integer.parseInt(birthYear);
            int month = Integer.parseInt(birthMonth);
            int day = Integer.parseInt(birthDay);

            if (year < 1900 || year > LocalDate.now().getYear()) {
                return new Response<>(Status.BAD_REQUEST, "Año inválido (≥ 1900)");
            }

            LocalDate birth = LocalDate.of(year, month, day);
            int code = Integer.parseInt(countryCode);
            if (code < 0 || String.valueOf(code).length() > 3) {
                return new Response<>(Status.BAD_REQUEST, "Código de país inválido (máx. 3 dígitos)");
            }

            long phoneNumber = Long.parseLong(phone);
            if (phoneNumber < 0 || String.valueOf(phoneNumber).length() > 11) {
                return new Response<>(Status.BAD_REQUEST, "Teléfono inválido (máx. 11 dígitos)");
            }

            Passenger passenger = new Passenger(id, firstname.trim(), lastname.trim(), birth, code, phoneNumber, country.trim());
            return new Response<>(Status.OK, "Datos validados", passenger);

        } catch (Exception e) {
            return new Response<>(Status.BAD_REQUEST, "Datos inválidos: " + e.getMessage());
        }
    }

    public void forceUpdatePassenger(Passenger updated) {
        repo.update(list -> {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == updated.getId()) {
                    list.set(i, updated);
                    notifyObservers();
                    return true;
                }
            }
            return false;
        });
    }

    public Response<List<String>> getPassengerNameLabels() {
        try {
            List<Passenger> list = repo.getAll();
            list.sort(Comparator.comparingLong(Passenger::getId));

            List<String> names = list.stream()
                    .map(p -> p.getFirstname() + " " + p.getLastname() + " (ID: " + p.getId() + ")")
                    .collect(Collectors.toList());

            return new Response<>(Status.OK, "Etiquetas generadas", names);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo nombres");
        }
    }

    public Response<Passenger> getPassengerByFullName(String name) {
        return repo.find(p -> p.getFullname().equals(name))
                .map(p -> new Response<>(Status.OK, "Pasajero encontrado", p))
                .orElse(new Response<>(Status.NOT_FOUND, "Pasajero no encontrado"));
    }
}
