import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Kamil Klosowski
 * @project StreamingVideo
 * @since 20/02/2018
 */
public class Solution {

    public final String FILE_NAME;
    private final List<String> raw;
    int[] params;
    int[] videoSizes;
    Endpoint[] endpoints;
    int requestNo;
    int[] caches;
    Request requests[];
    Integer latencies[][];

    public Solution(List<String> raw, String filename) {
        this.FILE_NAME = filename;
       this.raw = raw;
       params = Arrays.stream(raw.get(0).split(" ")).mapToInt(Integer::valueOf).toArray();
       videoSizes = Arrays.stream(raw.get(1).split(" ")).mapToInt(Integer::valueOf).toArray();
       endpoints = new Endpoint[params[1]];
       requestNo = params[2];
       caches = IntStream.generate(() -> params[4]).limit(params[3]).toArray();

        //Latencies from endpoints to each cache
         latencies = raw.stream().filter(x -> x.split(" ").length == 2)
                .map(x -> Arrays.stream(x.split(" "))
                        .map(Integer::valueOf)
                        .toArray(Integer[]::new))
                .toArray(Integer[][]::new);

        requests = raw.stream().filter(x -> x.split(" ").length == 3)
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
        generateOutput(generateVideoDistrubution(requests));
        System.out.println(FILE_NAME);
        System.out.println(Arrays.toString(caches));
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
    
    /**
    * Checks if a video fits in a cache
    * @param cache - a particular cache
    * @param video - a particular video
    * @return
    */
    public boolean videoFits(int cache, int video){
        return this.caches[cache] >= videoSizes[video];
    }



    public Map<Integer, List<Integer>> generateVideoDistrubution(Request[] requests) {
        //For each video get all the requests
        Map<Integer, List<Request>> requestsByVideo = Arrays.stream(requests).collect(Collectors.groupingBy(Request::getVideoNumber));

        Map<Integer, List<int[]>> cacheVideoTimeSaved = new HashMap<>();

        Map<Integer, List<Integer>> output = new HashMap<>();

        for (int i = 0; i < caches.length; i++) {
            int finalI = i;
            output.put(i, new ArrayList<>());
            List<int[]> videoTimeSaved = requestsByVideo.entrySet().stream()
                    .filter(x -> isConnected(finalI, x.getValue().stream()
                            .map(y-> endpoints[y.requestingEndpoint]).collect(Collectors.toList())))
                    .map(x -> new int[]{
                            x.getKey(),
                            x.getValue().stream()
                            .mapToInt(y -> calculateTimeSaved(y, finalI))
                            .sum()})
                    .sorted(Comparator.comparing(x -> ((int[])x)[1]).reversed())
                    .collect(Collectors.toList());
            cacheVideoTimeSaved.put(finalI, videoTimeSaved);
        }


        cacheVideoTimeSaved.entrySet().forEach(
                x -> {
                    while (x.getValue().stream().anyMatch(y -> videoFits(x.getKey(), y[0])))
                    {
                        List<int[]> copy = new ArrayList<>(x.getValue());
                        copy.stream()
                                .filter(y -> videoFits(x.getKey(), y[0]))
                                .forEach(y -> {
                                    putVideoInCache(x.getKey(), y[0]);
                                    output.get(x.getKey()).add(y[0]);
                                    x.getValue().remove(y);
                                });
                    }
                });

        return output;

        //Calculate the time saved on each cache allocation for every video
        //Find cumulative time saved by each cache
        //Sort by time saved
//        requestsByVideo.
        //Find best cache for that single video
        //Return <request, cache, timesaved>


    }

    public void putVideoInCache(int cacheNumber, int videoNumber){
        this.caches[cacheNumber] -= this.videoSizes[videoNumber];
    }

    //Best caches sorted by the time saved to cache descending
    public List<Integer[]> bestCachesPerRequest(Request request, Endpoint endpoint) {
        return Arrays.stream(requestTimeSavedPerCache(request, endpoint))
                .sorted(Comparator.comparing(x -> ((int[]) x)[1]).reversed())
                .collect(Collectors.toList());
    }

    public boolean isConnected (int cacheNo, List<Endpoint> endpoints){
        return 0 < endpoints.stream().filter(x -> x.cacheLatencies.containsKey(cacheNo)).count();
    }

    public int calculateTimeSaved(Request request, int cacheNo){
        Endpoint endpoint = endpoints[request.requestingEndpoint];
        return endpoint.dcLatency - endpoint.cacheLatencies.getOrDefault(cacheNo,0) * request.requestsAmount;
    }

    public void generateOutput(Map<Integer, List<Integer>> outputMap) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(FILE_NAME + ".out");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(outputMap.size()); //How many cache servers we are using

            outputMap.entrySet().forEach(x ->
            {
                printWriter.print(x.getKey() + " ");
                if(x.getValue() != null) {
                    x.getValue().forEach(y -> printWriter.print(y + " "));
                }
                printWriter.print("\n");
            });

            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
