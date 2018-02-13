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
    public static final String FILE_SIZE = "kittens";
    public static void main(String[] args) {

        try {
            List<String> raw = Files.lines(Paths.get("../StreamingVideo/src/"+FILE_SIZE+".in")).collect(Collectors.toList());

            String[] details = raw.get(0).split(" ");
            raw.remove(0);

            int HEIGHT = Integer.valueOf(details[0]);
            int WIDTH = Integer.valueOf(details[1]);
            int MIN = Integer.valueOf(details[2]);
            int MAX = Integer.valueOf(details[3]);

            char[][] parsed = new char[WIDTH][HEIGHT];
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    parsed[i][j] = raw.get(j).charAt(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
