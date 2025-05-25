package airport.core.models.storage.adapters;

import airport.core.models.Plane;
import org.json.JSONObject;

public class PlaneAdapter implements JsonAdapter<Plane> {

    @Override
    public JSONObject toJSON(Plane plane) {
        JSONObject json = new JSONObject();
        json.put("id", plane.getId());
        json.put("brand", plane.getBrand());
        json.put("model", plane.getModel());
        json.put("maxCapacity", plane.getMaxCapacity());
        json.put("airline", plane.getAirline());
        return json;
    }

    @Override
    public Plane fromJSON(JSONObject json) {
        String id = json.getString("id");
        String brand = json.getString("brand");
        String model = json.getString("model");
        int maxCapacity = json.getInt("maxCapacity");
        String airline = json.getString("airline");

        return new Plane(id, brand, model, maxCapacity, airline);
    }
}

