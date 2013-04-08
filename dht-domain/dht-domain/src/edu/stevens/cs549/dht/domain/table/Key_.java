package edu.stevens.cs549.dht.domain.table;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2012-08-08T16:04:52.207-0400")
@StaticMetamodel(Key.class)
public class Key_ {
	public static volatile SingularAttribute<Key, Integer> id;
	public static volatile SingularAttribute<Key, Integer> key;
	public static volatile SingularAttribute<Key, String> skey;
	public static volatile ListAttribute<Key, Item> items;
}
