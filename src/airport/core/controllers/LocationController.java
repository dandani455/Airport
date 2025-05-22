package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Location;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.LocationAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationController {

    private final JsonRepository<Location> repo;

    public LocationController() {
        this.repo = new JsonRepository<>("json/locations.json", new LocationAdapter());
    }

    public Response<List<Location>> getAllLocations() {
        try {
            List<Location> list = repo.getAll();
            list.sort(Comparator.comparing(Location::getAirportId));
            return new Response<>(Status.OK, "Localizaciones obtenidas", list);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo localizaciones", Collections.emptyList());
        }
    }
}
