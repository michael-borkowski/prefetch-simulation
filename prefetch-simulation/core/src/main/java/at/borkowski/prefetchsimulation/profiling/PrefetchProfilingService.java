package at.borkowski.prefetchsimulation.profiling;

import at.borkowski.prefetchsimulation.Request;

/**
 * Represents a profiling service for prefetch simulations.
 */
public interface PrefetchProfilingService {
   void cacheHit(Request request);

   void arrival(Request request, long responseTime, long dataAge, int dataVolume);
}
