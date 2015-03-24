package at.borkowski.prefetchsimulation.profiling;

import java.util.Set;

import at.borkowski.prefetchsimulation.Request;
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

   Set<Request> getCacheHitRequests();

   Long getFetchStart(Request request);

   Long getFetchFinish(Request request);
   
   Long getScheduledStart(Request request);
}
