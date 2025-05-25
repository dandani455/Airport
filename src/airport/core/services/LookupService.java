package airport.core.services;

import airport.core.models.Location;
import airport.core.models.Plane;

import java.util.List;

public class LookupService {
    
    private final List<Plane> planes;
    private final List<Location> locations;

    public LookupService(List<Plane> planes, List<Location> locations) {
        this.planes = planes;
        this.locations = locations;
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
}