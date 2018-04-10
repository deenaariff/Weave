package main;
import configuration.Configuration;
import node.RaftNode;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Main Class to Run a Raft node
 * 
 * @author deenaariff
 */
@SpringBootApplication
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

        Map<String,Integer> ports = config.getPorts();

		RaftNode node = new RaftNode(ports.get("heartbeat"),ports.get("voting"));
		
		System.out.println("Starting Raft Consensus Algorithm");

		// Start the Client Service API
		SpringApplication.run(Application.class, args);
		
		// Run the State Machine 
		while (true) {	
			if (node.getHostInfo().isLeader()) node.runLeader();				
			else if (node.getHostInfo().isCandidate()) node.runCandidate();				
			else if (node.getHostInfo().isFollower()) node.runFollower();								
		}
		
	}

}
