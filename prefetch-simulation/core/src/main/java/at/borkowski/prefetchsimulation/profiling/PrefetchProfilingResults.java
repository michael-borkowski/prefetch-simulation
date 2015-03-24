package at.borkowski.prefetchsimulation.profiling;

import at.borkowski.scovillej.profile.SeriesResult;

/**
 * Represents the profiling results for a prefetching simulation.
 */
public interface PrefetchProfilingResults {
   /**
    * Returns the RT (response time) series.
    * 
    * @return the RT series
    */
   SeriesResult<Long> getResponseTime();

   SeriesResult<Long> getDataAge();

   SeriesResult<Long> getDataVolume();

   /**
    * Returns the cache hit series.
    * 
    * @return the cache hit series
    */
   SeriesResult<Void> getCacheHits();
}
