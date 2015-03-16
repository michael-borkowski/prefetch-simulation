package at.borkowski.scovillej.prefetch.profiling;

import at.borkowski.scovillej.prefetch.Request;

/**
 * Represents a profiling service for prefetch simulations.
 */
public interface PrefetchProfilingService {
   /**
    * Registers that a data fetch has been completed.
    * 
    * @param request
    *           the request fetched
    * @param size
    *           the size fetched
    * @param tick
    *           the current tick
    * @param duration
    *           the duration of transfer
    */
   void fetched(Request request, int size, long tick, long duration);

   /**
    * Registers a cache hit (ie. the request being satisfied directly from the
    * cache without need to transfer data anymore).
    * 
    * @param request
    *           the request
    * @param age
    *           the cache age in ticks
    */
   void cacheHit(Request request, long age);

   /**
    * Registers a cache miss (ie. the request nit being satisfied from the cache
    * and the need for transferring data).
    * 
    * @param request
    *           the request
    */
   void cacheMiss(Request request);

   /**
    * Will be changed
    * 
    * @param request
    *           will be changed
    */
   // TODO: change to unified arrival
   void lateArrival(Request request);

}
