package at.borkowski.prefetchsimulation.members.client;

import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;

/**
 * Represents the cache sub-processor of {@link FetchClient}. It is responsible
 * for caching results of requests (actually, since content is irrelevant, it
 * only caches the fact that they have been fetched along with a timestamp).
 */
public class CacheProcessor {
   private Map<Request, Long> cache = new HashMap<>();
   private Map<Request, Long> requestCache = new HashMap<>();

   /**
    * Returns <code>true</code> if the cache contains the given request
    * 
    * @param request
    *           the request to be checked
    * @return whether the cache contains the request
    */
   public boolean hasFile(Request request) {
      return cache.containsKey(request);
   }

   /**
    * Returns the timestamp with which the request has been saved. Behavior is
    * undefined (currently throws a {@link NullPointerException}) if the request
    * is not stored (see {@link #hasFile(Request)}).
    * 
    * @param request
    *           the request to be checked
    * @return the timestamp at which the request has been saved
    */
   public long getTimestamp(Request request) {
      return cache.get(request);
   }

   /**
    * Saves a request to the cache.
    * 
    * @param request
    *           the request to save
    * @param tick
    *           the current tick (timestamp of the request)
    * @param requestTick
    *           the tick at which the data has been requested
    */
   public void save(Request request, long tick, long requestTick) {
      cache.put(request, tick);
      requestCache.put(request, requestTick);
   }

   /**
    * Returns the timestamp with which the request has been requested from the
    * server. Behavior is undefined (currently throws a
    * {@link NullPointerException}) if the request is not stored (see
    * {@link #hasFile(Request)}).
    * 
    * @param request
    *           the request to be checked
    * @return the timestamp at which the request has been requested
    */
   public long getRequestTimestamp(Request request) {
      return requestCache.get(request);
   }
}
