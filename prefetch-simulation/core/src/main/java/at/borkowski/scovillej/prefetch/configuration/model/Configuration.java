package at.borkowski.scovillej.prefetch.configuration.model;

import java.util.List;
import java.util.Map;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.algorithms.PrefetchAlgorithm;

public class Configuration {
   private final long ticks;
   private final List<Request> requests;
   private final Map<Long, Integer> rateReal;
   private final Map<Long, Integer> ratePredicted;
   private final PrefetchAlgorithm algorithm;

   public Configuration(long ticks, List<Request> requests, Map<Long, Integer> rateReal, Map<Long, Integer> ratePredicted, PrefetchAlgorithm algorithm) {
      this.ticks = ticks;
      this.requests = requests;
      this.rateReal = rateReal;
      this.ratePredicted = ratePredicted;
      this.algorithm = algorithm;
   }

   public long getTicks() {
      return ticks;
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
   
   public PrefetchAlgorithm getAlgorithm() {
      return algorithm;
   }

}
