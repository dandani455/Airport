package airport.core.controllers.validators;

import airport.Flight;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Location;
import airport.core.models.Plane;
import airport.core.models.Passenger;
import airport.core.services.LookupService;

import java.time.LocalDateTime;

public class FlightValidator {

    public static Response<Flight> validate(String id, String planeId, String departureId, String arrivalId,
            String scaleId, LocalDateTime departureDate,
            int durationHours, int durationMinutes,
            int scaleHours, int scaleMinutes,
            LookupService lookup) {
        if (id == null || id.trim().isEmpty() || planeId == null || departureId == null || arrivalId == null || departureDate == null) {
            return new Response<>(Status.BAD_REQUEST, "Todos los campos obligatorios deben estar llenos");
        }

        id = id.trim();

        if (!id.matches("[A-Z]{3}\\d{3}")) {
            return new Response<>(Status.BAD_REQUEST, "El ID del vuelo debe tener el formato XXXYYY");
        }

        Plane plane = lookup.getPlane(planeId);
        Location departure = lookup.getLocation(departureId);
        Location arrival = lookup.getLocation(arrivalId);
        Location scale = (scaleId == null || scaleId.equals("Ninguna")) ? null : lookup.getLocation(scaleId);

        if (plane == null || departure == null || arrival == null) {
            return new Response<>(Status.BAD_REQUEST, "Datos inválidos: avión o localizaciones no encontrados");
        }

        if (scale != null && (scale.getAirportId().equals(departure.getAirportId()) || scale.getAirportId().equals(arrival.getAirportId()))) {
            return new Response<>(Status.BAD_REQUEST, "La escala no puede ser igual a la salida o llegada");
        }

        if (durationHours < 0 || durationMinutes < 0 || (durationHours == 0 && durationMinutes == 0)) {
            return new Response<>(Status.BAD_REQUEST, "La duración del vuelo debe ser mayor a 00:00");
        }

        if (scale == null && (scaleHours != 0 || scaleMinutes != 0)) {
            return new Response<>(Status.BAD_REQUEST, "Si no hay escala, su duración debe ser 00:00");
        }

        Flight flight;
        if (scale == null) {
            flight = new Flight(id, plane, departure, arrival, departureDate, durationHours, durationMinutes);
        } else {
            flight = new Flight(id, plane, departure, scale, arrival, departureDate,
                    durationHours, durationMinutes, scaleHours, scaleMinutes);
        }

        return new Response<>(Status.OK, "Vuelo válido", flight);
    }

    public static Response<Void> validatePassengerCapacity(Flight flight) {
        int capacidad = flight.getPlane().getMaxCapacity();
        int ocupados = flight.getPassengers().size();

        if (ocupados >= capacidad) {
            return new Response<>(Status.BAD_REQUEST, "El avión ya alcanzó su capacidad máxima (" + capacidad + " pasajeros)");
        }

        return null; // ✅ Todo correcto
    }

    public static String generarMensajeCapacidadRestante(Flight flight) {
        int disponibles = flight.getPlane().getMaxCapacity() - flight.getPassengers().size();

        if (disponibles == 0) {
            return "Pasajero agregado. El avión está ahora completamente lleno.";
        } else if (disponibles <= 3) {
            return "Pasajero agregado exitosamente. Quedan solo " + disponibles + " asientos.";
        } else {
            return "Pasajero agregado exitosamente.";
        }
    }
}
