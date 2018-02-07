package test;

import ledger.Ledger;
import org.junit.jupiter.api.Test;
import routing.RoutingTable;
import rpc_heartbeat.FollowerListenHeartBeat;
import rpc_heartbeat.LeaderSendHeartBeat;
import rpc_heartbeat.RespondHeartBeat;
import rpc_heartbeat.SendHeartBeat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRPCHeartbeat {

    @Test
    public void testRespondHeartBeat() {

        Ledger ledger = new Ledger();

        Callable<Void> callable = new FollowerListenHeartBeat(ledger,8080, 10);
        ExecutorService exec = Executors.newFixedThreadPool(3);
        Future<Void> future = exec.submit(callable);

        try {
            future.get();
        } catch (Exception e) {
            // The exception will be printed out
            System.out.println("Exception: " + e);
        }
    }


    @Test
    public void testSendHeartBeat() {

        Ledger ledger = new Ledger();

        RoutingTable rt = new RoutingTable();
        rt.addEntry("127.0.0.1");

        ExecutorService exec = Executors.newFixedThreadPool(3);

        Callable<Void> callable = new LeaderSendHeartBeat(ledger, rt);
        Future<Void> future = exec.submit(callable);

        try {
            future.get();
        } catch (Exception e) {
            // The exception will be printed out
            System.out.println("Exception: " + e);
        }

    }



}
