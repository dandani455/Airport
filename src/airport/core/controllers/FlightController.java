package airport.core.controllers;

import airport.Flight;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Location;
import airport.core.models.Plane;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.FlightAdapter;
import airport.core.services.LookupService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FlightController {

    private final JsonRepository<Flight> repo;

    public FlightController(List<Plane> planes, List<Location> locations) {
        LookupService lookup = new LookupService(planes, locations);
        this.repo = new JsonRepository<>("json/flights.json", new FlightAdapter(lookup));
    }

    public Response<List<Flight>> getAllFlights() {
        try {
            List<Flight> flights = repo.getAll();
            flights.sort(Comparator.comparing(Flight::getId));
            return new Response<>(Status.OK, "Vuelos obtenidos exitosamente", flights);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo vuelos", Collections.emptyList());
        }
    }
}
