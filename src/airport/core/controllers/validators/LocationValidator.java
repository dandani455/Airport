package airport.core.controllers.validators;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Location;

import java.math.BigDecimal;

public class LocationValidator {

    public static Response<Location> validate(String id, String name, String city, String country,
            String latitudeStr, String longitudeStr) {
        try {
            if (id == null || !id.matches("[A-Z]{3}")) {
                return new Response<>(Status.BAD_REQUEST, "ID debe tener exactamente 3 letras mayúsculas");
            }

            if (name == null || name.trim().isEmpty()
                    || city == null || city.trim().isEmpty()
                    || country == null || country.trim().isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Nombre, ciudad y país no pueden estar vacíos");
            }

            double latitude = Double.parseDouble(latitudeStr);
            if (latitude < -90 || latitude > 90 || new BigDecimal(latitudeStr).scale() > 4) {
                return new Response<>(Status.BAD_REQUEST, "Latitud inválida (rango -90 a 90 y máximo 4 decimales)");
            }

            double longitude = Double.parseDouble(longitudeStr);
            if (longitude < -180 || longitude > 180 || new BigDecimal(longitudeStr).scale() > 4) {
                return new Response<>(Status.BAD_REQUEST, "Longitud inválida (rango -180 a 180 y máximo 4 decimales)");
            }

            Location location = new Location(
                    id,
                    name.trim(),
                    city.trim(),
                    country.trim(),
                    latitude,
                    longitude
            );

            return new Response<>(Status.OK, "Localización válida", location);

        } catch (NumberFormatException e) {
            return new Response<>(Status.BAD_REQUEST, "Latitud o longitud no son numéricas válidas");
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error validando localización: " + e.getMessage());
        }
    }
}
