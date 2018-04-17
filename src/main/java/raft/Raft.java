package raft;
import configuration.Configuration;
import ledger.Ledger;
import node.RaftNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The Main Class to Run a Raft node
 * 
 * @author deenaariff
 */
@SpringBootApplication
public class Raft {

	@Autowired
	private static Ledger ledger;

	public static void main(String[] args) {

	    Configuration config;
		
		/* Create a new Raft Node
        Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream( Paths.get( args[ 0 ] ) ) ) {
            config = yaml.loadAs( in, Configuration.class );
        } catch (Exception e) {
            throw new RuntimeException("Unable to Locate Configuration File");
        }

        Map<String,Integer> ports = config.getPorts(); */

		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		ledger = (Ledger) context.getBean("ledger");

		RaftNode node = new RaftNode(ledger,8080,8081);
		
		System.out.println("Starting Raft Consensus Algorithm");

		// Start the Client Service API
		SpringApplication.run(Raft.class, args);
		
		// Run the State Machine
		while (true) {
			System.out.println(ledger.getUpdates());
			if (node.getHostInfo().isLeader()) node.runLeader();				
			else if (node.getHostInfo().isCandidate()) node.runCandidate();				
			else if (node.getHostInfo().isFollower()) node.runFollower();								
		}
		
	}

}
