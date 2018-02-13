import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oskar
 * @version 1.0
 * created on: 13/02/2018.
 */
public class Main {
    public static final String FILE_SIZE = "me_at_the_zoo";
    public static void main(String[] args) {

        try {
            List<String> raw = Files.lines(Paths.get("../StreamingVideo/src/"+FILE_SIZE+".in")).collect(Collectors.toList());

            String[] data = raw.get(0).split(" ");
            raw.remove(0);

            int videoNo = Integer.valueOf(data[0]);
            int endpointNo = Integer.valueOf(data[1]);
            int requestNo = Integer.valueOf(data[2]);
            int cacheNo = Integer.valueOf(data[3]);
            int cacheSize = Integer.valueOf(data[4]);


            /*
            char[][] parsed = new char[WIDTH][HEIGHT];
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    parsed[i][j] = raw.get(j).charAt(i);
                }
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
