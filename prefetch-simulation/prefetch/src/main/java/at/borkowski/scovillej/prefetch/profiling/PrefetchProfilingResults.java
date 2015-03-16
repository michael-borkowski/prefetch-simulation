package at.borkowski.scovillej.prefetch.profiling;

import at.borkowski.scovillej.profile.SeriesResult;

/**
 * Represents the profiling results for a prefetching simulation.
 */
public interface PrefetchProfilingResults {
   /**
    * Returns the URT (user response time) series.
    * 
    * @return the URT series
    */
   SeriesResult<Long> getURT();

   /**
    * Returns the series of overdue times of cache misses (how many ticks a
    * request has been overdue).
    * 
    * @return the overdue series
    */
   SeriesResult<Long> getOverdue();

   /**
    * Returns the data age series (how old data was upon client code supply).
    * 
    * @return the data age series
    */
   // TODO make it so
   SeriesResult<Long> getCacheHitAges();

   /**
    * Returns the cache miss series.
    * 
    * @return the cace miss series
    */
   SeriesResult<Void> getCacheMisses();

   /**
    * Returns the series of stretch results (user response time per KB).
    * 
    * @return the stretch series
    */
   SeriesResult<Double> getStretch();

}
