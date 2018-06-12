package client_controller;

import info.HostInfo;
import ledger.Ledger;
import rpc.rpc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncSocketClientController extends SocketClientController {

    private ServerSocket listener;

    public SyncSocketClientController(HostInfo host, Ledger ledger) {
        super(host,ledger);
    }

    public void listen() {

        try {
            this.listener = new ServerSocket(super.host_info.getEndPointPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) { // Listen for Client Requests);
            try {

                Socket socket = listener.accept(); // accept incoming mesages
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String content = br.readLine();
                String response = super.processResponse(content);

                if(response != null) {
                    rpc.sendClientResponse(socket,response);
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
