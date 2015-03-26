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
   private final double networkUptime;
   private final Distribution<Double> relativePredictionTimeError;
   private final Distribution<Double> relativePredictionAmplitudeError;
   private final Distribution<Long> absolutePredictionTimeError;
   private final Distribution<Integer> absolutePredictionAmplitudeError;
   Distribution<Double> relativeJitter;
   private final Collection<RequestSeries> recurringRequestSeries;
   private final Collection<Request> intermittentRequests;
   private final Class<? extends PrefetchAlgorithm> algorithm;
   private final Map<String, String> algorithmConfiguration;

   private Long seed;

   public Configuration(long totalTicks, Distribution<Integer> byterate, Distribution<Long> slotLength, double networkUptime, Distribution<Double> relativeJitter, Distribution<Integer> absoluteJitter, Distribution<Double> relativePredictionTimeError, Distribution<Double> relativePredictionAmplitudeError, Distribution<Long> absolutePredictionTimeError, Distribution<Integer> absolutePredictionAmplitudeError, Collection<RequestSeries> recurringRequestSeries, Collection<Request> intermittentRequests, Class<? extends PrefetchAlgorithm> algorithm, Map<String, String> algorithmConfiguration, long lookAheadTime) {
      this.totalTicks = totalTicks;
      this.byterate = byterate;
      this.slotLength = slotLength;
      this.networkUptime = networkUptime;
      this.relativeJitter = relativeJitter;
      this.absoluteJitter = absoluteJitter;
      this.relativePredictionTimeError = relativePredictionTimeError;
      this.relativePredictionAmplitudeError = relativePredictionAmplitudeError;
      this.absolutePredictionTimeError = absolutePredictionTimeError;
      this.absolutePredictionAmplitudeError = absolutePredictionAmplitudeError;
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

   public Distribution<Integer> getAbsolutePredictionAmplitudeError() {
      return absolutePredictionAmplitudeError;
   }

   public Distribution<Long> getAbsolutePredictionTimeError() {
      return absolutePredictionTimeError;
   }

   public Distribution<Double> getRelativePredictionAmplitudeError() {
      return relativePredictionAmplitudeError;
   }

   public Distribution<Double> getRelativePredictionTimeError() {
      return relativePredictionTimeError;
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
