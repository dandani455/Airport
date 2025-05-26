package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.controllers.validators.LocationValidator;
import airport.core.models.Location;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.LocationAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationController extends BaseController {

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

    public Response<Void> createLocation(String id, String name, String city, String country, String latitudeStr, String longitudeStr) {
        try {
            Response<Location> valid = LocationValidator.validate(id, name, city, country, latitudeStr, longitudeStr);
            if (valid.getStatus() != Status.OK) {
                return new Response<>(valid.getStatus(), valid.getMessage());
            }

            Location location = valid.getObject();

            boolean exists = repo.getAll().stream()
                    .anyMatch(loc -> loc.getAirportId().equals(location.getAirportId()));
            if (exists) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe una localización con ese ID");
            }

            repo.add(location);
            notifyObservers();

            return new Response<>(Status.CREATED, "Localización creada correctamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al crear localización");
        }
    }
}
