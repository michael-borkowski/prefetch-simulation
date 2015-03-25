package at.borkowski.prefetchsimulation.genesis;

import java.util.List;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;

public class Genesis {
   private final long ticks, lookAheadTime;
   private final List<Request> requests;
   private final Map<Long, Integer> rateReal;
   private final Map<Long, Integer> ratePredicted;
   private final Class<? extends PrefetchAlgorithm> algorithm;
   private final Map<String, String> algorithmConfiguration;

   public Genesis(long ticks, List<Request> requests, Map<Long, Integer> rateReal, Map<Long, Integer> ratePredicted, Class<? extends PrefetchAlgorithm> algorithm, Map<String, String> algorithmConfiguration, long lookAheadTime) {
      this.ticks = ticks;
      this.requests = requests;
      this.rateReal = rateReal;
      this.ratePredicted = ratePredicted;
      this.algorithm = algorithm;
      this.lookAheadTime = lookAheadTime;
      this.algorithmConfiguration = algorithmConfiguration;
   }

   public long getTicks() {
      return ticks;
   }
   
   public long getLookAheadTime() {
      return lookAheadTime;
   }

   public Map<Long, Integer> getRatePredicted() {
      return ratePredicted;
   }

   public Map<Long, Integer> getRateReal() {
      return rateReal;
   }

   public List<Request> getRequests() {
      return requests;
   }
   
   public Class<? extends PrefetchAlgorithm> getAlgorithm() {
      return algorithm;
   }

   public Map<String, String> getAlgorithmConfiguration() {
      return algorithmConfiguration;
   }

}
