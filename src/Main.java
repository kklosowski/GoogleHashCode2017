import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oskar
 * @version 1.0
 * created on: 13/02/2018.
 */
public class Main {
    public static final String FILE_NAME = "me_at_the_zoo";

    public static void main(String[] args) {

        try {
            List<String> raw = Files.lines(Paths.get("../StreamingVideo/src/" + FILE_NAME + ".in")).collect(Collectors.toList());

            int[] params = Arrays.stream(raw.get(0).split(" ")).mapToInt(Integer::valueOf).toArray();
            final int[] videoSizes = Arrays.stream(raw.get(1).split(" ")).mapToInt(Integer::valueOf).toArray();
            Endpoint[] endpoints = new Endpoint[params[1]];
            int requestNo = params[2];
            int[] caches = IntStream.generate(() -> params[4]).limit(params[3]).toArray();

            Integer latencies[][] = raw.stream().filter(x -> x.split(" ").length == 2)
                    .map(x -> Arrays.stream(x.split(" "))
                            .map(Integer::valueOf)
                            .toArray(Integer[]::new))
                    .toArray(Integer[][]::new);

            Request requests[] = raw.stream().filter(x -> x.split(" ").length == 3)
                    .map(x -> Arrays.stream(x.split(" "))
                            .map(Integer::valueOf)
                            .toArray(Integer[]::new))
                    .map(x -> new Request(x[0], x[2], x[1], videoSizes[x[0]]))
                    .toArray(Request[]::new);

            //Latency array to endpoint array
            for (int i = 0, epnum = 0; i < latencies.length; epnum++) {
                endpoints[epnum] = new Endpoint(epnum,latencies[i][0]);
                int len = latencies[i][1];
                i++;
                for (int j = 0; j < len; j++, i++) {
                    endpoints[epnum].addEpLatency(latencies[i][0], latencies[i][1]);
                }
            }

            //Assign requests to endpoints
            Arrays.stream(requests).forEach(x -> endpoints[x.requestingEndpoint].addRequest(x));

            System.out.println("debug");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Best possible time saving for each cache (bundles requests from ech endpoint connected to cache)
    public List<Request> bestAllocationPerCache(int cacheNum){
        return null;
    }

    //Best caches sorted by the time saved to cache descending
    public List<Integer[]> bestCachesPerRequest(Request request, Endpoint endpoint){
        return Arrays.stream(requestTimeSavedPerCache(request, endpoint))
                .sorted(Comparator.comparing(x -> ((int[]) x)[1]).reversed())
                .collect(Collectors.toList());
    }

    //Time saved for each cache for a given request from an endpoint
    public static Integer[][] requestTimeSavedPerCache(Request request, Endpoint endpoint){
        return endpoint.cacheLatencies.entrySet().stream()
                .map(x -> new Integer[]{x.getKey(), Math.abs(x.getValue() - endpoint.dcLatency)})
                .toArray(Integer[][]::new);
    }
}
