package rpc_heartbeat;

import rpc.rpc;
import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HeartbeatListener implements Runnable {

    private HostInfo host_info;
    private Ledger ledger;
    private RoutingTable rt;
    private ServerSocket listener;

    public HeartbeatListener(HostInfo host_info, Ledger ledger, RoutingTable rt) {
        this.host_info = host_info;
        this.ledger = ledger;
        this.rt = rt;
    }

    @Override
    public void run() {

        try {
            this.listener = new ServerSocket(this.host_info.getHeartBeatPort());
            listener.setSoTimeout(this.host_info.getHeartbeatTimeoutInterval());
            System.out.println("[" + this.host_info.getState() + "]: Heartbeat Timeout Interval (" + this.host_info.getHeartbeatTimeoutInterval() + "ms )");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Listen for the heartbeat until the waiting time interval has elapsed
        while (true) {
            try {

                Socket socket = listener.accept();
                System.out.println("[" + this.host_info.getState() + "]: Received a Heartbeat");

                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final HeartBeat hb = (HeartBeat) inputStream.readObject();

                if(this.host_info.isLeader()) {

                    // Heartbeat is acknowledged and is fro me
                    if(hb.isAcknowledged() && this.host_info.matchRoute((hb.getRoute()))) {
                        ledger.receiveConfirmation(hb,this.rt);
                    } else {
                        // Implement Term based handling
                    }

                } else if(this.host_info.isCandidate()) {

                    // Term based handling

                } else if(this.host_info.isFollower()) {

                    if(hb.isAcknowledged()) {
                        // Handle this
                        // This is from a leader
                    } else {
                        // implement checking leader authencity
                        if(true) {
                            hb.setAcknowledged(true);
                            ledger.update(hb);
                            rpc.returnHeartbeat(hb);
                        } else {
                            // Implement if not our leader
                        }
                    }
                }

                socket.close();

            } catch (SocketTimeoutException s) {
                if(this.host_info.isFollower()) {
                    System.out.println("[" + this.host_info.getState() + "]: Interval for Heart Beat Listener Elapsed : (" + this.host_info.getHeartbeatTimeoutInterval() + ")");
                    host_info.becomeCandidate();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }

        try {
            this.listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
