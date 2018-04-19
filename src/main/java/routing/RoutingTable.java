package routing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutingTable {

	private List<Route> table;
	private HashMap<Integer,Route> id_map;
	
	public RoutingTable() {
        this.id_map = new HashMap<Integer, Route>();
		this.table = new ArrayList<Route>();
	}

	public RoutingTable(String configFile) {
	    this.id_map = new HashMap<Integer, Route>();
        this.table = new ArrayList<Route>();
        configToTable(configFile);
    }
	
	public List<Route> getTable() {
		return this.table;
	}

    /**
     * This method returns the number of nodes it would take to specify the
     * majority of the cluster.
     *
     * @return
     */
	public Integer getMajority() {
        return (int) Math.ceil(this.table.size() / 2);
    }
	
	public void addEntry(String ip, int heartbeat_port, int voting_port) {
		this.table.add(new Route(ip, heartbeat_port, voting_port));
	}


    /**
     * Get the Routing information for a given id
     *
     * @param id
     * @return
     */
    public Route getRouteById(Integer id) {
        try {
            return id_map.get(id);
        } catch (Exception e) {
            throw new RuntimeException("Route not available for this id");
        }
    }

    /**
     * Construct the Routing Table Given the Appropriate xml file
     *
     * @param configFile
     * @return
     */
    private void configToTable(String configFile) {

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(configFile).getFile());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("node");

            System.out.println("[Main]: Building Routing Table for " + nList.getLength() +  "nodes" );

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                Route new_route = new Route();

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    Integer id = Integer.parseInt(eElement.getAttribute("id"));

                    new_route.setId(id);
                    new_route.setIP(eElement.getElementsByTagName("ip").item(0).getTextContent());
                    new_route.setEndpointPort(Integer.parseInt(eElement.getElementsByTagName("client").item(0).getTextContent()));
                    new_route.setHeartBeatPort(Integer.parseInt(eElement.getElementsByTagName("heartbeat").item(0).getTextContent()));
                    new_route.setVotingPort(Integer.parseInt(eElement.getElementsByTagName("voting").item(0).getTextContent()));

                    this.id_map.put(id,new_route);

                }

                this.table.add(new_route);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}



