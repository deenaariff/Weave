package routing;

public class Route {

    private String IP;
    private int endpoint_port;
    private int heartbeat_port;
    private int voting_port;

    public Route() {
    }

    public Route(String ip, int heartbeat_port, int voting_port) {
        this.IP = ip;
        this.heartbeat_port = heartbeat_port;
        this.voting_port = voting_port;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getEndpointPort() {
        return endpoint_port;
    }

    public void setEndpointPort(int endpoint_port) {
        this.endpoint_port = endpoint_port;
    }

    public void setHeartBeatPort(int heartbeat_port) {
        this.heartbeat_port = heartbeat_port;
    }

    public int getHeartbeatPort() {
        return heartbeat_port;
    }

    public int getVotingPort() {
        return voting_port;
    }

    public void setVotingPort(int voting_port) {
        this.voting_port = voting_port;
    }

}
