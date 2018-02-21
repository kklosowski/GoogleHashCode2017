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

    //Time saved for each cache for a given request from an endpoint
    public Integer[][] requestTimeSavedPerCache(Request request, Endpoint endpoint) {
        return endpoint.cacheLatencies.entrySet().stream()
                .map(x -> new Integer[]{x.getKey(), Math.abs(x.getValue() - endpoint.dcLatency)})
                .toArray(Integer[][]::new);
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
        Map<Integer, List<int[]>> cacheVideoTimeSaved = new HashMap<>();
        Map<Integer, List<int[]>> timeSavedVideoCache = new HashMap<>();

        for (int i = 0; i < cacheCapacities.length; i++) {
            int finalI = i;
            List<int[]> videoTimeSaved = requestsByVideo.entrySet().stream()
                    .filter(x -> isConnected(finalI, x.getValue().stream()
                            .map(y -> endpoints[y.requestingEndpoint]).collect(Collectors.toList())))
                    .filter(x -> videoFits(finalI, x.getKey()))
                    .filter(x -> !videoAlreadyInCache(finalI, x.getKey()))
                    .map(x -> new int[]{
                            x.getKey(),
                            x.getValue().stream()
                                    .mapToInt(y -> calculateTimeSaved(y, finalI))
                                    .sum()})
                    .sorted(Comparator.comparing(x -> ((int[]) x)[1]).reversed())
                    .collect(Collectors.toList());
            cacheVideoTimeSaved.put(finalI, videoTimeSaved);
        }

        //Create a map with timesaved as a key and List<{cache, video}> pair as a value
        cacheVideoTimeSaved.entrySet().forEach(x -> {
            x.getValue().forEach(y -> {
                timeSavedVideoCache.putIfAbsent(y[1], new ArrayList<>());
                timeSavedVideoCache.get(y[1]).add(new int[]{x.getKey(), y[0]});
            });
        });

        return timeSavedVideoCache;
    }

    private boolean videoAlreadyInCache(int cache, int video) {
        return this.cacheVideoAssignments.get(cache).contains(video);
    }


    public void distributeVideos() {

        Map<Integer, List<int[]>> timeSavedVideoCache = getCacheVideoPairsSortedByTimeSaved();

        //Todo Final iteration over time saved with removing requests until none left
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

//        //Swap places of cache and video and put it in a new  Map<Integer, List<int[]>> videoCacheTimeSaved
//        cacheVideoTimeSaved.entrySet().forEach(x -> {
//            //x.getKey() - cache number
//            x.getValue().forEach(y -> {
//                videoCacheTimeSaved.get(y[0]).add(new int[]{x.getKey(), y[1]});
//            });
//        });
//
//
//        //Distribute using a greedy solution iterating over each cache and filling it sequentially
//        cacheVideoTimeSaved.entrySet().forEach(x -> {
//            while (x.getValue().stream().anyMatch(y -> videoFits(x.getKey(), y[0]))) {
//                List<int[]> copy = new ArrayList<>(x.getValue());
//                copy.stream()
//                        .filter(y -> videoFits(x.getKey(), y[0]))
//                        .forEach(y -> {
//                            putVideoInCache(x.getKey(), y[0]);
//                            outputCacheFirst.get(x.getKey()).add(y[0]);
//                            x.getValue().remove(y);
//                        });
//            }
//        });
    }

    public List<Integer> endpointsRequestingVideo(int videoNumber) {
        return requests.stream()
                .filter(x -> x.videoNumber == videoNumber)
                .map(x -> x.requestingEndpoint)
                .collect(Collectors.toList());
    }

    public List<Integer> endpointsConnectedToCache(List<Integer> endpointNumbers, int cacheNumber) {
        return Arrays.stream(endpoints)
                .filter(x -> x.cacheLatencies.keySet().contains(cacheNumber))
                .map(x -> x.number)
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

    //Best caches sorted by the time saved to cache descending
    public List<Integer[]> bestCachesPerRequest(Request request, Endpoint endpoint) {
        return Arrays.stream(requestTimeSavedPerCache(request, endpoint))
                .sorted(Comparator.comparing(x -> ((int[]) x)[1]).reversed())
                .collect(Collectors.toList());
    }

    public boolean isConnected(int cacheNo, List<Endpoint> endpoints) {
        return 0 < endpoints.stream().filter(x -> x.cacheLatencies.containsKey(cacheNo)).count();
    }

    public int calculateTimeSaved(Request request, int cacheNo) {
        Endpoint endpoint = endpoints[request.requestingEndpoint];
        return endpoint.dcLatency - endpoint.cacheLatencies.getOrDefault(cacheNo, 0) * request.requestsAmount;
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
