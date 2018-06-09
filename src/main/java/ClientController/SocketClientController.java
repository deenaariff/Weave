package ClientController;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import rpc.JsonUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class SocketClientController {

    public HostInfo host_info;
    private Ledger ledger;

    public SocketClientController(HostInfo host, Ledger ledger) {
        this.host_info = host;
        this.ledger = ledger;
    }

    /**
     * Handle a a Client Message Over a Socket
     *
     * @param content
     * @return
     * @throws IOException
     */
    public String processResponse(String content) throws IOException {

        String response = null;

        if (content != null) {

            JsonReader reader = new JsonReader(new StringReader(content));
            reader.setLenient(true);
            Map<String, Object> clientMessage = new Gson().fromJson(reader, Map.class);

            host_info.getLogger().log("CLIENT MESSAGE : " + content);

            String cmd = (String) clientMessage.get("cmd");
            String id = (String) clientMessage.get("id");
            HashMap<String, Object> rsp = new HashMap<>();

            rsp.put("cmd",cmd);
            rsp.put("id",id);

            if (cmd.equals("get")) {
                Map<String, String> keyStore = this.ledger.getKeyStore();

                String key = (String) clientMessage.get("var");
                if (keyStore.containsKey(key)) {
                    String value = keyStore.get(key);
                    rsp.put("val", value);
                    rsp.put("valid", Boolean.TRUE);
                } else {
                    rsp.put("valid", Boolean.FALSE);
                }

            } else if (cmd.equals("set")) {
                String key = (String) clientMessage.get("var");
                String value = (String) clientMessage.get("val");
                Log update = new Log(this.host_info.getTerm(), ledger.getLastApplied() + 1, key, value);
                ledger.addToLogs(update);

                rsp.put("val", value);
                rsp.put("valid", Boolean.TRUE);

            } else if (cmd.equals("kill")) {
                ledger = new Ledger();
                host_info.restart();
                rsp.put("response", host_info.getEndPointPort() + " has been restarted");
            }

            response = JsonUtil.toJson(rsp) + "\n";

        }

        return response;

    }
}
