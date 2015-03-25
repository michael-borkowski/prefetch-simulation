package at.borkowski.prefetchsimulation.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;

/**
 * This algorithm schedules all fetches for the deadline of their request, or
 * earlier if this slot is already taken.
 */
public class StartAtDeadlineAlgorithm implements PrefetchAlgorithm {
   @Override
   public void configure(Map<String, String> configuration) {}

   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      HashMap<Request, Long> ret = new HashMap<>();

      for (Request req : requests) {
         long start = req.getDeadline();
         ret.put(req, start);
      }

      return ret;
   }
}
