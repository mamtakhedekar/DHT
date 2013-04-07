package edu.stevens.cs549.dht.state;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.stevens.cs549.dht.domain.table.Item;
import edu.stevens.cs549.dht.domain.table.Key;
import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.representation.TableRec;
import edu.stevens.cs549.dht.routing.IRouting;

public class Persist {

	/*
	 * This class provides operations for persisting the contents of a DHT node
	 * (key-value pairs only) to disk.
	 * 
	 * We use JAXB for marshalling hashtable contents as XML.
	 */

	protected static Logger log = Logger
			.getLogger("edu.stevens.cs549.dht.state.Persist");

	private static void severe(String msg) {
		log.severe(msg);
	}
	

	/*
	 * Operations on keys and items in database.
	 */
	public static List<Key> getAll(EntityManager em) {
		TypedQuery<Key> query = em.createNamedQuery("SearchAll", Key.class);
		return query.getResultList();
	}
	
	public static void deleteAll(EntityManager em) {
		Query query = em.createNamedQuery("DeleteAll");
		query.executeUpdate();
	}
	
	public static Key getSkey (EntityManager em, String k) {
		TypedQuery<Key> query = em.createNamedQuery("SearchSKey", Key.class).setParameter("skey", k);
		List<Key> keys = query.getResultList();
		if (keys.isEmpty()) {
			return null;
		} else {
			return keys.get(0);
		}
	}
	
	public static Key addKey (EntityManager em, String k) {
		Key key = getSkey(em, k);
		if (key == null) {
			key = new Key();
			key.setKey(k.hashCode() % IRouting.NKEYS);
			key.setSkey(k);
			key.setItems(new ArrayList<Item>());
			em.persist(key);
		} 
		return key;
	}
		
	public static List<Key> getKeys (EntityManager em, long k) {
		TypedQuery<Key> query = em.createNamedQuery("SearchKey", Key.class).setParameter("key", k);
		return query.getResultList();
	}
	
	public static void deleteKeys (EntityManager em, long k) {
/*		Query query = em.createNamedQuery("DeleteKey").setParameter("key", k);
		query.executeUpdate();*/

		TypedQuery<Key> query;

		query = em.createNamedQuery("SearchKey", Key.class).setParameter("key",
				String.valueOf(k).hashCode());

		List<Key> keys = query.getResultList();

		for (Key ky : keys) {

			em.remove(ky);

		}
	}
	
	public static void addItem (EntityManager em, Key k, String v) {
		Item item = new Item();
		item.setKey(k);
		item.setValue(v);
		em.persist(item);
		k.getItems().add(item);
	}


	/*
	 * Loading and saving database or parts thereof
	 */
	protected synchronized static TableDB toTableDB(List<Key> table) {
		/*
		 * Create a TableDB structure of keys (and items) from
		 * a list resulting from a database query.
		 */
		TableDB db = new TableDB();
		db.entry = new TableRec[table.size()];

		Iterator<Key> keys = table.iterator();
		for (int i = 0; keys.hasNext(); i++) {
			Key k = keys.next();
			List<Item> xs = k.getItems();
			if (xs != null) {
				String[] es = new String[xs.size()];
				for (int j=0; j<xs.size(); j++) es[j] = xs.get(j).getValue();
				db.entry[i] = new TableRec(k.getSkey(), es);
			}
		}
		return db;
	}
	
	protected synchronized static void fromTableDB(EntityManager em, TableDB db) {
		/*
		 * Add contents of db to the on-disk database.
		 */
		for (TableRec r : db.entry) {
			Key key = addKey(em, r.key);
			for (String s : r.vals) {
				addItem(em, key, s);
			}
		}
	}

	public static void load(EntityManager em, String filename) {
		try {
			JAXBContext context = JAXBContext.newInstance(TableDB.class);
			Unmarshaller um = context.createUnmarshaller();
			InputStream is = new FileInputStream(filename);
			TableDB db = (TableDB) um.unmarshal(is);
			is.close();

			deleteAll(em);
			fromTableDB(em, db);
		} catch (JAXBException e) {
			severe("JAXB error: " + e);
		} catch (FileNotFoundException e) {
			severe("File not found: " + filename);
		} catch (IOException e) {
			severe("IO Exception closing " + filename);
		}
	}

	public static void save(EntityManager em, String filename) {
		try {
			JAXBContext context = JAXBContext.newInstance(TableDB.class);
			Marshaller m = context.createMarshaller();
			OutputStream os = new FileOutputStream(filename);
			
			TableDB db = toTableDB(getAll(em));
			
			m.marshal(db, os);
			os.close();
		} catch (JAXBException e) {
			severe("JAXB error: " + e);
		} catch (FileNotFoundException e) {
			severe("File not found: " + filename);
		} catch (IOException e) {
			severe("IO Exception closing " + filename);
		}
	}

	public static TableDB xferBindings(EntityManager em, final long key) {
		TableDB db = toTableDB(getKeys(em, key));
		deleteKeys(em, key);
		return db;
	}

	public static void addBindings(EntityManager em, TableDB db) {
		fromTableDB(em, db);
	}


	/*
	 * Display information on a console.
	 */
	public static StringBuffer displayVals(String[] vs) {
		StringWriter sr = new StringWriter();
		PrintWriter wr = new PrintWriter(sr);
		
		wr.print("{");
		if (vs.length > 0) {
			for (int i = 0; i < vs.length - 1; i++) {
				wr.printf("%s,", vs[i]);
			}
			wr.print(vs[vs.length - 1]);
		}
		wr.print("}");
		return sr.getBuffer();
	}

	public static StringBuffer display(EntityManager em) {
		TableDB db = toTableDB(getAll(em));
		
		StringWriter sr = new StringWriter();
		PrintWriter wr = new PrintWriter(sr);
		
		if (db.entry.length == 0) {
			wr.println("No entries.");
		} else {
			wr.printf("%8s %8s %s", "KEY", "NKey","VALUES");
			wr.println();
			for (TableRec r : db.entry) {
				wr.printf("%8s %8s %s", r.key, r.key.hashCode() % IRouting.NKEYS, displayVals(r.vals).toString());
				wr.println();
			}
		}
		return sr.getBuffer();
	}

}
