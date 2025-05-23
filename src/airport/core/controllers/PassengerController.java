package airport.core.controllers;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Passenger;
import airport.core.models.storage.JsonRepository;
import airport.core.models.storage.adapters.PassengerAdapter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PassengerController {

    private final JsonRepository<Passenger> repo;

    public PassengerController() {
        this.repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter());
    }

    public Response<List<Passenger>> getAllPassengers() {
        try {
            List<Passenger> list = repo.getAll();
            list.sort(Comparator.comparingLong(Passenger::getId));
            return new Response<>(Status.OK, "Pasajeros obtenidos", list);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo pasajeros", Collections.emptyList());
        }
    }

    public Response<Passenger> getPassengerById(long id) {
        try {
            Passenger passenger = repo.find(p -> p.getId() == id).orElse(null);
            if (passenger == null) {
                return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
            }
            return new Response<>(Status.OK, "Pasajero encontrado", passenger);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error buscando pasajero");
        }
    }

    public Response<Void> updatePassenger(
            long id, String firstname, String lastname, String birthYear,
            String birthMonth, String birthDay, String countryCode,
            String phone, String country
    ) {
        try {
            // Validar ID
            if (id < 0 || String.valueOf(id).length() > 15) {
                return new Response<>(Status.BAD_REQUEST, "ID inválido: debe ser ≥ 0 y tener máximo 15 dígitos.");
            }

            Passenger existing = repo.find(p -> p.getId() == id).orElse(null);
            if (existing == null) {
                return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
            }

            boolean changed = false;

            // Validar campos no vacíos y actualizarlos
            if (firstname != null && !firstname.trim().isEmpty() && !firstname.equals(existing.getFirstname())) {
                existing.setFirstname(firstname.trim());
                changed = true;
            }

            if (lastname != null && !lastname.trim().isEmpty() && !lastname.equals(existing.getLastname())) {
                existing.setLastname(lastname.trim());
                changed = true;
            }

            if (country != null && !country.trim().isEmpty() && !country.equals(existing.getCountry())) {
                existing.setCountry(country.trim());
                changed = true;
            }

            // Validar y actualizar fecha de nacimiento
            if (birthYear != null && birthMonth != null && birthDay != null
                    && !birthYear.trim().isEmpty() && !birthMonth.trim().isEmpty() && !birthDay.trim().isEmpty()) {

                try {
                    int y = Integer.parseInt(birthYear);
                    int m = Integer.parseInt(birthMonth);
                    int d = Integer.parseInt(birthDay);
                    
                    if (y < 1900 || y > LocalDate.now().getYear()) {
                        return new Response<>(Status.BAD_REQUEST, "Año de nacimiento inválido. Debe ser mayor a 1900.");
                    }
                    LocalDate newBirth = LocalDate.of(y, m, d);
                    
                    if (!newBirth.equals(existing.getBirthDate())) {
                        existing.setBirthDate(newBirth);
                        changed = true;
                    }
                } catch (Exception e) {
                    return new Response<>(Status.BAD_REQUEST, "Fecha de nacimiento inválida");
                }
            }

            // Validar código de país
            if (countryCode != null && !countryCode.trim().isEmpty()) {
                try {
                    int code = Integer.parseInt(countryCode);
                    if (code < 0 || String.valueOf(code).length() > 3) {
                        return new Response<>(Status.BAD_REQUEST, "Código de país inválido: máximo 3 dígitos");
                    }
                    if (code != existing.getCountryPhoneCode()) {
                        existing.setCountryPhoneCode(code);
                        changed = true;
                    }
                } catch (NumberFormatException e) {
                    return new Response<>(Status.BAD_REQUEST, "Código de país inválido");
                }
            }

            // Validar número de teléfono
            if (phone != null && !phone.trim().isEmpty()) {
                try {
                    long phoneNum = Long.parseLong(phone);
                    if (phoneNum < 0 || String.valueOf(phoneNum).length() > 11) {
                        return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido: máximo 11 dígitos");
                    }
                    if (phoneNum != existing.getPhone()) {
                        existing.setPhone(phoneNum);
                        changed = true;
                    }
                } catch (NumberFormatException e) {
                    return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido");
                }
            }

            if (!changed) {
                return new Response<>(Status.NO_CONTENT, "Información no cambiada porque está igual");
            }

            // Reemplazar el pasajero en la lista y guardar
            repo.update(list -> {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == id) {
                        list.set(i, existing);
                        return true;
                    }
                }
                return false;
            });

            return new Response<>(Status.OK, "Pasajero actualizado exitosamente");

        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error actualizando pasajero");
        }
    }

    public Response<List<String>> getPassengerNameLabels() {
        try {
            List<Passenger> list = repo.getAll();
            list.sort(Comparator.comparingLong(Passenger::getId));

            List<String> names = list.stream()
                    .map(p -> p.getFirstname() + " " + p.getLastname() + " (ID: " + p.getId() + ")")
                    .collect(Collectors.toList());

            return new Response<>(Status.OK, "Nombres con ID obtenidos", names);
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error obteniendo nombres", Collections.emptyList());
        }
    }

    public Response<Passenger> getPassengerByFullName(String name) {
        try {
            return repo.find(p -> p.getFullname().equals(name))
                    .map(p -> new Response<>(Status.OK, "Pasajero encontrado", p))
                    .orElseGet(() -> new Response<>(Status.NOT_FOUND, "Pasajero no encontrado"));
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error buscando pasajero");
        }
    }
}
