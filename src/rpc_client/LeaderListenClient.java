package rpc_client;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * This class needs to be run by the Leader to listen to incoming client messages
 *
 * @author thomasnguyen
 */
public class LeaderListenClient implements Callable<Void> {

    private int port;

    /**
     * The call method for the class
     */
    @Override
    public Void call() throws IOException, ClassNotFoundException {
        ServerSocket listener = new ServerSocket(this.port);
        boolean voted = false;
        try {
            while (true) {
                Socket socket = listener.accept();
                try {

                    final InputStream yourInputStream = socket.getInputStream();
                    final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);

//                    Vote vote = (Vote) inputStream.readObject();
//                    if(voted == false)  {
//                        System.out.println("Voted for a candidate");
//                        vote.castVote();
//                        voted = true;
//                    } else {
//                        System.out.println("Rejected candidate");
//                    }
//
//                    try {
//                        sendVote(vote);
//                    } catch (Exception e) {
//                        System.out.println(e);
//                    }
                } finally {
                    socket.close();
                }
            }
        } finally {
            listener.close();
        }
        //return null;
    }
}
