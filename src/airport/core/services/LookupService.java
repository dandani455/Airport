package airport.core.services;

import airport.Flight;
import airport.core.models.Location;
import airport.core.models.Passenger;
import airport.core.models.Plane;
import java.util.ArrayList;

import java.util.List;

public class LookupService {

    private final List<Plane> planes;
    private final List<Location> locations;
    private final List<Passenger> passengers;
    private final List<Flight> flights;

    public LookupService(List<Plane> planes, List<Location> locations, List<Passenger> passengers, List<Flight> flights) {
        this.planes = planes;
        this.locations = locations;
        this.passengers = passengers;
        this.flights = flights;
    }

    public LookupService(List<Plane> planes, List<Location> locations) {
        this(planes, locations, new ArrayList<>(), new ArrayList<>());
    }

    public Plane getPlane(String id) {
        return planes.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Location getLocation(String id) {
        return locations.stream()
                .filter(l -> l.getAirportId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Passenger getPassenger(long id) {
        return passengers.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Flight getFlight(String id) {
        return flights.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}