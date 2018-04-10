package test;

import ledger.Ledger;
import ledger.Log;
import org.junit.jupiter.api.Test;
import routing.RoutingTable;
import rpc_heartbeat.FollowerListenHeartBeat;
import rpc_heartbeat.LeaderSendHeartBeat;
import info.HostInfo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRPCHeartbeat {

    private final String local = "127.0.0.1";

    @Test
    public void testHeartBeats() {

        // Initialize Follower RPC Objects
        Ledger f_ledger = new Ledger();
        HostInfo follower = new HostInfo(local);

        // Create the follower callable
        Callable<Void> f_callable = new FollowerListenHeartBeat(f_ledger,follower.getHeartBeatPort(), 10);

        // Initialize leader RPC Objects
        Ledger l_ledger = new Ledger();
        Log log = new Log(0,0,"test_key","test_value");
        l_ledger.addToQueue(log);
        RoutingTable rt = new RoutingTable();
        rt.addEntry(local);

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
