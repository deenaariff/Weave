package test;

/*
import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import routing.Route;
import state_helpers.Follower;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;*/


public class TestFollowerHandlers {

    /*private HostInfo mock_leader_info;
    private HostInfo follower_info;

    /**
     * Constructor to initalize utilized values
     *
    public TestFollowerHandlers() {
        Route mock_leader_route = new Route();
        mock_leader_route.setIP("127.0.0.1");
        mock_leader_route.setIP("8080");
        this.mock_leader_info = new HostInfo(mock_leader_route);

        this.follower_info = new HostInfo(new Route());
    }

    /**
     * Creates a Socket and returns a Heartbeat received over it
     *
     * @param hb
     * @param ledger
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
    private HeartBeat receive_heartbeat_over_socket(HeartBeat hb, Ledger ledger) throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(8080);
        Follower.HandleHeartBeat(hb, ledger,follower_info);
        Socket socket = ss.accept();

        final InputStream yourInputStream = socket.getInputStream();
        final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
        return (HeartBeat) inputStream.readObject();
    }

    @Test
    /**
     * Tests Follower Response to AppendEntries RPC with lower term
     *

    public void test_HandleHeartBeat_with_greater_term_than_leader() {
        HeartBeat hb = new HeartBeat(this.mock_leader_info, new ArrayList<>(), new Route(), 0, null, 0);
        this.follower_info.setTerm(1);
        try {
            HeartBeat received_hb = receive_heartbeat_over_socket(hb, new Ledger());
            Assertions.assertEquals(received_hb.getReply(), Boolean.FALSE);
        } catch (Exception e) {
            Assertions.fail("Test failed : " + e.getMessage());
        }
    }

    @Test
    /**
     * Tests Follower Response to AppendEntries RPC with valid term
     *

    public void test_handleHeartBeat_with_valid_term_from_leader() {
        HeartBeat hb = new HeartBeat(this.mock_leader_info, new ArrayList<>(), new Route(), 0, null, 0);
        this.follower_info.setTerm(0);
        try {
            HeartBeat received_hb = receive_heartbeat_over_socket(hb, new Ledger());
            Assertions.assertEquals(received_hb.getReply(), Boolean.TRUE);
        } catch (Exception e) {
            Assertions.fail("Test failed : " + e.getMessage());
        }
    }

    @Test
    /**
     * This Tests a Follower receiving an AppendEntries RPC where PrevLogIndex & PrevLogTerm Matches Logs
     * for index > 0
     *
    public void test_handleHeartBeat_with_prevLog_term_match_greater_than_0() {
        HeartBeat hb = new HeartBeat(this.mock_leader_info, new ArrayList<>(), new Route(), 1, new Log(1,0,"prev_test","prev_test"), 1);
        this.follower_info.setTerm(0);

        Ledger ledger = new Ledger();
        ledger.addToLogs(new Log(1,0,"prev_test","prev_test"));

        try {
            HeartBeat received_hb = receive_heartbeat_over_socket(hb, ledger);
            Assertions.assertEquals(received_hb.getReply(), Boolean.TRUE);
        } catch (Exception e) {
            Assertions.fail("Test failed : " + e.getMessage());
        }
    }

    @Test
    /**
     * This Tests a Follower receiving an AppendEntries RPC where PrevLogIndex & PrevLogTerm does not match Logs
     * for index > 0
     *
    public void test_handleHeartBeat_with_prevLog_term_not_match_greater_than_0() {
        HeartBeat hb = new HeartBeat(this.mock_leader_info, new ArrayList<>(), new Route(), 1, new Log(1,0,"prev_test","prev_test"), 1);
        this.follower_info.setTerm(0);

        Ledger ledger = new Ledger();
        ledger.addToLogs(new Log(1,0,"not_match","not_match"));

        try {
            HeartBeat received_hb = receive_heartbeat_over_socket(hb, ledger);
            Assertions.assertEquals(received_hb.getReply(), Boolean.FALSE);
        } catch (Exception e) {
            Assertions.fail("Test failed : " + e.getMessage());
        }
    }
    */

}