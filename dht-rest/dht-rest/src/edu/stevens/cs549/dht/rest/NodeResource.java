package edu.stevens.cs549.dht.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import edu.stevens.cs549.dht.node.IDHTResource;
import edu.stevens.cs549.dht.representation.NodeInfo;
import edu.stevens.cs549.dht.representation.NodeInfoRep;
import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.routing.IRouting.Failed;

@Path("/node")
@Stateless
public class NodeResource {
    @Context
    private UriInfo context;

    /**
     * Default constructor. 
     */
    public NodeResource() {
    }
    
    @EJB(beanName="DHTBean")
    IDHTResource dht;

    @GET 
    @Produces("application/xml")
    public NodeInfoRep getNodeInfoXML() {
		try {
			return new NodeInfoRep(dht.getNodeInfo());
		} catch (Failed e) {
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
    }
        
    @GET 
    @Produces("application/json")
    public NodeInfoRep getNodeInfoJSON() {
		try {
			return new NodeInfoRep(dht.getNodeInfo());
		} catch (Failed e) {
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
    }
    @POST@Path("/join")
    @Produces("application/json")
    public void insertIntoDHTJSON() {
    	try {
			new NodeInfoRep(dht.getNodeInfo());
		} catch (Failed e) {
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
    }
    
    @GET
    @Path("/{SKEY}")
    @Produces("application/xml")
    public TableDB getKeyBindings(@PathParam("SKEY") String skey) {
    	String tmpURI = context.getAbsolutePath().toString();
    	tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/")); 
    	System.out.println("URI ==== " + tmpURI);
    	TableDB table = null;
    	try {
				table =  new TableDB(skey, dht.getLocalKeyVals(skey));
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
    	return table;
    }
    
    @GET
    @Path("/{SKEY}")
    @Produces("application/json")
    public TableDB getKeyBindingsJson(@PathParam("SKEY") String skey) {
    	String tmpURI = context.getAbsolutePath().toString();
    	tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/")); 
    	System.out.println("URI ==== " + tmpURI);
    	TableDB table = null; 
    	try {
    		table =  new TableDB(skey, dht.getLocalKeyVals(skey));
		} catch (WebApplicationException e) {
			e.printStackTrace();
		}catch (Failed e) {
			e.printStackTrace();
		}
		return table;
    }    
    
  
    @PUT
    @Path("/{SKEY}")
    @Consumes("application/xml")
    public void insertKeyBinding(@PathParam("SKEY") String key, @QueryParam("value") String val) {
		try {
			dht.add(key, val);
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @PUT
    @Path("/{SKEY}")
    @Consumes("application/json")
    public void insertKeyBindingJson(@PathParam("SKEY") String key, @QueryParam("value") String val) {
		try {
			dht.add(key, val);
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
    }    
     
    @DELETE
    @Path("/{SKEY}")
    @Consumes("application/xml")
    public void deleteKeyBinding(@PathParam("SKEY") String key, @QueryParam("value") String val) {
		try {
			dht.delete(key, val);
		}catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
    }
    
    @DELETE
    @Path("/{SKEY}")
    @Consumes("application/json")
    @Produces("application/json")
    public TableDB deleteKeyBindingJson(@PathParam("SKEY") String key, @QueryParam("value") String val) {
		TableDB table = null; 
		try {
			table = new TableDB(key, dht.delete(key, val));
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
		return table;
    }    
    
    @DELETE@Path("/all/{KEY}")
    @Consumes("application/json")
    public void deleteAllBindings(@PathParam("KEY") String key) {
    		String tmpURI = context.getAbsolutePath().toString();
    		tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/"));
    		tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/"));
    		System.out.println("URI FOR DELETE ALL ==== " + tmpURI);
    		try {
    			dht.deleteAll(key);
    		} catch (WebApplicationException e) {
    			e.printStackTrace();
    		} catch (Failed e) {
    			e.printStackTrace();
    		}

    }
    
    @GET@Path("/succ")
    @Produces("application/json")
    public NodeInfoRep getSuccesorJSON(){
		NodeInfoRep nodeInfoRep = null;
    	try {
			nodeInfoRep = new NodeInfoRep(dht.getSucc());
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}    	
    	return nodeInfoRep;
    }
    
    @GET@Path("/pred")
    @Produces("application/json")
    public NodeInfoRep getPredecesorJSON(){
		NodeInfoRep nodeInfoRep = null;
    	try {
    		NodeInfo pred = dht.getPred();
    		if ( pred == null )
    		{
    			System.out.println("Returning Pred is null!");
    			return null;
    		}
			nodeInfoRep = new NodeInfoRep(pred);
		} catch (WebApplicationException e) {
			throw e;
		} catch (Failed e) {
			e.printStackTrace();
		}
    	
    	return nodeInfoRep;
    }
    
    @GET
    @Path("/finger/{KEY}")
    @Produces("application/json")
    @Consumes("application/json")
    public NodeInfoRep getFinger(@PathParam("KEY") String key){
    	NodeInfoRep ret = null;
    	try {
			ret = new NodeInfoRep(dht.closestPrecedingFinger(Long.parseLong(key)));
		} catch (WebApplicationException e) {
			e.printStackTrace();
		}catch (Failed e) {
			e.printStackTrace();
		}
    	
    	return ret;
    }
    
    
    @GET
    @Path("/xfer/{key}")
    @Consumes("application/json")
    @Produces("application/json")
    public TableDB xferKeyBindings(@PathParam("key") String skey) {
    	String tmpURI = context.getAbsolutePath().toString();
    	tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/")); 
    	tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/")); 
    	//System.out.println("URI ==== " + tmpURI);
    	TableDB table = null;
    	try {
    		long key = Long.parseLong(skey) ;
    		table = dht.xferBindings(key);
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (Failed e) {
			e.printStackTrace();
		}
    	return table;
    }
    
    @PUT@Path("/notify")
    @Consumes("application/json")
	public void notifyNodeJSON(NodeInfoRep prednodeInfo){
	    	String tmpURI = context.getAbsolutePath().toString();
			tmpURI = tmpURI.substring(0, tmpURI.lastIndexOf("/"));
	    	try {
				dht.notify(new NodeInfo(prednodeInfo.id, new URI(prednodeInfo.uri)));
			} catch (WebApplicationException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (Failed e) {
				e.printStackTrace();
			}
	    }
}