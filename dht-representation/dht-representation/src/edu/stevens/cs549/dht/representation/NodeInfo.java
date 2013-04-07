package edu.stevens.cs549.dht.representation;

import java.net.URI;

public class NodeInfo implements java.io.Serializable {	
	/*
	 * Information about a node: its URI and key (hashcode)
	 */
	private static final long serialVersionUID = 1L;

	public long key;
	public URI addr;
	
	public NodeInfo (long k, URI u) {
		key = k;
		addr = u;
	}
	
	@Override
	public String toString() {
		return "[key="+key+",addr="+addr+"]";
	}
	
	// this method returns 0 when there is no match meaning the key key is not in the interval
	// it returns a non-zero value when the key is within the interval
	public static int isBetweenPredAndSuccInclusive( long key, NodeInfo pred, NodeInfo succ)
	{
		int retCode = 0;
		
		if ( pred.key == succ.key ) // when a node in the DHT does not have a known successor
		{
			retCode = 3;
		}
		else if ( succ.key >= pred.key ) // Normal case
		{
			if ( pred.key < key && key <= succ.key )
			{
				retCode = 1;
			}
		}
		else if ( succ.key < pred.key ) // where succ  < pred loop over case  
		{
			if ( pred.key < key && succ.key < key)
				// 60 < 62(key) < 10 - within the interval
			{
				return 4;
			}
			else if ( pred.key > key && key <= succ.key )
				// 62 < 5 (key)< 10 - within the interval
				// A positive test
			{
				retCode = 2;
			}
			else if ( pred.key > key && succ.key < key )
				// we should make sure that the key is between pred and succ
				// for e.g we should avoid the case where 62 < 12(key) < 10
				// outside the interval
				// A negative test
			{
				retCode = 0;
			}

		}
		else
			retCode = 0;
		
		return retCode;
	}
	
	public static int isBetweenPredAndSucc( long key, long predKey, long succKey )
	{
		int retCode = 0;
		if ( succKey >= predKey ) // Normal case
		{
			if ( predKey < key && key < succKey ) // note here we are checking excluding the border value
			{
				retCode = 1;
			}
		}
		else if ( succKey < predKey ) // where succ  < pred loop over case  
		{
			if ( predKey < key && succKey < key)
				// 60 < 62(key) < 10 - within the interval
			{
				return 4;
			}
			else if ( predKey > key && key < succKey ) // note here we are excluding the border values
				// 62 < 5 (key)< 10 - within the interval
				// A positive test
			{
				retCode = 2;
			}
			else if ( predKey > key && succKey < key )
				// we should make sure that the key is between pred and succ
				// for e.g we should avoid the case where 62 < 12(key) < 10
				// outside the interval
				// A negative test
			{
				retCode = 0;
			}
		}
		else
			retCode = 0;
		
		return retCode;
	}
}

