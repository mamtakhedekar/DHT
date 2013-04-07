package edu.stevens.cs549.dht.node;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.xml.ws.WebServiceException;

import com.sun.jersey.api.client.Client;

import edu.stevens.cs549.dht.representation.NodeInfo;
import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.representation.TableRec;
import edu.stevens.cs549.dht.rest.WebClient;
import edu.stevens.cs549.dht.routing.IRouting;
import edu.stevens.cs549.dht.routing.IRouting.Failed;
import edu.stevens.cs549.dht.routing.IRoutingLocal;
import edu.stevens.cs549.dht.state.IStateLocal;

/**
 * Session Bean implementation class DHT
 */
@Stateless(name="DHTBean", mappedName="ejb/DHT")
public class DHT implements IDHTNode, IDHTResource {

	/*
	 * DHT logic.
	 * 
	 * This logic may be invoked from a RESTful Web service or from the command
	 * line, as reflected by the two interfaces.
	 */
	protected Client client;
	
	protected Logger log = Logger.getLogger("edu.stevens.cs549.dht.node.DHT");

	protected void info(String s) {
		log.info(s);
	}

	protected void severe(String s) {
		log.severe(s);
	}

	/*
	 * Key-value pairs stored in this node (in a database server).
	 */
	@EJB(beanName = "StateBean")
	private IStateLocal state;

	/*
	 * Finger tables, and predecessor and successor pointers, are also stored in
	 * a local RMI server, to be retrieved by the business logic for a Web
	 * service.
	 */
	@EJB(beanName = "RoutingBean")
	private IRoutingLocal routing;

	@Resource(name="NumFingerStabs")
	private String numFingerStabs;
	protected int ntimes;

	/*
	 * The URL for this node in the DHT (cached in DHT bean).
	 */
	private NodeInfo info;

	@PostConstruct
	protected void init() {
		this.info = routing.getNodeInfo();
		this.ntimes = Integer.parseInt(numFingerStabs);
		this.client = Client.create();
	}

	public NodeInfo getNodeInfo() throws Failed {
		return this.info;
	}

	/**
	 * Default constructor.
	 */
	public DHT() {
	}
	
	/*
	 * Get the successor of a node. Need to perform a Web service call to that
	 * node, and it then retrieves its routing state from its local RMI object.
	 * 
	 * Make a special case for when this is the local node, i.e.,
	 * info.addr.equals(localInfo.addr), otherwise get an infinite loop.
	 */
	private NodeInfo getSucc(NodeInfo succNode) {
		NodeInfo succ = null;
		if ( info.addr.equals(succNode.addr))
		{
			try {
				succ =  getSucc();
			} catch (Failed e) {
				e.printStackTrace();
			}
		}
		else
		{
			succ = WebClient.getRemoteSuccForNode(client, succNode);
		}
		
		return succ;
	}

	/*
	 * This version gets the local successor from RMI server.
	 */
	public NodeInfo getSucc() throws Failed {
		return routing.getSucc();
	}

	/*
	 * Get the predecessor of a node. Need to perform a Web service call to that
	 * node, and it then retrieves its routing state from its local RMI object.
	 * 
	 * Make a special case for when this is the local node, i.e.,
	 * info.addr.equals(localInfo.addr), otherwise get an infinite loop.
	 */
	protected NodeInfo getPred(NodeInfo predNode) throws Failed {
		if ( info.addr.equals(predNode.addr))
		{
			return getPred();
		}
		else
		{
			return WebClient.getRemotePredForNode(client, predNode);
		}
	}

	/*
	 * This version gets the local predecessor from RMI server.
	 */
	public NodeInfo getPred() throws Failed {
		return routing.getPred();
	}

	/*
	 * Perform a Web service call to get the closest preceding finger in the
	 * finger table of the argument node.
	 */
	protected NodeInfo closestPrecedingFinger(NodeInfo info, long k)
			throws Failed {
		return WebClient.getRemoteClosestPreceedingFingerForNode(client, info, k);
	}

	/*
	 * For the local version, call the local routing bean.
	 */
	public NodeInfo closestPrecedingFinger(long k) throws Failed {
		return routing.closestPrecedingFinger(k);
	}

	/*
	 * Get all bindings at the specified node up to key k.
	 */
	protected TableDB xferBindings(NodeInfo info, long key) throws Failed {
		TableDB db = new TableDB();
		db.entry = new TableRec[0]; 
		String[] binding = null;
		for ( int i = (int) ((this.info.key + 1 ) % IRouting.NKEYS);
				i <= key; i++ )
		{
			binding = WebClient.getRemoteXferKeyBinding(client, info, (new Integer(i)).toString());

			if ( binding != null )
			{
				info("xfer bindings for key="+i+" from old succ="+info.key);
				db.AddEntry((new Integer(i)).toString(), binding );
			}

		}
		
		return db;
	}

