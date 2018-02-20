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

    //public static final String FILE_NAME = "videos_worth_spreading";
    public static ArrayList<String> fileNames;

    public static void main(String[] args) {
        fileNames = new ArrayList<>();
        fileNames.add("kittens");
        fileNames.add("videos_worth_spreading");
        fileNames.add("me_at_the_zoo");
        fileNames.add("videos_worth_spreading");
        try {
            for (String FILE_NAME: fileNames) {
                List<String> raw = Files.lines(Paths.get("../StreamingVideo/src/" + FILE_NAME + ".in")).collect(Collectors.toList());
                Solution solution = new Solution(raw,FILE_NAME);
                solution.solve();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}