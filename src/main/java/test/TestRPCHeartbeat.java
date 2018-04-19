package test;

import ledger.Ledger;
import ledger.Log;
import routing.RoutingTable;
import rpc_heartbeat.FollowerListenHeartBeat;
import rpc_heartbeat.LeaderSendHeartBeat;
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
     * This method runs both the leader and the follower locally. The follower
     * is placed into the routing table as local host. Then the executor service
     * runs both the leader and the follower on separate threads.
     */
    public void testHeartBeats() {

        // Initialize Follower RPC Objects
        Ledger f_ledger = new Ledger();
        HostInfo follower = new HostInfo(local);

        // Create the follower callable
        Callable<Void> f_callable = new FollowerListenHeartBeat(f_ledger,follower, 10);

        // Initialize leader RPC Objects
        Ledger l_ledger = new Ledger();
        Log log = new Log(0,0,"test_key","test_value");
        l_ledger.addToQueue(log);
        RoutingTable rt = new RoutingTable();
        rt.addEntry("127.0.0.1",8081,8082);

        // Create the leader callable
        Callable<Void> l_callable = new LeaderSendHeartBeat(l_ledger, rt);

        ExecutorService exec = Executors.newFixedThreadPool(3);
        Future<Void> f_future = exec.submit(f_callable);
        Future<Void> l_future = exec.submit(l_callable);

        try {
            f_future.get();
        } catch (Exception e) {
            // The exception will be printed out
            System.out.println("Exception: " + e);
        }
    }
}
