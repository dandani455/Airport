package airport.core.controllers;

import airport.Flight;
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

public class PassengerController extends BaseController {

    private final JsonRepository<Passenger> repo;

    public PassengerController() {
        this.repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter());
    }

    public PassengerController(List<Flight> flights) {
        this.repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter(flights));
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
            // ✅ Validación de campos vacíos
            if (firstname == null || firstname.trim().isEmpty()
                    || lastname == null || lastname.trim().isEmpty()
                    || birthYear == null || birthYear.trim().isEmpty()
                    || birthMonth == null || birthMonth.trim().isEmpty()
                    || birthDay == null || birthDay.trim().isEmpty()
                    || countryCode == null || countryCode.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()
                    || country == null || country.trim().isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Ningún campo puede estar vacío");
            }

            // ✅ Validar ID
            if (id < 0 || String.valueOf(id).length() > 15) {
                return new Response<>(Status.BAD_REQUEST, "ID inválido: debe ser ≥ 0 y tener máximo 15 dígitos.");
            }

            // ✅ Validar fecha de nacimiento
            int y, m, d;
            try {
                y = Integer.parseInt(birthYear);
                m = Integer.parseInt(birthMonth);
                d = Integer.parseInt(birthDay);
                if (y < 1900 || y > LocalDate.now().getYear()) {
                    return new Response<>(Status.BAD_REQUEST, "Año de nacimiento inválido (>=1900)");
                }
            } catch (Exception e) {
                return new Response<>(Status.BAD_REQUEST, "Fecha de nacimiento inválida");
            }
            LocalDate birth = LocalDate.of(y, m, d);

            // ✅ Validar código de país
            int code;
            try {
                code = Integer.parseInt(countryCode);
                if (code < 0 || String.valueOf(code).length() > 3) {
                    return new Response<>(Status.BAD_REQUEST, "Código de país inválido: máximo 3 dígitos");
                }
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "Código de país inválido");
            }

            // ✅ Validar número de teléfono
            long phoneNum;
            try {
                phoneNum = Long.parseLong(phone);
                if (phoneNum < 0 || String.valueOf(phoneNum).length() > 11) {
                    return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido: máximo 11 dígitos");
                }
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido");
            }

            // ✅ Buscar si el pasajero ya existe
            Passenger existing = repo.find(p -> p.getId() == id).orElse(null);
            if (existing == null) {
                // CREAR nuevo pasajero
                Passenger newPassenger = new Passenger(id, firstname.trim(), lastname.trim(), birth, code, phoneNum, country.trim());
                repo.add(newPassenger);
                notifyObservers();
                return new Response<>(Status.OK, "Pasajero creado exitosamente");
            }

            // ACTUALIZAR pasajero existente
            boolean changed = false;

            if (!firstname.trim().equals(existing.getFirstname())) {
                existing.setFirstname(firstname.trim());
                changed = true;
            }

            if (!lastname.trim().equals(existing.getLastname())) {
                existing.setLastname(lastname.trim());
                changed = true;
            }

            if (!birth.equals(existing.getBirthDate())) {
                existing.setBirthDate(birth);
                changed = true;
            }

            if (!country.trim().equals(existing.getCountry())) {
                existing.setCountry(country.trim());
                changed = true;
            }

            if (code != existing.getCountryPhoneCode()) {
                existing.setCountryPhoneCode(code);
                changed = true;
            }

            if (phoneNum != existing.getPhone()) {
                existing.setPhone(phoneNum);
                changed = true;
            }

            if (!changed) {
                return new Response<>(Status.NO_CONTENT, "Información no cambiada porque está igual");
            }

            repo.update(list -> {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == id) {
                        list.set(i, existing);
                        return true;
                    }
                }
                return false;
            });

            notifyObservers();
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

    public void forceUpdatePassenger(Passenger updated) {
        repo.update(list -> {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == updated.getId()) {
                    list.set(i, updated);
                    notifyObservers();
                    return true;
                }
            }
            return false;
        });
    }
}
