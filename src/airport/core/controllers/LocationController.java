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

    public Response<Void> createLocation(String id, String name, String city, String country, String latitudeStr, String longitudeStr) {
        try {
            // Validar ID
            if (id == null || !id.matches("[A-Z]{3}")) {
                return new Response<>(Status.BAD_REQUEST, "ID debe tener exactamente 3 letras mayúsculas");
            }

            // Verificar unicidad del ID
            List<Location> all = repo.getAll();
            if (all.stream().anyMatch(loc -> loc.getAirportId().equals(id))) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe una localización con ese ID");
            }

            // Validar campos no vacíos
            if (name == null || name.trim().isEmpty()
                    || city == null || city.trim().isEmpty()
                    || country == null || country.trim().isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Nombre, ciudad y país no pueden estar vacíos");
            }

            // Validar latitud
            double latitude;
            try {
                latitude = Double.parseDouble(latitudeStr);
                if (latitude < -90 || latitude > 90) {
                    return new Response<>(Status.BAD_REQUEST, "Latitud debe estar entre -90 y 90");
                }
                if (String.valueOf(latitude).split("\\.")[1].length() > 4) {
                    return new Response<>(Status.BAD_REQUEST, "Latitud debe tener como máximo 4 decimales");
                }
            } catch (Exception e) {
                return new Response<>(Status.BAD_REQUEST, "Latitud inválida");
            }

            // Validar longitud
            double longitude;
            try {
                longitude = Double.parseDouble(longitudeStr);
                if (longitude < -180 || longitude > 180) {
                    return new Response<>(Status.BAD_REQUEST, "Longitud debe estar entre -180 y 180");
                }
                if (String.valueOf(longitude).split("\\.")[1].length() > 4) {
                    return new Response<>(Status.BAD_REQUEST, "Longitud debe tener como máximo 4 decimales");
                }
            } catch (Exception e) {
                return new Response<>(Status.BAD_REQUEST, "Longitud inválida");
            }

            // Crear localización
            Location location = new Location(id, name.trim(), city.trim(), country.trim(), latitude, longitude);
            repo.add(location);

            return new Response<>(Status.CREATED, "Localización creada correctamente");
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al crear localización");
        }
    }
}
