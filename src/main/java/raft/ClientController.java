package raft;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;

import routing.Route;
import routing.RoutingTable;
import rpc.JsonUtil;
import spark.Request;
import spark.Response;

import java.util.HashMap;

import static spark.Spark.*;

public class ClientController {

    private HostInfo host;
    private Ledger ledger;
    private Route route;
    private RoutingTable rt;

    public ClientController(HostInfo host, Ledger ledger, Route route, RoutingTable rt) {
        this.host = host;
        this.ledger = ledger;
        this.route = route;
        this.rt = rt;
    }

    public void listen() {
        // Start embedded server at this port
        port(this.route.getEndpointPort());

        // Main Page
        get("/", (Request request, Response response) -> {
            HashMap<String, Object> rsp = new HashMap<String, Object>();

            rsp.put("IP Address", this.route.getIP());
            rsp.put("Endpoint Port", this.route.getEndpointPort());
            rsp.put("Heartbeat Port", this.route.getHeartbeatPort());
            rsp.put("Voting Port", this.route.getVotingPort());
            rsp.put("State", this.host.getState());
            rsp.put("Last Applied Index", this.ledger.getLastApplied());
            rsp.put("Commit Index", this.ledger.getCommitIndex());
            rsp.put("Term", this.host.getTerm());
            rsp.put("Votes Obtained", this.host.getVotesObtained());

            return JsonUtil.toJson(rsp);
        });

        // Get All routes
        get("/routes", (Request request, Response response) -> {
            HashMap<String, Object> rsp = new HashMap<String, Object>();

            rsp.put("IP Address", this.route.getIP());
            rsp.put("Endpoint Port", this.route.getEndpointPort());
            rsp.put("Routes", this.rt.getTable());
            rsp.put("State", this.host.getState());
            rsp.put("Term", this.host.getTerm());

            return JsonUtil.toJson(rsp);
        });

        // GET - Update the Key Value Store
        get("update/:key/:value", (Request request, Response response) -> {
            HashMap<String, Object> rsp = new HashMap<String, Object>();

            String key = request.params(":key");
            String value = request.params(":value");
            Log update = new Log(host.getTerm(), ledger.getLastApplied() + 1, key, value);
            ledger.addToLogs(update);

            rsp.put("Total Logs", ledger.getLastApplied());
            rsp.put("Key", key);
            rsp.put("Value", value);
            rsp.put("State", this.host.getState());
            rsp.put("Term", this.host.getTerm());

            return JsonUtil.toJson(rsp);
        });

        get("/getKeyStore", (Request request, Response response) -> {
            HashMap<String, Object> rsp = new HashMap<String, Object>();

            rsp.put("Endpoint", this.host.getEndPointPort());
            rsp.put("Term", this.host.getTerm());
            rsp.put("Commit Index", this.ledger.getCommitIndex());
            rsp.put("Data", this.ledger.getKeyStore());

            return JsonUtil.toJson(rsp);
        });
    }

}
