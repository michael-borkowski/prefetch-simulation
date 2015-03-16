package at.borkowski.scovillej.prefetch.configuration;

import java.util.Collection;
import java.util.Map;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.algorithms.PrefetchAlgorithm;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionService;

public class ConfigurationReaderTest_Algorithm implements PrefetchAlgorithm {
   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      return null;
   }
}
