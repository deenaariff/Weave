package rpc_vote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.Callable;

import info.HostInfo;
import messages.Vote;
import routing.Route;
import routing.RoutingTable;

/**
 * This class implements Callable to implement the logic of requesting Votes from
 * all available nodes in the routingTable. It method, call, will be run when the 
 * Node is in a Candidate state.
 * 
 **/
public class RequestVote implements Callable<Integer> {

	private RoutingTable rt;
	private HostInfo host_info;
	private Integer votes_obtained;

	/**
	 * Constructor of the RequestVote Class
	 * 
	 * @param rt The routing table of the Node
	 * @param host HostInfo of the node
	 */
	public RequestVote (RoutingTable rt, HostInfo host) {
		this.rt = rt;
		this.host_info = host;
		this.votes_obtained = 0;
	}
	
	/**
	 * Method that sends newVote objects to a given node in a cluster
	 *  
	 * @param hostName The host_info IP of the receiving node
	 * @param portNumber The port number the receiving node is listening to vote requests on
	 * @param vote A new Vote object to enable the receiving Node to vote with
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void send (String hostName, int portNumber, Vote vote) throws ConnectException, IOException {
		Socket socket = new Socket(hostName, portNumber);
		final OutputStream outputStream = socket.getOutputStream();
		final ObjectOutputStream output = new ObjectOutputStream(outputStream);
		output.writeObject(vote);
		socket.close();
	}

	/**
	 * This method sends newVote Objects to all nodes in the implementing node's routing table. 
	 * If a majority of these nodes vote for this node, then the node goes on to become a leader node. 
	 * 
	 * @return 1 if Node received majority of nodes, else return 0
	 */
	public Integer call() throws Exception {
		int totalTableLength = this.rt.getTable().size();
		
		// Send New Vote Objects to all nodes in the routing Table.
        System.out.println("[" + this.host_info.getState() + "]: Requesting Votes from " + totalTableLength +  " Hosts");

        int unresponsive_nodes = 0;

		for (Route route : this.rt.getTable()) {
			Vote newVote = new Vote(this.host_info);
			// Add logic to not send to yourself
			if(this.host_info.matchRoute(route) == false) {
			    try {
                    send(route.getIP(), route.getVotingPort(), newVote);
                } catch (ConnectException e) {
                    System.out.println("[" + this.host_info.getState() + "]: Failed to Connect To " + route.getIP() + " at Voting Port " + route.getVotingPort());
                    // do Nothing as non_response will be Handled by listener
			    } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
			} else {
                this.votes_obtained = 1; // vote for yourself
            }

		}

        System.out.println("[" + this.host_info.getState() + "]: Listening for Responses to Vote Requests on port: " + this.host_info.getVotingPort());
		
		// Create a ServerSocket listener to listen to Vote responses
		ServerSocket listener = new ServerSocket(this.host_info.getVotingPort());
        listener.setSoTimeout(1000);

        int messagesReceived = 1; // will not be sending message to self

        // Run until all nodes have responded
        while(true) {
            try {
                Socket socket = listener.accept();
                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final Vote vote = (Vote) inputStream.readObject();
                boolean voteStatus = vote.getVoteStatus();

                // If voteStatus indicates the node has voted for you, the updated number of votesObtained
                messagesReceived += 1;
                if(voteStatus == true) {
                    this.votes_obtained += 1;
                }
                socket.close();
            } catch (SocketTimeoutException s) {
                if(messagesReceived != totalTableLength) {
                    System.out.println("[" + this.host_info.getState() + "]: Failed to Receive Responses From " + (totalTableLength - messagesReceived) + " out of " + totalTableLength + " Nodes in Cluster");
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        listener.close();

        System.out.println("[" + this.host_info.getState() + "]: " + votes_obtained + " votes obtained out of " + totalTableLength +  " hosts");
		
		// Has obtained majority of votes? => if yes then return 1
		return (this.votes_obtained >= totalTableLength/2)? 1 : 0;
	}
	
	/**
	 * Tests
	public static void main(String[] args) {
		
		RoutingTable rt = new RoutingTable();
		rt.addEntry("127.0.0.1");
		
		HostInfo tmp_host = new HostInfo("127.0.0.1",null,8080);
		
		Callable<Void> listener = new ListenVote(rt.VOTING_PORT);
		
		Callable<Integer> callable = new RequestVote(rt,tmp_host);
		ExecutorService exec = Executors.newFixedThreadPool(3);
		
		exec.submit(listener);
	    Future<Integer> future = exec.submit(callable);
	    
	    try {
		    int output = future.get();
		    System.out.println("RequestVote returned: " + output);
		} catch (Exception e) {
			// The exception will be printed out
			System.out.println("Exception: " + e);
		}
	     
	}
	 */

}