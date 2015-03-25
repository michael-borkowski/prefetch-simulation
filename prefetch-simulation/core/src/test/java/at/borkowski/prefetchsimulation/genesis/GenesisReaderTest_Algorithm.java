package at.borkowski.prefetchsimulation.genesis;

import java.util.Collection;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;

public class GenesisReaderTest_Algorithm implements PrefetchAlgorithm {
   @Override
   public void configure(Map<String, String> configuration) {
   }
   
   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      return null;
   }
}
