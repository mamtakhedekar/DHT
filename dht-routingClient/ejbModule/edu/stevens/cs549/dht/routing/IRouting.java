package edu.stevens.cs549.dht.routing;

import edu.stevens.cs549.dht.representation.NodeInfo;

/*
 * Interface for the RMI server that exposes the state of the routing tables.
 * ONLY to be invoked by the local business logic of the DHT node.
 */

public interface IRouting {

	public NodeInfo getNodeInfo();
	
	public static class Failed extends Exception {
		private static final long serialVersionUID = 1L; 
	}
	
	public void setFailed();
	public void checkFailed() throws Failed;

	public static final int NFINGERS = 6;
	
	public static final int NKEYS = 64;
	
	public NodeInfo getPred() throws Failed;
	
	public void setPred(NodeInfo pred) throws Failed;
	
	public NodeInfo getSucc() throws Failed;
	
	public void setSucc(NodeInfo succ) throws Failed;
	
	public NodeInfo setFinger (int i, NodeInfo info) throws Failed;
	
	public NodeInfo getFinger (int i) throws Failed;
	
	public NodeInfo closestPrecedingFinger (long k) throws Failed;
	
	public StringBuffer routes();

}
