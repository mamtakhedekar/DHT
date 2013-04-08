package edu.stevens.cs549.dht.domain.table;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Item
 *
 */
@Entity

public class Item implements Serializable {

	@Id @GeneratedValue
	private int id;
	private String value;
	private static final long serialVersionUID = 1L;

	public Item() {
		super();
	}   
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}   
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@ManyToOne
	private Key key;

	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	
   
}
