package airport.core.models.storage.adapters;

import airport.core.models.Location;
import org.json.JSONObject;

public class LocationAdapter implements JsonAdapter<Location> {

    @Override
    public JSONObject toJSON(Location location) {
        JSONObject json = new JSONObject();
        json.put("airportId", location.getAirportId());
        json.put("airportName", location.getAirportName());
        json.put("airportCity", location.getAirportCity());
        json.put("airportCountry", location.getAirportCountry());
        json.put("airportLatitude", location.getAirportLatitude());
        json.put("airportLongitude", location.getAirportLongitude());
        return json;
    }

    @Override
    public Location fromJSON(JSONObject json) {
        return new Location(
            json.getString("airportId"),
            json.getString("airportName"),
            json.getString("airportCity"),
            json.getString("airportCountry"),
            json.getDouble("airportLatitude"),
            json.getDouble("airportLongitude")
        );
    }
}
