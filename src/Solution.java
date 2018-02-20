import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Kamil Klosowski
 * @project StreamingVideo
 * @since 20/02/2018
 */
public class Solution {

    private final List<String> raw;
    int[] params;
    int[] videoSizes;
    Endpoint[] endpoints;
    int requestNo;
    int[] caches;

    public Solution(List<String> raw) throws IOException {
        this.raw = raw;

       params = Arrays.stream(raw.get(0).split(" ")).mapToInt(Integer::valueOf).toArray();
       videoSizes = Arrays.stream(raw.get(1).split(" ")).mapToInt(Integer::valueOf).toArray();
       endpoints = new Endpoint[params[1]];
       requestNo = params[2];
       caches = IntStream.generate(() -> params[4]).limit(params[3]).toArray();

        //Latencies from endpoints to each cache
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
            endpoints[epnum] = new Endpoint(epnum, latencies[i][0]);
            int len = latencies[i][1];
            i++;
            for (int j = 0; j < len; j++, i++) {
                endpoints[epnum].addEpLatency(latencies[i][0], latencies[i][1]);
            }
        }

        //Assign requests to endpoints
        Arrays.stream(requests).forEach(x -> endpoints[x.requestingEndpoint].addRequest(x));

    }

    public void solve(){

    }

    //Time saved for each cache for a given request from an endpoint
    public Integer[][] requestTimeSavedPerCache(Request request, Endpoint endpoint) {
        return endpoint.cacheLatencies.entrySet().stream()
                .map(x -> new Integer[]{x.getKey(), Math.abs(x.getValue() - endpoint.dcLatency)})
                .toArray(Integer[][]::new);
    }

    //Best possible time saving for each cache (bundles requests from ech endpoint connected to cache)
    public List<Request> bestAllocationPerCache(int cacheNum, Request[] requests) {


        return null;
    }

    public void bestCachePerRequestCumulative(Request[] requests) {
        //For each video get all the requests
        Map<Integer, Request[]> requestsByVideo = null;
        Arrays.stream(requests).collect(Collectors.groupingBy(Request::getVideoNumber));

//        for (int i = 0; i < ; i++) {
//
//        }
//        requestsByVideo.entrySet().stream().filter(x -> isConnected())
        //Calculate the time saved on each cache allocation for every video
        //Find cumulative time saved by each cache
        //Sort by time saved
//        requestsByVideo.
        //Find best cache for that single video
        //Return <request, cache, timesaved>


    }

    //Best caches sorted by the time saved to cache descending
    public List<Integer[]> bestCachesPerRequest(Request request, Endpoint endpoint) {
        return Arrays.stream(requestTimeSavedPerCache(request, endpoint))
                .sorted(Comparator.comparing(x -> ((int[]) x)[1]).reversed())
                .collect(Collectors.toList());
    }
}
