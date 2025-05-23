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
            Passenger existing = repo.find(p -> p.getId() == id).orElse(null);
            if (existing == null) {
                return new Response<>(Status.NOT_FOUND, "Pasajero no encontrado");
            }

            if (firstname == null || firstname.trim().isEmpty()
                    || lastname == null || lastname.trim().isEmpty()
                    || country == null || country.trim().isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Ningún campo debe estar vacío");
            }

            int y, m, d;
            try {
                y = Integer.parseInt(birthYear);
                m = Integer.parseInt(birthMonth);
                d = Integer.parseInt(birthDay);
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "Fecha de nacimiento inválida");
            }

            LocalDate birthDate;
            try {
                birthDate = LocalDate.of(y, m, d);
            } catch (Exception e) {
                return new Response<>(Status.BAD_REQUEST, "Fecha de nacimiento no existe");
            }

            int code;
            try {
                code = Integer.parseInt(countryCode);
                if (code < 0 || String.valueOf(code).length() > 3) {
                    return new Response<>(Status.BAD_REQUEST, "Código de país inválido");
                }
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "Código de país inválido");
            }

            long phoneNum;
            try {
                phoneNum = Long.parseLong(phone);
                if (phoneNum < 0 || String.valueOf(phoneNum).length() > 11) {
                    return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido");
                }
            } catch (NumberFormatException e) {
                return new Response<>(Status.BAD_REQUEST, "Número de teléfono inválido");
            }

            repo.update(list -> {
                for (Passenger p : list) {
                    if (p.getId() == id) {
                        p.setFirstname(firstname.trim());
                        p.setLastname(lastname.trim());
                        p.setCountry(country.trim());
                        p.setBirthDate(birthDate);
                        p.setCountryPhoneCode(code);
                        p.setPhone(phoneNum);
                        return true; // Se hizo una modificación
                    }
                }
                return false; // No se encontró el pasajero
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
            return repo.find(p -> (p.getFirstname() + " " + p.getLastname()).equals(name))
                    .map(p -> new Response<>(Status.OK, "Pasajero encontrado", p))
                    .orElseGet(() -> new Response<>(Status.NOT_FOUND, "Pasajero no encontrado"));
        } catch (Exception e) {
            return new Response<>(Status.INTERNAL_SERVER_ERROR, "Error buscando pasajero");
        }
    }
}
