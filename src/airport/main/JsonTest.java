package airport.main;

import airport.core.models.Passenger;
import airport.core.models.storage.JsonRepository;
import java.time.LocalDate;
import java.util.List;

import airport.core.models.Passenger;
import airport.core.models.storage.adapters.PassengerAdapter;

public class JsonTest {
    public static void main(String[] args) {
        JsonRepository<Passenger> repo = new JsonRepository<>("json/passengers.json", new PassengerAdapter());

        // LISTAR
        System.out.println("=== Lista actual ===");
        List<Passenger> list = repo.getAll();
        for (Passenger passenger : list) {
            System.out.println(passenger.getFullname() + " - " + passenger.getId());
        }

        // ACTUALIZAR
        repo.update(passengers -> {
            for (Passenger pass : passengers) {
                if (pass.getId() == 99999) {
                    pass.setFirstname("Updated");
                    pass.setCountry("UpdatedLand");
                    return true;
                }
            }
            return false;
        });

    }
}
