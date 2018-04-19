package test;

import ledger.Ledger;
import ledger.Log;
import routing.Route;
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

        Route local_route = new Route();
        local_route.setIP("127.0.0.1");
        local_route.setId(1);
        local_route.setHeartBeatPort(8081);
        local_route.setVotingPort(8082);

        HostInfo follower = new HostInfo(local_route);

        // Create the follower callable
        Callable<Void> f_callable = new FollowerListenHeartBeat(f_ledger,follower, 10);

        // Initialize leader RPC Objects
        Ledger l_ledger = new Ledger();
        Log log = new Log(0,0,"test_key","test_value");
        l_ledger.addToQueue(log);
        RoutingTable rt = new RoutingTable();
        rt.addEntry(local_route.getIP(),local_route.getHeartbeatPort(),local_route.getVotingPort());

        // Create the leader callable
        Route leader_route = new Route();
        leader_route.setIP("127.0.0.1");
        leader_route.setId(2);
        leader_route.setHeartBeatPort(8091);
        leader_route.setVotingPort(8092);

        HostInfo leader = new HostInfo(leader_route);
        Callable<Void> l_callable = new LeaderSendHeartBeat(l_ledger, leader, rt);

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
