package edu.stevens.cs549.dht.representation;

// import com.sun.xml.txw2.annotation.XmlElement;


public class TableRec {
	
	/*
	 * Persisting table on disk.
	 */
		
	public String key;
	
	public String[] vals;
	
	public TableRec (String k, String[] vs) {
		this.key = k;
		this.vals = vs;
	}
	
	public TableRec()
	{
		key = "";
		vals = new String[0];
	}
}
