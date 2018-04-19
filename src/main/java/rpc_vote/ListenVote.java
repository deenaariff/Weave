package rpc_vote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import messages.Vote;

/**
 * This class implements Callable to implement the logic of listening for
 * any voting requests from candidates during a leader election. This will vote for the
 * first candidate message received if in an open position. Otherwise it will reject all other
 * votes.
 * 
 **/
public class ListenVote implements Callable<Void> {
	
	private int port;
	
	/**
	 * The Constructor for the ListenVote class
	 */
	public ListenVote(int port) {
		this.port = port;
	}

	/**
	 * Send a vote back to the original sender of the Vote Object
	 *
	 * @param vote The Vote object which should be returned to the sender of the vote
	 */
	private void sendVote (Vote vote) throws UnknownHostException, IOException {
		Socket socket = new Socket(vote.getHost(),vote.getHostPort());
		final OutputStream outputStream = socket.getOutputStream();
		final ObjectOutputStream output = new ObjectOutputStream(outputStream);
		output.writeObject(vote);
		socket.close();
	}
	
	/**
	 * The call method for the class
	 */
	public Void call() throws IOException, ClassNotFoundException {
		ServerSocket listener = new ServerSocket(this.port);
		boolean voted = false;
		try {
	    	while (true) {
		    	Socket socket = listener.accept();
		        try {
		        	
		            final InputStream yourInputStream = socket.getInputStream();
		            final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
		            
		            Vote vote = (Vote) inputStream.readObject();  
		            if(voted == false)  {
		            	System.out.println("Voted for a candidate");
		            	vote.castVote();
			            voted = true;
		            } else {
		            	System.out.println("Rejected candidate");
		            }
		        
		            try {
		            	sendVote(vote);
		            } catch (Exception e) {
		            	System.out.println(e);
		            }
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
