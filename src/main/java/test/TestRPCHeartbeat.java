package test;

import ledger.Ledger;
import ledger.Log;
import routing.Route;
import routing.RoutingTable;
import info.HostInfo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/**
 * This class is used to test the communication of RPC heartbeats between
 * leaders and followers.
 */
public class TestRPCHeartbeat {

    private final String local = "127.0.0.1";

    /**
     * This method runs both the leader and the rpc locally. The rpc
     * is placed into the routing table as local host. Then the executor service
     * runs both the leader and the rpc on separate threads.
     */
    public void testHeartBeats() {

    }
}
