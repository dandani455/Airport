package airport.core.models.storage.adapters;

import airport.Flight;
import airport.core.models.Location;
import airport.core.models.Plane;
import airport.core.services.LookupService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class FlightAdapter implements JsonAdapter<Flight> {

    private final LookupService lookup;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlightAdapter(LookupService lookup) {
        this.lookup = lookup;
    }

    @Override
    public JSONObject toJSON(Flight flight) {
        JSONObject json = new JSONObject();
        json.put("id", flight.getId());
        json.put("plane", flight.getPlane().getId());
        json.put("departureLocation", flight.getDepartureLocation().getAirportId());
        json.put("arrivalLocation", flight.getArrivalLocation().getAirportId());
        json.put("scaleLocation", flight.getScaleLocation() == null ? JSONObject.NULL : flight.getScaleLocation().getAirportId());
        json.put("departureDate", flight.getDepartureDate().format(FORMATTER));
        json.put("hoursDurationArrival", flight.getHoursDurationArrival());
        json.put("minutesDurationArrival", flight.getMinutesDurationArrival());
        json.put("hoursDurationScale", flight.getHoursDurationScale());
        json.put("minutesDurationScale", flight.getMinutesDurationScale());
        return json;
    }

    @Override
    public Flight fromJSON(JSONObject json) {
        String id = json.getString("id");

        String planeId = json.getString("plane");
        Plane plane = lookup.getPlane(planeId);

        String departureId = json.getString("departureLocation");
        String arrivalId = json.getString("arrivalLocation");
        Location departure = lookup.getLocation(departureId);
        Location arrival = lookup.getLocation(arrivalId);

        Location scale = null;
        if (!json.isNull("scaleLocation")) {
            String scaleId = json.getString("scaleLocation");
            scale = lookup.getLocation(scaleId);
        }

        LocalDateTime departureDate = LocalDateTime.parse(json.getString("departureDate"), FORMATTER);

        int hArrival = json.getInt("hoursDurationArrival");
        int mArrival = json.getInt("minutesDurationArrival");
        int hScale = json.getInt("hoursDurationScale");
        int mScale = json.getInt("minutesDurationScale");

        if (scale == null) {
            return new Flight(id, plane, departure, arrival, departureDate, hArrival, mArrival);
        } else {
            return new Flight(id, plane, departure, scale, arrival, departureDate, hArrival, mArrival, hScale, mScale);
        }
    }
}
