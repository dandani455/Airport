
package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Plane;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PlaneAdapter;

import java.util.Comparator;
import java.util.List;

public class PlaneController {

    private final JsonRepository<Plane> repo;

    public PlaneController() {
        this.repo = new JsonRepository<>("json/planes.json", new PlaneAdapter());
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
