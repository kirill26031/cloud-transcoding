import java.io.*;
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

    public static boolean executeVideoConversion(String command, String inputFile, String outputFile) {
        command = command.replace("{input}", inputFile).replace("{output}", outputFile);
        command = "ffmpeg " + command;
        Runtime rt = Runtime.getRuntime();
        try {
            Process process = rt.exec(command.split(" "));

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            System.err.println(output);
//            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            output = new StringBuilder();
//            while ((line = reader.readLine()) != null) {
//                output.append(line);
//            }
//            System.out.println(output);

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Error on execution of " + command);
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error on execution of " + command);
            return false;
        }
    }
}
