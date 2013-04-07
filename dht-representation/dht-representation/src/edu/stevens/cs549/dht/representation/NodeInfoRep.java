package edu.stevens.cs549.dht.representation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeInfoRep {
	
	/*
	 * Representation of a node info.
	 */
	
	public long id;
	
	public String uri;
	
	public NodeInfoRep(NodeInfo p) {
		this.id = p.key;
		this.uri = p.addr.toString();
	}
	
	public NodeInfoRep() {
		super();
	}

}
