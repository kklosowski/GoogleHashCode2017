import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
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
    int[] cacheCapacities;
    Map<Integer, List<Integer>> cacheVideoAssignments;
    List<Request> requests;
    Integer latencies[][];

    public Solution(List<String> raw, String filename) {
        this.FILE_NAME = filename;
        this.raw = raw;
        params = Arrays.stream(raw.get(0).split(" ")).mapToInt(Integer::valueOf).toArray();
        videoSizes = Arrays.stream(raw.get(1).split(" ")).mapToInt(Integer::valueOf).toArray();
        endpoints = new Endpoint[params[1]];
        requestNo = params[2];
        cacheCapacities = IntStream.generate(() -> params[4]).limit(params[3]).toArray();
        this.cacheVideoAssignments = new HashMap<>();

        //Init empty caches
        for (int i = 0; i < cacheCapacities.length; i++) {
            cacheVideoAssignments.put(i, new ArrayList<>());
        }

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
                .collect(Collectors.toList());

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
        requests.forEach(x -> endpoints[x.requestingEndpoint].addRequest(x));
    }

    public void solve() {
        distributeVideos();
        generateOutput();
        System.out.println(FILE_NAME);
        System.out.println(Arrays.toString(cacheCapacities));
    }

    /**
     * Checks if a video fits in a cache
     *
     * @param cache - a particular cache
     * @param video - a particular video
     * @return
     */
    public boolean videoFits(int cache, int video) {
        return this.cacheCapacities[cache] >= videoSizes[video];
    }


    //This method is based on the state of leftover requests and capacity of the caches
    Map<Integer, List<int[]>> getCacheVideoPairsSortedByTimeSaved() {

        //For each video get all the requests
        Map<Integer, List<Request>> requestsByVideo = requests.stream().collect(Collectors.groupingBy(Request::getVideoNumber));
        Map<Integer, List<int[]>> timeSavedCacheVideo = new HashMap<>();

        for (int i = 0; i < cacheCapacities.length; i++) {
            int finalI = i;
            requestsByVideo.entrySet().stream()
                    .filter(x -> videoFits(finalI, x.getKey()))
                    .filter(x -> !videoAlreadyInCache(finalI, x.getKey()))
//                    .filter(x -> isConnected(finalI, x.getValue().stream()
//                            .map(y -> endpoints[y.requestingEndpoint]).collect(Collectors.toList())))
                    .map(x -> new int[]{
                            x.getKey(),
                            x.getValue().stream()
                                    .mapToInt(y -> calculateTimeSaved(y, finalI))
                                    .sum()})
                    .forEach(x -> {
                        timeSavedCacheVideo.putIfAbsent(x[1], new ArrayList<>());
                        timeSavedCacheVideo.get(x[1]).add(new int[]{finalI, x[0]});
                    });
        }

        return timeSavedCacheVideo;
    }

    private boolean videoAlreadyInCache(int cache, int video) {
        return this.cacheVideoAssignments.get(cache).contains(video);
    }


    public void distributeVideos() {

        Map<Integer, List<int[]>> timeSavedVideoCache = getCacheVideoPairsSortedByTimeSaved();

        int counter = 1;
        long lastTime = System.currentTimeMillis();
        while (timeSavedVideoCache.size() > 0) {
            int[] nextToCache = timeSavedVideoCache.entrySet().stream()
                    .sorted(Map.Entry.<Integer, List<int[]>>comparingByKey().reversed())
                    .findFirst().get().getValue().get(0);
            putVideoInCache(nextToCache[0], nextToCache[1]);
            //Recalculate
            timeSavedVideoCache = getCacheVideoPairsSortedByTimeSaved();
            long now = System.currentTimeMillis();
            System.out.println("Calculated: " + counter++ +  "   Duration= " + String.valueOf( Math.abs(lastTime - now)) + "   Requests= " + requests.size());
            lastTime = now;
        }

    }

    public List<Integer> endpointsRequestingVideo(int videoNumber) {
        return requests.stream()
                .filter(x -> x.videoNumber == videoNumber)
                .map(x -> x.requestingEndpoint)
                .collect(Collectors.toList());
    }

    public List<Integer> endpointsConnectedToCache(List<Integer> endpointNumbers, int cacheNumber) {
        return endpointNumbers.stream()
                .filter(x -> endpoints[x].cacheLatencies.keySet().contains(cacheNumber))
                .collect(Collectors.toList());
    }

    public void removeServedRequests(int cacheNumber, int videoNumber) {
        List<Integer> endpointsToRemove = endpointsConnectedToCache(endpointsRequestingVideo(videoNumber), cacheNumber);
        List<Request> copy = new ArrayList<>(this.requests);
        endpointsToRemove.forEach(x -> {
            copy.stream()
                    .filter(y -> y.requestingEndpoint == x && y.videoNumber == videoNumber)
                    .forEach(y -> this.requests.remove(y));
        });

    }

    public void putVideoInCache(int cacheNumber, int videoNumber) {
        this.cacheCapacities[cacheNumber] -= this.videoSizes[videoNumber];
        this.cacheVideoAssignments.get(cacheNumber).add(videoNumber);
        removeServedRequests(cacheNumber, videoNumber);
    }


    public boolean isConnected(int cacheNo, List<Endpoint> endpoints) {
        return 0 < endpoints.stream().filter(x -> x.cacheLatencies.containsKey(cacheNo)).count();
    }

    public int calculateTimeSaved(Request request, int cacheNo) {
        Endpoint endpoint = endpoints[request.requestingEndpoint];
        int cacheLatency = endpoint.cacheLatencies.getOrDefault(cacheNo, 0);
        if (cacheLatency == 0){
            return 0;
        } else {
            return (endpoint.dcLatency - cacheLatency) * request.requestsAmount;
        }
    }

    public void generateOutput() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(FILE_NAME + ".out");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(this.cacheVideoAssignments.size()); //How many cache servers we are using

            this.cacheVideoAssignments.entrySet().forEach(x ->
            {
                printWriter.print(x.getKey() + " ");
                if (x.getValue() != null) {
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
