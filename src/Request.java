/**
 * @author Kamil Klosowski
 * @project StreamingVideo
 * @since 14/02/2018
 */
public class Request {

    public int videoNumber;
    public int requestsAmount;
    public int requestingEndpoint;
    public int videoSize;
    public int requestSize;

    public Request(int videoNumber, int requestsAmount, int requestingEndpoint, int videoSize) {
        this.videoNumber = videoNumber;
        this.requestsAmount = requestsAmount;
        this.requestingEndpoint = requestingEndpoint;
        this.videoSize = videoSize;
        this.requestSize = videoSize * requestsAmount;
    }

    public int getVideoNumber() {
        return videoNumber;
    }
}
