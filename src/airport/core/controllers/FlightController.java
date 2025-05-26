package airport.core.controllers;

import airport.Flight;
import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.controllers.validators.FlightValidator;
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
import java.util.stream.Collectors;

public class FlightController extends BaseController {

    private final JsonRepository<Flight> repo;
    private final List<Passenger> passengers;
    private final LookupService lookup;

    public FlightController(List<Plane> planes, List<Location> locations, List<Passenger> passengers) {
        this.passengers = passengers;
        this.lookup = new LookupService(planes, locations, passengers, null);
        this.repo = new JsonRepository<>("json/flights.json", new FlightAdapter(lookup));
    }

    public FlightController(List<Plane> planes, List<Location> locations) {
        this(
                planes,
                locations,
                new JsonRepository<>("json/passengers.json", new PassengerAdapter()).getAll()
        );
    }

    public Response<Void> createFlight(String id, String planeId, String departureId, String arrivalId, String scaleId,
            LocalDateTime departureDate,
            int durationHours, int durationMinutes,
            int scaleHours, int scaleMinutes) {
        try {
            boolean existe = repo.getAll().stream().anyMatch(f -> f.getId().equals(id));
            if (existe) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe un vuelo con ese ID");
            }

            Response<Flight> valid = FlightValidator.validate(
                    id, planeId, departureId, arrivalId, scaleId, departureDate,
                    durationHours, durationMinutes, scaleHours, scaleMinutes,
                    lookup
            );

            if (valid.getStatus() != Status.OK) {
                return new Response<>(valid.getStatus(), valid.getMessage());
            }

            repo.add(valid.getObject());
            notifyObservers();
            return new Response<>(Status.CREATED, "Vuelo creado exitosamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al crear el vuelo");
        }
    }

    public Response<Void> delayFlight(String flightId, int hours, int minutes) {
        try {
            if (hours < 0 || minutes < 0 || (hours == 0 && minutes == 0)) {
                return new Response<>(Status.BAD_REQUEST, "La duración del retraso debe ser mayor a 00:00");
            }

            Flight flight = repo.getAll().stream()
                    .filter(f -> f.getId().equals(flightId))
                    .findFirst()
                    .orElse(null);

            if (flight == null) {
                return new Response<>(Status.NOT_FOUND, "Vuelo no encontrado");
            }

            flight.delay(hours, minutes);
            notifyObservers();

            return new Response<>(Status.OK, "Vuelo retrasado exitosamente");
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al retrasar vuelo");
        }
    }

    public Response<List<Flight>> getFlightsByPassengerId(long passengerId) {
        try {
            List<Flight> allFlights = repo.getAll();

            List<Flight> filtered = allFlights.stream()
                    .filter(f -> f.getPassengers().stream().anyMatch(p -> p.getId() == passengerId))
                    .sorted(Comparator.comparing(Flight::getDepartureDate))
                    .collect(Collectors.toList());

            return new Response<>(Status.OK, "Vuelos del pasajero obtenidos", filtered);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo vuelos del pasajero", Collections.emptyList());
        }
    }

    public Response<List<Flight>> getAllFlights() {
        try {
            List<Flight> flights = repo.getAll();
            flights.sort(Comparator.comparing(Flight::getDepartureDate));
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
            Flight flight = repo.getAll().stream()
                    .filter(f -> f.getId().equals(flightId))
                    .findFirst()
                    .orElse(null);

            if (flight == null) {
                return new Response<>(Status.NOT_FOUND, "Vuelo no encontrado");
            }

            Passenger passenger = getPassengerByIdLocal(passengerId);
            if (passenger == null) {
                return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
            }

            boolean yaRelacionado = passenger.getFlights().stream().anyMatch(f -> f.getId().equals(flightId))
                    || flight.getPassengers().stream().anyMatch(p -> p.getId() == passengerId);

            if (yaRelacionado) {
                return new Response<>(Status.BAD_REQUEST, "El pasajero ya está en este vuelo");
            }

            Response<Void> valid = FlightValidator.validatePassengerCapacity(flight);
            if (valid != null) {
                return valid;
            }

            passenger.addFlight(flight);
            flight.addPassenger(passenger);

            notifyObservers();
            String mensaje = FlightValidator.generarMensajeCapacidadRestante(flight);
            return new Response<>(Status.OK, mensaje);

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al agregar pasajero al vuelo");
        }
    }
}
