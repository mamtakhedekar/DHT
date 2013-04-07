package edu.stevens.cs549.dht.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import edu.stevens.cs549.dht.representation.NodeInfo;
import edu.stevens.cs549.dht.representation.NodeInfoRep;
import edu.stevens.cs549.dht.representation.TableDB;


public class WebClient {
	
	/*
	 * This project pulls out the logic for REST clients, abstracting from 
	 * the client object, which should be created just once.  It is used
	 * by the WebClientBean EJB in the server, and the CLI in the console client.
	 */

	/*
	 * Ping a remote site to see if it is still available.
	 */
	public static boolean isFailed (Client client, URI uri) {
		WebResource r = client.resource(uri);
		ClientResponse c = r.get(ClientResponse.class);
		return c.getClientResponseStatus().getStatusCode()>=300;
	}

	public static NodeInfo getRemoteSuccForNode(Client client, NodeInfo info)
	{
		WebResource r = client.resource(info.addr);
		
		NodeInfoRep infoRep = r.path("succ").accept("application/json").type(MediaType.APPLICATION_JSON_TYPE).get(NodeInfoRep.class);
		NodeInfo infor = null;
		try {
			infor = new NodeInfo( infoRep.id, new URI(infoRep.uri));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return infor;
		
	}

	public static NodeInfo getRemotePredForNode(Client client, NodeInfo info)
	{
		WebResource r = client.resource(info.addr);
		NodeInfoRep infoRep = null;
		try {
			infoRep = r.path("pred").accept("application/json").type(MediaType.APPLICATION_JSON_TYPE).get(NodeInfoRep.class);

		}
		catch ( UniformInterfaceException ex)
		{
			if ( ex.getResponse().getStatus() == 404 )
			{
				return null;
			}
			return null;
		}

		if (infoRep == null)
			return null;
		
		NodeInfo infor = null;
		try {
			infor = new NodeInfo( infoRep.id, new URI(infoRep.uri));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return infor;
		
	}
	
	public static NodeInfo getRemoteNodeInfo(Client client, NodeInfo info)
	{
		return getRemoteNodeInfo(client, info.addr.toString());
	}
	
	public static NodeInfo getRemoteNodeInfo(Client client, String uri)
	{
		WebResource r = client.resource(uri);
		NodeInfoRep newNodeInfoRep = r.accept("application/json").type(MediaType.APPLICATION_JSON_TYPE).get(edu.stevens.cs549.dht.representation.NodeInfoRep.class);
		try {
			return new NodeInfo(newNodeInfoRep.id, new URI(newNodeInfoRep.uri));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static NodeInfo getRemoteClosestPreceedingFingerForNode(Client client, NodeInfo info, long key)
	{
		WebResource r = client.resource(info.addr);
		NodeInfoRep infoRep = r.path("finger/" + key).accept("application/json").get(edu.stevens.cs549.dht.representation.NodeInfoRep.class);
		
		try {
			return new NodeInfo(infoRep.id, new URI(infoRep.uri));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	public static void notifyRemoteNode(Client client, NodeInfo notifiedNode, NodeInfo notifierNode)
	{
		WebResource r = client.resource(notifiedNode.addr);
		//ClientResponse c = r.get(ClientResponse.class);
		NodeInfoRep nodeRep = new NodeInfoRep(notifierNode);
		r.path("notify").type(MediaType.APPLICATION_JSON_TYPE).put(nodeRep);
	}

	public static void putKeyBinding(Client client, NodeInfo node, String k, String val)
	{
		WebResource r = client.resource(node.addr);
		r.path(k).queryParam("value", val).type(MediaType.APPLICATION_JSON_TYPE).put();
	}

	public static String[] delKeyBinding(Client client, NodeInfo node, String k, String val)
	{
		WebResource r = client.resource(node.addr);
		TableDB list = null;
		try
		{
			list = r.path(k).queryParam("value", val).type(MediaType.APPLICATION_JSON_TYPE).delete(edu.stevens.cs549.dht.representation.TableDB.class);
		}
		catch(UniformInterfaceException ex)
		{
			return null;
		}
		if ( list == null )
			return null;
		else if ( list.entry == null )
			return null;
		
		if ( list.entry.length != 0 )
			return list.entry[0].vals;
		else
			return null;
	}
	
	public static void delAllKeyBindings(Client client, NodeInfo node, String k)
	{
		WebResource r = client.resource(node.addr);
		r.path("all/"+k).type(MediaType.APPLICATION_JSON_TYPE).delete();
	}
	
	public static String[] getRemoteKeyBinding(Client client, NodeInfo info, String k) {
		WebResource r = client.resource(info.addr);
		TableDB list =null;
		try
		{
			list = r.path(k).accept("application/json").type(MediaType.APPLICATION_JSON_TYPE).get(edu.stevens.cs549.dht.representation.TableDB.class);
		}
		catch(UniformInterfaceException ex)
		{
			return null;
		}
		
		if ( list == null )
			return null;
		else if ( list.entry == null )
			return null;
		
		if ( list.entry.length != 0 )
			return list.entry[0].vals;
		else
			return null;
	}
	
	public static String[] getRemoteXferKeyBinding(Client client, NodeInfo info, String k) {
		WebResource r = client.resource(info.addr);
		TableDB list = null;
		try
		{
			list = r.path("xfer/"+k).accept("application/json").type(MediaType.APPLICATION_JSON_TYPE).get(edu.stevens.cs549.dht.representation.TableDB.class);
		}
		catch(UniformInterfaceException ex)
		{
			return null;
		}
		if ( list == null )
			return null;
		else if ( list.entry == null )
			return null;
		
		if ( list.entry.length != 0 )
			return list.entry[0].vals;
		else
			return null;
	}

}
