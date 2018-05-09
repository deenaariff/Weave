package raft;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;

import routing.Route;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

public class ClientController {

    private HostInfo host;
    private Ledger ledger;
    private Route route;

    public ClientController(HostInfo host, Ledger ledger, Route route) {
        this.host = host;
        this.ledger = ledger;
        this.route = route;
    }

    public void listen() {
        // Start embedded server at this port
        port(this.route.getEndpointPort());

        // Main Page
        get("/", (Request request, Response response) -> {
            String info = this.route.getIP() + ":" + this.route.getEndpointPort();
            return "[" + host.getState() + "]: " + info;
        });

        // GET - Update the Key Value Store
        get("update/:key/:value", (Request request, Response response) -> {
            String key = request.params(":key");
            String value = request.params(":value");
            Log update = new Log(host.getTerm(),ledger.getLastApplied()+1,key,value);
            ledger.addToLogs(update);
            return "[" + host.getState() + "]: Logs in Ledger: " + ledger.getLastApplied();
        });
    }


}
