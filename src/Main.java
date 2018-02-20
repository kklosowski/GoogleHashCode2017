import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oskar
 * @version 1.0
 *          created on: 13/02/2018.
 */
public class Main {

    public static final String FILE_NAME = "me_at_the_zoo";

    public static void main(String[] args) {
        try {
            List<String> raw = Files.lines(Paths.get("../StreamingVideo/src/" + FILE_NAME + ".in")).collect(Collectors.toList());
            Solution solution = new Solution(raw);
            solution.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
