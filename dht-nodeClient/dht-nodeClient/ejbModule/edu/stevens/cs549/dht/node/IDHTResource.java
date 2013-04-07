package edu.stevens.cs549.dht.node;

import javax.ejb.Local;

import edu.stevens.cs549.dht.representation.NodeInfo;
import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.routing.IRouting.Failed;


/*
 * The part of the DHT business logic that is used in the 
 * business logic for a node resource).
 */

@Local
public interface IDHTResource {
	
	public NodeInfo getNodeInfo() throws Failed;
	
	public NodeInfo getSucc() throws Failed;
	
	public NodeInfo getPred() throws Failed;
	
	/*
	 * Exposing the finger table in the node API.
	 * Alternatively search for predecessor tail-recursively,
	 * but be sure to redirect TCP connection to the next node.
	 */
	public NodeInfo closestPrecedingFinger (long k) throws Failed;
	
	/*
	 * When a node is inserted, it takes a range of key-value pairs from
	 * its successor.  This operation returns that range (all bindings 
	 * at the successor up to and including the key of the new predecessor).
	 * As a side-effect, the bindings are removed from the successor node.
	 */
	public TableDB xferBindings (long key) throws Failed;
	
	
	public String[] getLocalKeyVals(String k) throws Failed;
	
	public void notify(NodeInfo nodeInfo) throws Failed;

	public String[] get(String k) throws Failed;
	
	public void add(String key, String val) throws Failed;
	
	public String[] delete(String k, String v) throws Failed;
	
	public void deleteAll(String k) throws Failed;
	
}
