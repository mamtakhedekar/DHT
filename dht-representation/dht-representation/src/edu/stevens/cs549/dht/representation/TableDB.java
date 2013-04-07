package edu.stevens.cs549.dht.representation;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="table")
// @XmlAccessorType(XmlAccessType)
public class TableDB {
	
	public TableRec[] entry;
	
	public TableDB(String k, String[] vs) {
		TableRec[] s = { new TableRec(k,vs) };
		this.entry = s;
	}
	
	public TableDB(TableRec r)
	{
		TableRec[] s = { r };
		this.entry = s;
	}
	
	public TableDB(TableRec[] r)
	{
		this.entry = r;
	}
	
	public void AddEntry(String k, String[] vs)
	{
		TableRec[] s = new TableRec[entry.length+1];
		
		for ( int i = 0; i < entry.length; i++ )
		{
			s[i] = entry[i];
		}
		s[entry.length] = new TableRec(k,vs);
		entry = s;
	}
	
	public TableDB() {
		super();
	}	

}
