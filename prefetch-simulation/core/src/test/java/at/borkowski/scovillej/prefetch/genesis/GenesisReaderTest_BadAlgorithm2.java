package at.borkowski.scovillej.prefetch.genesis;

import java.util.Collection;
import java.util.Map;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.algorithms.PrefetchAlgorithm;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionService;

public class GenesisReaderTest_BadAlgorithm2 implements PrefetchAlgorithm {
   public GenesisReaderTest_BadAlgorithm2() {
      throw new RuntimeException();
   }
   
   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      return null;
   }
}
