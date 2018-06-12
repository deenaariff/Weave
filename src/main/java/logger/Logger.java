package logger;

import info.HostInfo;

public class Logger {

    private HostInfo host_info;

    public Logger(HostInfo host) {
        this.host_info = host;
    }

    public void log(String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.host_info.getState());
        builder.append(this.host_info.getTerm());
        builder.append(") ");
        builder.append("| ");
        builder.append(System.currentTimeMillis());
        builder.append(" | ");
        builder.append(this.host_info.getEndPointPort());
        builder.append(" ]: ");
        builder.append(message);
        System.out.println(builder.toString());
    }


}
