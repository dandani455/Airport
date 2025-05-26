package airport.core.controllers.validators;

import airport.core.controllers.utils.Response;
import airport.core.controllers.utils.Status;
import airport.core.models.Passenger;

import java.time.LocalDate;

public class PassengerValidator {

    public static Response<Passenger> validate(long id, String firstname, String lastname, String birthYear, String birthMonth,
            String birthDay, String countryCode, String phone, String country) {
        try {
            if (firstname.isEmpty() || lastname.isEmpty() || birthYear.isEmpty() || birthMonth.isEmpty() || birthDay.isEmpty()
                    || countryCode.isEmpty() || phone.isEmpty() || country.isEmpty()) {
                return new Response<>(Status.BAD_REQUEST, "Ningún campo puede estar vacío");
            }

            if (id < 0 || String.valueOf(id).length() > 15) {
                return new Response<>(Status.BAD_REQUEST, "ID inválido: debe ser ≥ 0 y ≤ 15 dígitos");
            }

            int year = Integer.parseInt(birthYear);
            int month = Integer.parseInt(birthMonth);
            int day = Integer.parseInt(birthDay);

            if (year < 1900 || year > LocalDate.now().getYear()) {
                return new Response<>(Status.BAD_REQUEST, "Año inválido (≥ 1900)");
            }

            LocalDate birth = LocalDate.of(year, month, day);

            int code = Integer.parseInt(countryCode);
            if (code < 0 || String.valueOf(code).length() > 3) {
                return new Response<>(Status.BAD_REQUEST, "Código de país inválido (máx. 3 dígitos)");
            }

            long phoneNumber = Long.parseLong(phone);
            if (phoneNumber < 0 || String.valueOf(phoneNumber).length() > 11) {
                return new Response<>(Status.BAD_REQUEST, "Teléfono inválido (máx. 11 dígitos)");
            }

            Passenger passenger = new Passenger(id, firstname.trim(), lastname.trim(), birth, code, phoneNumber, country.trim());
            return new Response<>(Status.OK, "Datos validados", passenger);

        } catch (Exception e) {
            return new Response<>(Status.BAD_REQUEST, "Datos inválidos: " + e.getMessage());
        }
    }
}
