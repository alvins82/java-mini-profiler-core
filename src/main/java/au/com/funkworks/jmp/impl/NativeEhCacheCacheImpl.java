package au.com.funkworks.jmp.impl;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import au.com.funkworks.jmp.interfaces.CacheProfilerService;

public class NativeEhCacheCacheImpl implements CacheProfilerService {

	private Cache cache;

	public NativeEhCacheCacheImpl() {
		//Create a CacheManager using defaults  
		CacheManager manager = CacheManager.create();  		  
		//Create a Cache specifying its configuration.  
		cache = new Cache(  
		     new CacheConfiguration("profilerCache", 5000)  
		       .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)  
		       .overflowToDisk(true)  
		       .eternal(false)  
		       .timeToLiveSeconds(60)  
		       .timeToIdleSeconds(30)  
		       .diskPersistent(false)  
		       .diskExpiryThreadIntervalSeconds(0));  
		manager.addCache(cache);  
	}

	@Override
	public void put(String key, Map<String, Object> data) {
		Element elem = new Element(key, data);
		cache.put(elem);		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> get(String key) {		
		Element elem = cache.get(key);
		if (elem != null) {
			return (Map<String, Object>)elem.getValue();
		}
		return null;
	}
	
}
