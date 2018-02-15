import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kamil Klosowski
 * @project StreamingVideo
 * @since 14/02/2018
 */
public class Endpoint {
    public int number;
    public int dcLatency;
    public Map<Integer, Integer> cacheLatencies;
    public List<Request> requests;

    public Endpoint(int number, int dcLatency) {
        this.number = number;
        this.dcLatency = dcLatency;
        this.cacheLatencies = new HashMap<>();
        this.requests = new ArrayList<>();
    }

    public void addEpLatency(int ep, int lat) {
        cacheLatencies.put(ep, lat);
    }

    public void addRequest(Request request){
        requests.add(request);
    }

}
