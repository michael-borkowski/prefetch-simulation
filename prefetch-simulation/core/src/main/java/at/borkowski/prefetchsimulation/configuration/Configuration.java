package at.borkowski.prefetchsimulation.configuration;

import java.util.Collection;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;

public class Configuration {
   private final long totalTicks, lookAheadTime;
   private final Distribution<Long> slotLength;
   private final Distribution<Integer> byterate;
   private final Distribution<Integer> absoluteJitter;
   private final double networkUptime, predictionTimeAccuracy, predictionAmplitudeAccuracy;
   Distribution<Double> relativeJitter;
   private final Collection<RequestSeries> recurringRequestSeries;
   private final Collection<Request> intermittentRequests;
   private final Class<? extends PrefetchAlgorithm> algorithm;
   private final Map<String, String> algorithmConfiguration;

   private Long seed;

   public Configuration(long totalTicks, Distribution<Integer> byterate, Distribution<Long> slotLength, double networkUptime, Distribution<Double> relativeJitter, Distribution<Integer> absoluteJitter, double predictionTimeAccuracy, double predictionAmplitudeAccuracy, Collection<RequestSeries> recurringRequestSeries, Collection<Request> intermittentRequests, Class<? extends PrefetchAlgorithm> algorithm, Map<String, String> algorithmConfiguration, long lookAheadTime) {
      this.totalTicks = totalTicks;
      this.byterate = byterate;
      this.slotLength = slotLength;
      this.networkUptime = networkUptime;
      this.relativeJitter = relativeJitter;
      this.absoluteJitter = absoluteJitter;
      this.predictionTimeAccuracy = predictionTimeAccuracy;
      this.predictionAmplitudeAccuracy = predictionAmplitudeAccuracy;
      this.recurringRequestSeries = recurringRequestSeries;
      this.intermittentRequests = intermittentRequests;
      this.algorithm = algorithm;
      this.algorithmConfiguration = algorithmConfiguration;
      this.lookAheadTime = lookAheadTime;
   }

   public void clearSeed() {
      seed = null;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   public long getSeed() {
      return seed.longValue();
   }

   public boolean hasSeed() {
      return seed != null;
   }

   public Distribution<Integer> getAbsoluteJitter() {
      return absoluteJitter;
   }

   public Class<? extends PrefetchAlgorithm> getAlgorithm() {
      return algorithm;
   }

   public Collection<Request> getIntermittentRequests() {
      return intermittentRequests;
   }

   public long getLookAheadTime() {
      return lookAheadTime;
   }

   public double getNetworkUptime() {
      return networkUptime;
   }

   public double getPredictionTimeAccuracy() {
      return predictionTimeAccuracy;
   }

   public double getPredictionAmplitudeAccuracy() {
      return predictionAmplitudeAccuracy;
   }

   public Collection<RequestSeries> getRecurringRequestSeries() {
      return recurringRequestSeries;
   }

   public Distribution<Double> getRelativeJitter() {
      return relativeJitter;
   }

   public Distribution<Long> getSlotLength() {
      return slotLength;
   }

   public long getTotalTicks() {
      return totalTicks;
   }
   
   public Distribution<Integer> getByterate() {
      return byterate;
   }

   public Map<String, String> getAlgorithmConfiguration() {
      return algorithmConfiguration;
   }
}
