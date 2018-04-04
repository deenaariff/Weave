package configuration;

import java.util.Map;

public class Configuration {

    private Map<String,Integer> ports;

    public Integer getHeartBeatPort() {
        return ports.get("heartbeat");
    }

    public Integer getVotingPort() {
        return ports.get("voting");
    }

}
