package edu.stevens.cs549.dht.routing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;

import edu.stevens.cs549.dht.representation.NodeInfo;


/**
 * Session Bean implementation class Routing
 */
@Singleton(name="RoutingBean")
public class Routing implements IRoutingLocal, IRoutingRemote {

    /**
     * Default constructor. 
     */
    public Routing() {
    }
    
	public static Logger log = Logger
			.getLogger("edu.stevens.cs.cs549.dht.routing.Routing");


	protected NodeInfo info;
	
	@Resource(name="Uri")
	private String uri;
	
	@Resource(name="Key")
	private String key;
	
	@PostConstruct
	public void init()  {
		try {
			this.info = new NodeInfo(Long.parseLong(key), new URI(uri));
		} catch (URISyntaxException e) {
			log.severe("Bad URI: "+uri);
		}
		this.predecessor = null;
		this.successor = info;
	}
	
	public NodeInfo getNodeInfo() {
		return this.info;
	}
	

	/*
	 * Use the "failed" flag to simulate node failures.
	 */
	protected boolean failed = false;
	
	public void setFailed() {
		failed = true;
	}
	
	public void checkFailed() throws Failed {
		if (failed)
			throw new Failed();
	}
	
	/*
	 * Routing data structures.
	 */
	protected NodeInfo predecessor;
	
	protected NodeInfo successor;
    
	@Override
	public synchronized NodeInfo getPred() throws Failed {
		checkFailed();
		return predecessor;
	}

	@Override
	public synchronized void setPred(NodeInfo pred) throws Failed {
		checkFailed();
		predecessor = pred;
	}

	@Override
	public synchronized NodeInfo getSucc() throws Failed {
		checkFailed();
		return successor;
	}

	@Override
	public synchronized void setSucc(NodeInfo succ) throws Failed {
		checkFailed();
		successor = succ;
	}

	
	/*
	 * Finger table.
	 */
	private NodeInfo[] finger = new NodeInfo[NFINGERS];

	public synchronized NodeInfo setFinger(int i, NodeInfo info) throws Failed {
		checkFailed();
		finger[i] = info;
		return info;
	}

	@Override
	public synchronized NodeInfo getFinger(int i) throws Failed {
		checkFailed();
		return finger[i];
	}

	private long powersOfTwo(long i)
	{
		if ( i == 0 ) return 1;
		
		long res = 1;
		for( int j = 1; j <= i; j++ )
		{
			res = res * 2;
		}
		
		return res;
	}
	
	@Override
	public synchronized NodeInfo closestPrecedingFinger(long k) throws Failed {
		if ( finger.length == 0 )
		{
			return null;
		}
		int i = NFINGERS - 1;
		NodeInfo predFinger = finger[NFINGERS - 1];
		for ( long exp = powersOfTwo(i); i > 0 ; i--, exp = exp/2)
		{
			// we should look for the finger key  value whose is less than the key to search
			// if yes we return the finger Node pointer
			long fingerKey = (this.info.key + exp) % IRouting.NKEYS;
			if ( finger[i] != null && 
				(  NodeInfo.isBetweenPredAndSucc(fingerKey, this.info.key, k) != 0 ) )
			{
				predFinger =  finger[i] != null? finger[i] : getNodeInfo();
				System.out.println("Comparing finger key finger =" + fingerKey + 
						" with requested key =" + k );
				
				break;
			}
		}
		
		System.out.println("Ret closest finger for key " + k + " as " + predFinger.key );
		return predFinger;
	}

	@Override
	public synchronized StringBuffer routes() {
		StringWriter sr = new StringWriter();
		PrintWriter wr = new PrintWriter(sr);
		wr.println("self       : "+ info);
		wr.println("Predecessor: "+predecessor);
		wr.println("Successor  : "+successor);
		wr.println("Fingers:");
		wr.printf("%7s  %3s  %s\n", "Formula", "Key", "Succ");
		wr.printf("%7s  %3s  %s\n", "-------", "---", "----");
		for (int i=0, exp=1; i<IRouting.NFINGERS; i++,exp=2*exp) {
			wr.printf("%7s  %3d  %s\n", info.key+"+2^"+i, 
									 (info.key+exp)%IRouting.NKEYS,
									 finger[i]);
		}
		return sr.getBuffer();		
	}

}
