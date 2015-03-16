package at.borkowski.scovillej.prefetch.algorithms;

import java.util.Collection;
import java.util.Map;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionService;

/**
 * This interface represents an algorithm for scheduling prefetches for
 * requests.
 */
public interface PrefetchAlgorithm {
   Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService);
}
