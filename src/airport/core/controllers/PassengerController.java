package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Passenger;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PassengerAdapter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PassengerController {

    private final JsonRepository<Passenger> repo;

    public PassengerController() {
        this.repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter());
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
}
