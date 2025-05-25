package airport.core.controllers;

import airport.Flight;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Location;
import airport.core.models.Passenger;
import airport.core.models.Plane;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.FlightAdapter;
import airport.core.models.storage.adapters.PassengerAdapter;
import airport.core.services.LookupService;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FlightController {

    private final JsonRepository<Flight> repo;
    private final List<Passenger> passengers;

    private final PassengerController passengerController;

    public FlightController(List<Plane> planes, List<Location> locations, List<Passenger> passengers) {
        this.passengers = passengers;
        this.passengerController = new PassengerController();

        LookupService lookup = new LookupService(planes, locations);
        this.repo = new JsonRepository<>("json/flights.json", new FlightAdapter(lookup));
    }

    public FlightController(List<Plane> planes, List<Location> locations) {
        this(planes, locations, new JsonRepository<>("json/passengers.json", new PassengerAdapter()).getAll());
    }

    public Response<Void> createFlight(
            String id, Plane plane, Location departure, Location arrival, Location scale,
            LocalDateTime departureDate,
            int durationHours, int durationMinutes,
            int scaleHours, int scaleMinutes
    ) {
        try {
            final String finalId = id; 
            // Validar campos obligatorios
            if (id == null || id.trim().isEmpty() || plane == null || departure == null || arrival == null || departureDate == null) {
                return new Response<>(Status.BAD_REQUEST, "Todos los campos obligatorios deben estar llenos");
            }

            id = id.trim();

            // Validar formato de ID de vuelo: XXXYYY
            if (!id.matches("[A-Z]{3}\\d{3}")) {
                return new Response<>(Status.BAD_REQUEST, "El ID del vuelo debe tener el formato XXXYYY");
            }

            if (scale != null) {
                if (scale.getAirportId().equals(departure.getAirportId())
                        || scale.getAirportId().equals(arrival.getAirportId())) {
                    return new Response<>(Status.BAD_REQUEST, "La escala no puede ser igual a la salida o llegada");
                }
            }
            
            // Validar que el ID sea único
            boolean idExists = repo.getAll().stream().anyMatch(f -> f.getId().equals(finalId));
            if (idExists) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe un vuelo con ese ID");
            }

            // Validar duración > 00:00
            if (durationHours < 0 || durationMinutes < 0 || (durationHours == 0 && durationMinutes == 0)) {
                return new Response<>(Status.BAD_REQUEST, "La duración del vuelo debe ser mayor a 00:00");
            }

            // Si no hay escala, duración de escala debe ser 0
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

            repo.add(flight);
            return new Response<>(Status.CREATED, "Vuelo creado exitosamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al crear el vuelo");
        }
    }

    public Response<List<Flight>> getAllFlights() {
        try {
            List<Flight> flights = repo.getAll();
            flights.sort(Comparator.comparing(Flight::getDepartureDate)); // orden por fecha
            return new Response<>(Status.OK, "Vuelos obtenidos exitosamente", flights);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo vuelos", Collections.emptyList());
        }
    }

    private Passenger getPassengerByIdLocal(long id) {
        return passengers.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Response<Void> addPassengerToFlight(long passengerId, String flightId) {
        try {
            // Buscar vuelo
            Flight flight = repo.getAll().stream()
                    .filter(f -> f.getId().equals(flightId))
                    .findFirst()
                    .orElse(null);

            if (flight == null) {
                return new Response<>(Status.NOT_FOUND, "Vuelo no encontrado");
            }

            // Buscar pasajero desde la lista cargada (solo en sesión)
            Passenger passenger = getPassengerByIdLocal(passengerId);
            if (passenger == null) {
                return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
            }

            // ✅ Validación: Verificar si ya está en el vuelo
            boolean yaRelacionado = passenger.getFlights().stream().anyMatch(f -> f.getId().equals(flightId))
                    || flight.getPassengers().stream().anyMatch(p -> p.getId() == passengerId);

            if (yaRelacionado) {
                return new Response<>(Status.BAD_REQUEST, "El pasajero ya está en este vuelo");
            }

            // Relación en memoria (solo en sesión)
            passenger.addFlight(flight);
            flight.addPassenger(passenger);

            return new Response<>(Status.OK, "Pasajero agregado al vuelo exitosamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al agregar pasajero al vuelo");
        }
    }
}
