package airport.core.models.storage;

import airport.core.models.storage.adapters.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class JsonRepository<T> {
    private final String path;
    private final JsonAdapter<T> adapter;
    private final List<T> items;

    public JsonRepository(String path, JsonAdapter<T> adapter) {
        this.path = path;
        this.adapter = adapter;
        this.items = loadFromFile();
    }

    private List<T> loadFromFile() {
        List<T> result = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                result.add(adapter.fromJSON(array.getJSONObject(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveToFile() {
        JSONArray array = new JSONArray();
        for (T item : items) {
            array.put(adapter.toJSON(item));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            writer.write(array.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public void add(T item) {
        items.add(item);
        saveToFile();
    }

    public boolean remove(Predicate<T> condition) {
        boolean removed = items.removeIf(condition);
        if (removed) saveToFile();
        return removed;
    }

    public Optional<T> find(Predicate<T> condition) {
        return items.stream().filter(condition).findFirst();
    }

    public void update(Function<List<T>, Boolean> updateLogic) {
        boolean changed = updateLogic.apply(items);
        if (changed) saveToFile();
    }
}
