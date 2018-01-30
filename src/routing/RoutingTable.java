package routing;

import java.util.ArrayList;
import java.util.List;

public class RoutingTable {
	
	// TODO: Remove HEARTBEAT_PORT and VOTING_PORT because they are now env vars
	public final int HEARTBEAT_PORT = 8080;
	public final int VOTING_PORT = 8081;
	private List<String> table;
	
	public RoutingTable() {
		this.table = new ArrayList<String>();
	}
	
	public List<String> getTable() {
		return this.table;
	}
	
	public void addEntry(String ip) {
		this.table.add(ip);
	}

}
