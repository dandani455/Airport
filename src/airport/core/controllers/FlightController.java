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
