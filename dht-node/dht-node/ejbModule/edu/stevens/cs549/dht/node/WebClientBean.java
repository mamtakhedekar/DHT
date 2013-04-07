package edu.stevens.cs549.dht.node;

import java.net.URI;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.sun.jersey.api.client.Client;

import edu.stevens.cs549.dht.rest.WebClient;

@Stateless(name="WebClientBean")
@LocalBean
public class WebClientBean {
	
	/*
	 * Encapsulate Web client operations here.
	 */
	
	/*
	 * Creation of client instances is expensive, so just create one.
	 */
	protected Client client;
	
	public WebClientBean () {
		client = Client.create();
	}

	/*
	 * Ping a remote site to see if it is still available.
	 */
	public boolean isFailed (URI uri) {
		return WebClient.isFailed(client, uri);
	}

}
