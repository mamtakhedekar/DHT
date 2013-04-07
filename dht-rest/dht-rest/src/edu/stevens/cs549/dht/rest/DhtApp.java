package edu.stevens.cs549.dht.rest;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/dht")
public class DhtApp extends Application {

  @Override	
  public Set<Class<?>> getClasses() {
    Set<Class<?>> s = new HashSet<Class<?>>();
    s.add(NodeResource.class);
    return s;
  }
}