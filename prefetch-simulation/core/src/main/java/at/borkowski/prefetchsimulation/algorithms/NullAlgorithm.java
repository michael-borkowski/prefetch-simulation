package at.borkowski.prefetchsimulation.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;

/**
 * This algorithm doesn't schedule any fetches.
 */
public class NullAlgorithm implements PrefetchAlgorithm {
   @Override
   public void configure(Map<String, String> configuration) {
   }

   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      HashMap<Request, Long> ret = new HashMap<>();
      return ret;
   }
}