	/*
	 * For the local version, call the state bean.
	 */
	public TableDB xferBindings(long k) throws Failed {
		return state.xferBindings(k);
	}

	/*
	 * Find the node that will hold bindings for a key k. Search the circular
	 * list to find the node. Stop when the node's successor stores values
	 * greater than k.
	 */
	protected NodeInfo findPredecessor(long k) throws Failed {
		NodeInfo info = this.info;
		NodeInfo succ = getSucc(info);
		//while (k < info.key || k > succ.key) {
		while (NodeInfo.isBetweenPredAndSuccInclusive(k,info,succ) == 0) {
			info = closestPrecedingFinger(info, k);
			succ = getSucc(info);
		}
		return info;
	}

	/*
	 * Find the successor of k, starting at the current node.
	 */
	public NodeInfo findSuccessor(long k) throws Failed {
		NodeInfo predInfo = findPredecessor(k);
		return getSucc(predInfo);
	}

	public void stabilize() {
		NodeInfo successorsPred = null;
		NodeInfo mysuccessor =  null;
		try
		{
			if ( getSucc().key == this.info.key )
			{
				return;
			}

			mysuccessor =  getSucc();
			successorsPred = getPred(mysuccessor);
		
			// if my successor has a known predecessor and if this nodes key is greater than ours
			// this node needs to be our successor so we set it as our successor.
			if ( successorsPred != null && 
				NodeInfo.isBetweenPredAndSucc(successorsPred.key, this.info.key, mysuccessor.key) != 0 )
			{
				info("setting a new found succ= " + successorsPred.key + " mysuccessor =" +mysuccessor.key );
				
				routing.setSucc(successorsPred);
				
				TableDB db = xferBindings(mysuccessor, getSucc().key);
				for ( int i = 0; i < db.entry.length; i++ )
				{
					String[] bindings =	db.entry[i].vals;
					for ( int j = 0; j < bindings.length; j++ )
					{
						info("put binding for key=" +db.entry[i].key+" to node=" + getSucc().key );
						WebClient.putKeyBinding(client, getSucc(), db.entry[i].key, bindings[j]);
					}
				}
			}
		}
		catch (Failed e) {
			e.printStackTrace();
		}
		// now try to tell the new successor that we are its predecessor
		try {
			WebClient.notifyRemoteNode(client, getSucc(), this.info);
		} catch (WebServiceException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
	}

	public void notify(NodeInfo info) {
		try {
			if ( getPred() == null ||  
					( NodeInfo.isBetweenPredAndSucc(info.key, getPred().key, this.info.key) != 0) )
				{
					routing.setPred(info);
				}
		} catch (Failed e) {
			e.printStackTrace();
		}
	}

	private int next = 0;
	private int exp = 1;
	
	public void fixFingers() {
		long findFingerKey = 0;
		if ( next >= IRouting.NFINGERS )
		{
			next = 0;
			exp = 1;
		}
		try {
			findFingerKey = (this.info.key + exp)%IRouting.NKEYS;
			//info("Find finger key ==== " +  findFingerKey);
			NodeInfo succ = findSuccessor(findFingerKey);
			//info( "Found successor for finger=" + next + " succ=" + succ.key );
			routing.setFinger(next, succ );
			next = next + 1;
			exp = exp*2;
		} catch (Failed e) {
			e.printStackTrace();
		}
	}

	public void checkPredecessor() {
		// TODO
		/*
		 * Ping the predecessor node by doing a GET (getNodeInfo()).
		 */
	}

	public void fixFingers(int ntimes) {
		for (int i = 0; i < ntimes; i++) {
			fixFingers();
		}
	}
	
	/*
	 * Use EJB timer service to run background processing.
	 */
	@Schedule(second="5/5", minute="*",hour="*", persistent=false)
	protected void backgroundProcessing() {
		log.info("Performing background stabilization.");
		stabilize();
		fixFingers(ntimes);
	}

	/*
	 * Get the values under a key at the specified node. If the node is the
	 * current one, go to the RMI server.
	 */
	protected String[] get(NodeInfo n, String k) throws Failed {
		if (n.addr.equals(info.addr)) {
			return this.get(k);
		} else {
			return WebClient.getRemoteKeyBinding(client, n,k);
		}
	}

	/*
	 * Logic for finding the node where values for that key are stored.
	 */
	public String[] get(String k) throws Failed {
		try {
			Integer tempKey = new Integer(k.hashCode() % IRouting.NKEYS);
			System.out.println("KEY = " + tempKey.toString());
			
			if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey.intValue(), getPred(), this.info) != 0)
			{
				return state.get(k);
			}
			
			NodeInfo predFromFingerTable = closestPrecedingFinger(Integer.parseInt(tempKey.toString()));
			NodeInfo nodeToGet = null;
			if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey, this.info, predFromFingerTable) != 0 )
			{
				nodeToGet = predFromFingerTable;
			}
			else
			{
				nodeToGet = getSucc();
			}
			
			return get(nodeToGet, k);
			
		} catch (Failed e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Add a value under a key.
	 */
	public void add(NodeInfo n, String k, String v) throws Failed {
		if (n.addr.equals(info.addr)) {
			this.add(k, v);
		} else {
			WebClient.putKeyBinding( client, n, k, v);
		}
	}

	public void add(String k, String v) throws Failed {
		//state.add(k, v);
		Integer tempKey = new Integer(k.hashCode() % IRouting.NKEYS);
		//System.out.println("KEY = " + tempKey.toString());
		
		// check if the key can be added to self
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey.intValue(), getPred(), this.info) != 0)
		{
			state.add(k, v);
			return;
		}
		
		NodeInfo predFromFingerTable = closestPrecedingFinger(Integer.parseInt(tempKey.toString()));
		NodeInfo nodeToAdd = null;
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey, this.info, predFromFingerTable) != 0 )
		{
			nodeToAdd = predFromFingerTable;
		}
		else
		{
			nodeToAdd = getSucc();
		}
		
		add(nodeToAdd, k, v);
	}

	/*
	 * Reset values under a key.
	 */
	public String[] delete(NodeInfo n, String k, String v) throws Failed {
		if (n.addr.equals(info.addr)) {
			return this.delete(k, v);
		} else {
			return WebClient.delKeyBinding(client, n, k, v);
		}
	}

	public String[] delete(String k, String v) throws Failed {
		Integer tempKey = new Integer(k.hashCode() % IRouting.NKEYS);
		//System.out.println("HASHKEY = " + tempKey.toString());		
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey.intValue(), getPred(), this.info) != 0)
		{
			return state.delete(k, v);
		}

		NodeInfo predFromFingerTable = closestPrecedingFinger(Integer.parseInt(tempKey.toString()));
		NodeInfo nodeToDel = null;
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey, this.info, predFromFingerTable) != 0 )
		{
			nodeToDel = predFromFingerTable;
		}
		else
		{
			nodeToDel = getSucc();
		}
		return delete(nodeToDel, k, v);
	}

	/*
	 * Insert this node into the DHT identified by uri.
	 */
	public void insert(String uri) throws Failed {

		/*
		 * 1. Perform a Web service call to get NodeInfo for the DHT node. 2.
		 * Locate new node's successor in the DHT. 3. Transfer the bindings from
		 * the successor now stored in this node. 4. Do a "stabilize", this will
		 * reset the successor's predecessor pointer. The previous predecessor
		 * will learn about the change when it does its own stabilize.
		 */
		try {
			NodeInfo oldSuccessor = getSucc();
			NodeInfo newNodeInfo = WebClient.getRemoteNodeInfo(client, uri);

			if ( getSucc().key == info.key )
			{
				routing.setSucc(newNodeInfo);
			}
			else if ( NodeInfo.isBetweenPredAndSucc(newNodeInfo.key, this.info.key, getSucc().key) != 0)
			{
				routing.setSucc(newNodeInfo);
			}
	
			TableDB db = xferBindings(oldSuccessor, getSucc().key);
			for ( int i = 0; i < db.entry.length; i++ )
			{
				String[] bindings =	db.entry[i].vals;
				for ( int j = 0; j < bindings.length; j++ )
					WebClient.putKeyBinding( client, getSucc(), db.entry[i].key, bindings[j]);
			}

			stabilize();
			} catch (WebServiceException e) 
			{
				e.printStackTrace();
			}
	}

	public StringBuffer display() {
		return state.display();
	}

	public StringBuffer routes() {
		return routing.routes();
	}

	/*
	 * Set the current node to be failed.
	 */
	public void setFailed() {
		routing.setFailed();
	}

	@Override
	public String[] getLocalKeyVals(String k) throws Failed {
			return state.get(k);
	}

	@Override
	public void deleteAll(String k) throws Failed {
		long tempKey = k.hashCode() % IRouting.NKEYS;
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey, getPred(), this.info) != 0)
		{
			state.deleteAll(k);
		}

		NodeInfo predFromFingerTable = closestPrecedingFinger(tempKey);
		NodeInfo nodeToDel = null;
		if ( NodeInfo.isBetweenPredAndSuccInclusive(tempKey, this.info, predFromFingerTable) != 0 )
		{
			nodeToDel = predFromFingerTable;
		}
		else
		{
			nodeToDel = getSucc();
		}
		deleteAll(nodeToDel, k);
		
	}
	
	public void deleteAll(NodeInfo n, String k) throws Failed{
		if (n.addr.equals(info.addr)) {
			state.deleteAll(k);
		} else {
			WebClient.delAllKeyBindings( client, n, k);
		}
	}


}
