package edu.stevens.cs549.dht.state;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import edu.stevens.cs549.dht.domain.table.Item;
import edu.stevens.cs549.dht.domain.table.Key;
import edu.stevens.cs549.dht.representation.TableDB;
import edu.stevens.cs549.dht.routing.IRouting;
import edu.stevens.cs549.dht.routing.IRouting.Failed;
import edu.stevens.cs549.dht.routing.IRoutingLocal;

/**
 * Session Bean implementation class State
 */
@Stateless(name="StateBean")
public class State implements IStateLocal, IStateRemote {
	
    @EJB(beanName="RoutingBean")
    IRoutingLocal routing;
    
    @PersistenceContext(unitName="dht-domain")
    EntityManager em;
    
    /**
     * Default constructor. 
     */
    public State() {
    }

/*    @PostConstruct
    public void init()
    {
    	em.setFlushMode(FlushModeType.AUTO);
    }*/

    @Override
	public void add(String k, String v) throws Failed {
		Key key = Persist.addKey(em, k);
		Persist.addItem(em, key, v);
	}

	@Override
	public void backup(String filename) throws IOException, Failed {
		checkFailed();
		Persist.save(em, filename);
	}

	protected void checkFailed() throws Failed { 
    	routing.checkFailed();
    }

	@Override
	public String[] delete(String k, String v) throws Failed {
		Key key = Persist.getSkey(em, k);
		if (key != null) {
			for (Item item : key.getItems()) {
				if (v.equals(item.getValue())) {
					em.remove(item);
					key.getItems().remove(item);
					break;
				}
			}
		}
		
		//em.refresh(key);
		key = Persist.getSkey(em, k);
		
		if ( key.getItems() == null )
			return null;
		
		System.out.println("list has elems:"+ key.getItems().size() );
		String[] str = new String[key.getItems().size()];

		if (key != null) {
			int i = 0;
			for (Item item : key.getItems()) {
				str[i] = item.getValue();
				i++;
			}
		}
		return str;
	}

	@Override
	public StringBuffer display() {
		//em.clear();
		return Persist.display(em);
	}

	@Override
	public String[] get(String k) throws Failed {
		//em.clear();
		Key key = Persist.getSkey(em, k);
		if (key == null) {
			return null;
		}
		List<Item> items = key.getItems();
		String[] es = new String[items.size()];
		for (int i=0; i<items.size(); i++) {
			es[i] = items.get(i).getValue();
		}
		return es;
	}

	@Override
	public void reload(String filename) throws IOException, Failed {
		checkFailed();
		Persist.deleteAll(em);
		Persist.load(em, filename);
	}

	@Override
	public TableDB xferBindings(long k) throws Failed {
		checkFailed();
		return Persist.xferBindings(em, k);
	}

	@Override
	public void deleteAll(String k) throws Failed {
		checkFailed();
		Key key = Persist.getSkey(em, k);
		if (key != null) {
			for (Item item : key.getItems()) {
					em.remove(item);
			}
		}
		Persist.deleteKeys(em, k.hashCode() % IRouting.NKEYS);
	}

}
