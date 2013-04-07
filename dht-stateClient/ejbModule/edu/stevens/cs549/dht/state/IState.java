package edu.stevens.cs549.dht.state;

import java.io.IOException;

import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.routing.IRouting;
import edu.stevens.cs549.dht.routing.IRouting.Failed;

/*
 * The interface for a state server that maintains the
 * local (key,value)-pair bindings for a DHT node.
 * This should ONLY be accessed locally by the DHT Web service.
 * Think of it as a database server for a DHT node.
 */

public interface IState {
	
	/*
	 * Get all values stored under a key.
	 */
	public String[] get(String k) throws IRouting.Failed;

	/*
	 * Add a binding under a key (always cumulative).
	 */
	public void add(String k, String v) throws IRouting.Failed;

	/*
	 * Delete a binding under a key.
	 */
	public String[] delete(String k, String v) throws IRouting.Failed;
	
	/*
	 * Transfer bindings at this node up to key k.
	 */
	public TableDB xferBindings(long k) throws IRouting.Failed;
	
	/*
	 * Backup up the bindings in the table to a file.
	 */
	public void backup(String filename) throws IOException, IRouting.Failed;

	/*
	 * Reload the bindings from a file.
	 */
	public void reload(String filename) throws IOException, IRouting.Failed;
	
	/*
	 * Display the contents of the local hash table.
	 */
	public StringBuffer display();

	public void deleteAll(String k) throws IRouting.Failed;
}
