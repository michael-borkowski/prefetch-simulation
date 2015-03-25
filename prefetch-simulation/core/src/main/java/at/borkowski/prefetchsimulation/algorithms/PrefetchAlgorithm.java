package at.borkowski.prefetchsimulation.algorithms;

import java.util.Collection;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;

/**
 * This interface represents an algorithm for scheduling prefetches for
 * requests.
 */
public interface PrefetchAlgorithm {
   Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService);

   void configure(Map<String, String> configuration);
}
