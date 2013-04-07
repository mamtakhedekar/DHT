package edu.stevens.cs549.dht.client;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;

import edu.stevens.cs549.dht.node.IDHTNode;

public class Main {
	
	Logger log = Logger.getLogger("edu.stevens.cs549.dht.client.Main");
	
	/*
	 * Main program driving CLI to manage a DHT node.
	 * 
	 * DI performs an implicit COS name service lookup for a 
	 * session bean (with remote interface IDHTNode) in the app server.
	 * 
	 * We assume this CLI client and the app server are on the
	 * same physical machine, so they connect through the local COS
	 * name service, though this assumption could be relaxed.  In
	 * practice, this admin client should be on same local network as the 
	 * application it is controlling.
	 */
	
	public Main() {
		super();
	}

	/*
	 * Use DI to get a CORBA reference to a DHT Bean
	 */
	@EJB(name="ejb/DHTBean")
	public static IDHTNode dht;
		
	protected void run() {
		if (dht == null) {
			log.info("Failed to inject DHT bean");
			try {
				Context ctx = new InitialContext();
				dht = (IDHTNode)ctx.lookup("java:global/ejb/DHTBean");
				log.info("Successfully looked up DHT bean");
			} catch (Exception e) {
				log.severe("Exception while looking up DHT bean");
				e.printStackTrace();
				return;
			}
		} else {
			log.info("Successfully injected DHT bean");
		}
		/*
		 * Start the command-line loop.
		 */
		CliClient client = new CliClient(dht);
		client.cli();		
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}

}