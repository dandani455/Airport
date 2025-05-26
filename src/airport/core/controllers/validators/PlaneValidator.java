package airport.core.controllers.validators;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Plane;

public class PlaneValidator {

    public static Response<Plane> validate(String id, String brand, String model, String capacityStr, String airline) {
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
        return new Response<>(Status.OK, "Datos del avión validados", plane);
    }
}
