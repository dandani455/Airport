package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.controllers.validators.PlaneValidator;
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
        try {
            Response<Plane> valid = PlaneValidator.validate(id, brand, model, capacityStr, airline);
            if (valid.getStatus() != Status.OK) {
                return new Response<>(valid.getStatus(), valid.getMessage());
            }

            Plane plane = valid.getObject();

            boolean existe = repo.getAll().stream().anyMatch(p -> p.getId().equals(plane.getId()));
            if (existe) {
                return new Response<>(Status.BAD_REQUEST, "Ya existe un avión con ese ID");
            }

            repo.add(plane);
            notifyObservers();

            return new Response<>(Status.CREATED, "Avión creado exitosamente");
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
