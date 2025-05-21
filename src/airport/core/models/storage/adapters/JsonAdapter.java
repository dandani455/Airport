package airport.core.models.storage.adapters;

import org.json.JSONObject;

public interface JsonAdapter<T> {
    
    T fromJSON(JSONObject obj);
    JSONObject toJSON(T obj);

}
