package airport.core.models.storage.adapters;

import airport.Flight;
import airport.core.models.Passenger;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PassengerAdapter implements JsonAdapter<Passenger> {

    private final List<Flight> flights;

    public PassengerAdapter(List<Flight> flights) {
        this.flights = flights;
    }

    public PassengerAdapter() {
        this.flights = Collections.emptyList();
    }

    @Override
    public Passenger fromJSON(JSONObject obj) {
        return new Passenger(
                obj.getLong("id"),
                obj.getString("firstname"),
                obj.getString("lastname"),
                LocalDate.parse(obj.getString("birthDate")),
                obj.getInt("countryPhoneCode"),
                obj.getLong("phone"),
                obj.getString("country")
        );
    }

    @Override
    public JSONObject toJSON(Passenger p) {
        JSONObject obj = new JSONObject();
        obj.put("id", p.getId());
        obj.put("firstname", p.getFirstname());
        obj.put("lastname", p.getLastname());
        obj.put("birthDate", p.getBirthDate().toString());
        obj.put("countryPhoneCode", p.getCountryPhoneCode());
        obj.put("phone", p.getPhone());
        obj.put("country", p.getCountry());
        return obj;
    }
}
