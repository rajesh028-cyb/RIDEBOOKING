
import com.ridebooking.cli.DriverCli;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.service.DriverService;

public class Main {

    public static void main(String[] args) {

        DriverRepository driverRepository =
                new DriverRepository();

        DriverService driverService =
                new DriverService(driverRepository);

        DriverCli driverCli =
                new DriverCli(driverService);

        driverCli.registerDriver();
    }
}
