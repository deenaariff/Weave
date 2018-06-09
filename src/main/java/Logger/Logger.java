package Logger;

import info.HostInfo;

public class Logger {

    private HostInfo host_info;

    public Logger(HostInfo host) {
        this.host_info = host;
    }

    public void log(String message) {
        System.out.println("[" + this.host_info.getState() + " (" + this.host_info.getTerm() + ") " + "| " + System.currentTimeMillis() + " | " + this.host_info.getEndPointPort() + " ]:"  + message);
    }


}
