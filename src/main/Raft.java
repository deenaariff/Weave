package main;
import configuration.Configuration;
import node.RaftNode;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Main Class to Run a Raft node
 * 
 * @author deenaariff
 */
public class Raft {

	public static void main(String[] args) {

	    Configuration config;
		
		// Create a new Raft Node
        Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream( Paths.get( args[ 0 ] ) ) ) {
            config = yaml.loadAs( in, Configuration.class );
        } catch (Exception e) {
            throw new RuntimeException("Unable to Locate Configuration File");
        }

		RaftNode node = new RaftNode(config.getHeartBeatPort(),config.getVotingPort());
		
		System.out.println("Starting Raft Consensus Algorithm");
		
		// Run the State Machine 
		while (true) {	
			if (node.getHostInfo().isLeader()) node.runLeader();				
			else if (node.getHostInfo().isCandidate()) node.runCandidate();				
			else if (node.getHostInfo().isFollower()) node.runFollower();								
		}
		
	}

}
