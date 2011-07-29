package com.imjasonh.partychapp;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


// NOT thread-safe
public class LiveDatastore extends Datastore {

  private static final Logger logger =
      Logger.getLogger(LiveDatastore.class.getName());
  
  private static final String STATS_CACHE_KEY = "stats";
  
  private static Cache STATS_CACHE = null;
  
  static {
    try {
      STATS_CACHE = CacheManager.getInstance().getCacheFactory().createCache(
              ImmutableMap.of(GCacheFactory.EXPIRATION_DELTA, 24 * 60 * 60L));
    } catch (CacheException err) {
      logger.log(Level.SEVERE, "Could not initialize STATS_CACHE", err);
    }
  }
  
  
  static {
	  ObjectifyService.register(Channel.class);
	  ObjectifyService.register(Target.class);
	  ObjectifyService.register(Reason.class);
	  ObjectifyService.register(User.class);
	  ObjectifyService.register(PersistentConfiguration.class);
  }

  
  private Objectify ofy(){
	  return Ofy.instance();
  }
  
  private <T> T getAndLog(Class<? extends T> c, String s){

//		try{
//			throw new Exception("");
//		}catch (Exception e){
//		logger.log(Level.INFO, c.getName() + " read: \n" + 
//	                             e.getStackTrace());
//		}
		
	  return ofy().get(c, s);
  }
  
  @Override
  public Channel getChannelByName(String name) {
	  try {
		  return getAndLog(Channel.class, name);
	  }catch(NotFoundException e){
	      return null;
	  }
  }
  
  @Override
  public PersistentConfiguration getPersistentConfig() {
	  try{
		  return getAndLog(PersistentConfiguration.class, "config");
	  }catch(NotFoundException e){
		  return null;
	  }
  }
  
  @Override
  public User getUserByJID(String jid) {
    try {
    	User user = getAndLog(User.class, jid);
      	return user;
    } catch (NotFoundException notFound) {
	      return null;
    }    
  }

  @Override
  public User getUserByPhoneNumber(String phoneNumber) {
	  //get() returns null if nothing is found
	  User user = ofy().query(User.class).filter("phoneNumber", phoneNumber).get(); 
	  return user;
  }
  
  @Override
  public Target getTargetByID(String key) {
    try {
    	return getAndLog(Target.class, key);
    } catch (NotFoundException e) {
      // TODO(nsanch): there has to be a better way
      return null;
    }
  }

  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    Target t = getTarget(channel, name);
    if (t == null) {
      t = new Target(name, channel);
    }
    return t;
  }
  
  @Override
  public List<Target> getTargetsByChannel(String channelName) {
	  List<Target> targets = ofy().query(Target.class)
		  .filter("channelName", channelName)
		  .list();
	  return targets;
  }

  @Override
  public List<Reason> getReasons(Target target, int limit) {
	  List<Reason> reasons = ofy().query(Reason.class)
	  							  .filter("targetId", target.key())
	  							  .order("-timestamp")
	  							  .limit(limit)
	  							  .list();
	  return reasons;
  }

  public List<Channel> getAllChannels() {
	  List<Channel> channels = ofy().query(Channel.class).list();
	  return channels;
  }
  
  @Override
  public void put(Object o) {
	try{
		logger.warning(o.toString() + " was put.");
		ofy().put(o);
	}catch(Exception e){
	      logger.log(Level.SEVERE, "Could not put a "+o.getClass().getCanonicalName()+" using objectify.", e);
	}
  }

  @Override
  public void putAll(Collection<Object> objects) {
    try{ 
		ofy().put(objects);
	}catch(Exception e){
	      logger.log(Level.SEVERE, "Could not putAll.", e);
	}
  }

  @Override
  public void delete(Object o) {
	try{
		ofy().delete(o);
	}catch(Exception e){
	      logger.log(Level.SEVERE, "Could not delete a "+o.getClass().getCanonicalName()+".", e);
	}
  }
  
  @Override
  public void startRequest(){
  }
  
  @Override
  public void endRequest(){
  }

  int countUsersActiveInLastNDays(DatastoreService ds, int numDays) {
	  com.googlecode.objectify.Query<User> q = ofy().query(User.class);
	  if (numDays > 0) {
	      // This is ridiculous, but 30 days in milliseconds is 2.5B, and if numDays is
	      // in int, the expression below overflows and we look for
	      // lastSeen > some-future-date. To fix, just cast it to a long. 
	      long numDays64Bit = numDays;
		  q.filter("lastSeen >", new Date(System.currentTimeMillis() - numDays64Bit*24*60*60*1000));
	  }
	  return  q.count();
  }


  @Override
  public Datastore.Stats getStats(boolean useCache) {
    if (useCache) {
      try {
        Stats cachedStats = (Stats) STATS_CACHE.get(STATS_CACHE_KEY);
        if (cachedStats != null) {
          return cachedStats;
        }
        logger.info("Stats not in cache, re-computing");
      } catch (InvalidValueException err) {
        logger.log(Level.WARNING, "Could not load data from memcache", err);
      }
    }
    
    Stats ret = new Stats();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(new com.google.appengine.api.datastore.Query("__Stat_Kind__"));
    for (Entity kindStat : pq.asIterable()) {
      String kind = (String)kindStat.getProperty("kind_name");
      if ("Channel".equals(kind)) {
        ret.numChannels = ((Long)kindStat.getProperty("count")).intValue();
        ret.timestamp = (Date)kindStat.getProperty("timestamp");
      }
    }

    ret.numUsers = countUsersActiveInLastNDays(datastore, -1);
    ret.oneDayActiveUsers = countUsersActiveInLastNDays(datastore, 1);
    ret.sevenDayActiveUsers = countUsersActiveInLastNDays(datastore, 7);
    ret.thirtyDayActiveUsers = countUsersActiveInLastNDays(datastore, 30);
    
    STATS_CACHE.put(STATS_CACHE_KEY, ret);
    
    return ret;
  }
  
  private static class ExtractingKeyIterable implements Iterator<String> {
    private Iterator<Entity> wrapped;
    
    public ExtractingKeyIterable(Iterator<Entity> wrapped) {
      this.wrapped = wrapped;
    }
    
    public boolean hasNext() {
      return wrapped.hasNext();
    }
    
    public String next() {
      Key next = wrapped.next().getKey();
      return next.getName();
    }
    
    public void remove() {
      throw new UnsupportedOperationException("remove isn't supported");
    }
  }
  
  @Override
  public Iterator<String> getAllEntityKeys(
      Class<?> entityClass, String lastKey) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    com.google.appengine.api.datastore.Query q = 
        new com.google.appengine.api.datastore.Query(entityClass.getName());
    q.setKeysOnly();
    if (lastKey != null) {
      q.addFilter("name", FilterOperator.GREATER_THAN, lastKey);
    }
    PreparedQuery pq = datastore.prepare(q);
    FetchOptions fetchOptions = FetchOptions.Builder.withOffset(0);
    return new ExtractingKeyIterable(pq.asIterator(fetchOptions));
  }

	@Override
	public void deleteChannelByName(String name) {
		ofy().delete(Channel.class, name);
	}
  
}
