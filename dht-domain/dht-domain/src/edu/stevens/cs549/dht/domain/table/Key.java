package edu.stevens.cs549.dht.domain.table;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Key
 * 
 */
@Entity

@NamedQueries({
		@NamedQuery(name = "SearchAll", query = "select k from Key k"),
		@NamedQuery(name = "DeleteAll", query = "delete from Key k"),
		@NamedQuery(name = "SearchKey", query = "select k from Key k where k.key <= :key"),
		@NamedQuery(name = "DeleteKey", query = "delete from Key k where k.key <= :key"),
		@NamedQuery(name = "SearchSKey", query = "select k from Key k where k.skey = :skey")
})

@Table(name = "SKey")

public class Key implements Serializable {

	@Id
	@GeneratedValue
	private int id;
	@Column(name = "NKey")
	private int key;
	@Column(name = "SKey")
	private String skey;
	private static final long serialVersionUID = 1L;

	public Key() {
		super();
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getSkey() {
		return this.skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	@OneToMany(mappedBy = "key",cascade=CascadeType.REMOVE)
	@OrderBy
	private List<Item> items;

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}
