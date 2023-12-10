import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CliUtils {
    public static String getExecutorId() {
        String path = "/sys/devices/virtual/dmi/id/board_asset_tag";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            return br.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
