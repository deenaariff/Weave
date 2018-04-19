package routing;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoutingTable {

	private List<Route> table;
	
	public RoutingTable() {
		this.table = new ArrayList<Route>();
	}

	public RoutingTable(String configFile) {
        this.table = configToTable(configFile);
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
	    // TODO: This cannot be called ... deprecate this method?
        return (int) Math.ceil(this.table.size() / 2);
    }
	
	public void addEntry(String ip, int heartbeat_port, int voting_port) {
		this.table.add(new Route(ip, heartbeat_port, voting_port));
	}

    /**
     * Construct the Routing Table Given the Appropriate xml file
     *
     * @param configFile
     * @return
     */
    private List<Route> configToTable(String configFile) {
        List<Route> routes = new ArrayList<Route>();
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // read the XML document
            Route route = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have an item element, we create a new item
                    if (startElement.getName().getLocalPart().equals(Route)) {
                        route = new Route();
                        // We read the attributes from this tag and add the date
                        // attribute to our object
                        Iterator<Attribute> attributes = startElement
                                .getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(DATE)) {
                                route.setDate(attribute.getValue());
                            }

                        }
                    }
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(MODE)) {
                            event = eventReader.nextEvent();
                            item.setMode(event.asCharacters().getData());
                            continue;
                        }
                    }
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(UNIT)) {
                        event = eventReader.nextEvent();
                        item.setUnit(event.asCharacters().getData());
                        continue;
                    }
                }
                // If we reach the end of an item element, we add it to the list
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(ITEM)) {
                        routes.add(route);
                    }
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return routes;
    }

}


        }
