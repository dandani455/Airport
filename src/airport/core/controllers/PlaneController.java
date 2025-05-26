package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Plane;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PlaneAdapter;

import java.util.Comparator;
import java.util.List;

public class PlaneController extends BaseController {

    private final JsonRepository<Plane> repo;

    public PlaneController() {
        this.repo = new JsonRepository<>("json/planes.json", new PlaneAdapter());
    }

    public Response<Void> createPlane(String id, String brand, String model, String capacityStr, String airline) {
        final String finalId = id;
        try {
            if (id == null || brand == null || model == null || capacityStr == null || airline == null
                    || id.trim().isEmpty() || brand.trim().isEmpty() || model.trim().isEmpty()
                    || capacityStr.trim().isEmpty() || airline.trim().isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Todos los campos son obligatorios");
            }

            id = id.trim();
            brand = brand.trim();
            model = model.trim();
            airline = airline.trim();

            if (id.length() != 7
                    || !Character.isUpperCase(id.charAt(0)) || !Character.isUpperCase(id.charAt(1))
                    || !id.substring(2).matches("\\d{5}")) {
                return new Response<>(Status.BAD_REQUEST, "El ID debe tener el formato XXYYYYY (dos letras y cinco dígitos)");
            }

            List<Plane> planes = repo.getAll();
            boolean existe = planes.stream().anyMatch(p -> p.getId().equals(finalId));
            if (existe) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe un avión con ese ID");
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0) {
                    return new Response<>(Status.BAD_REQUEST, "La capacidad debe ser mayor que cero");
                }
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "La capacidad debe ser un número entero válido");
            }

            Plane plane = new Plane(id, brand, model, capacity, airline);
            repo.add(plane);

            notifyObservers(); // 🔔 Notifica a la vista que se actualice la tabla

            return new Response<>(Status.OK, "Avión creado exitosamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error al crear avión");
        }
    }

    public Response<List<Plane>> getAllPlanes() {
        try {
            List<Plane> list = repo.getAll();
            list.sort(Comparator.comparing(Plane::getId));
            return new Response<>(Status.OK, "Aviones obtenidos exitosamente", list);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo aviones", null);
        }
    }
}
