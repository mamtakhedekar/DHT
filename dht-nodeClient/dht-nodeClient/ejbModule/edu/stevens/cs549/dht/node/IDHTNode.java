package edu.stevens.cs549.dht.node;

import javax.ejb.Remote;

import edu.stevens.cs549.dht.representation.NodeInfo;
import edu.stevens.cs549.dht.routing.IRouting.Failed;

/*
 * The part of the DHT business logic that is used in the CLI
 * (the business logic for a command line interface).
 */

@Remote
public interface IDHTNode {
	
	public NodeInfo getNodeInfo() throws Failed;
	
	/*
	 * Adding and deleting content at a node.
	 */
	public String[] get(String k) throws Failed;
	
	public void add (String k, String v) throws Failed;
	
	public String[] delete (String k, String v) throws Failed;
	
	/*
	 * Insert this node into a DHT identified by a node's URI.
	 */
	public void insert(String uri) throws Failed;
	
	/*
	 * Display internal state at the CLI.
	 */
	public StringBuffer display() throws Failed;
	
	public StringBuffer routes() throws Failed;
	
	/*
	 * Simulate failure.
	 */
	public void setFailed();

}
