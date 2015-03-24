package at.borkowski.prefetchsimulation.configuration;

import java.util.Collection;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;

public class Configuration {
   private final long totalTicks, lookAheadTime;
   Distribution<Long> slotLength;
   private final int maximumByterate, absoluteJitter;
   private final double networkUptime, relativeJitter, predictionTimeAccuracy, predictionAmplitudeAccuracy;
   private final Collection<RequestSeries> recurringRequestSeries;
   private final Collection<Request> intermittentRequests;
   private final Class<? extends PrefetchAlgorithm> algorithm;

   private Long seed;

   public Configuration(long totalTicks, int maximumByterate, Distribution<Long> slotLength, double networkUptime, double relativeJitter, int absoluteJitter, double predictionTimeAccuracy, double predictionAmplitudeAccuracy, Collection<RequestSeries> recurringRequestSeries, Collection<Request> intermittentRequests, Class<? extends PrefetchAlgorithm> algorithm, long lookAheadTime) {
      this.totalTicks = totalTicks;
      this.maximumByterate = maximumByterate;
      this.slotLength = slotLength;
      this.networkUptime = networkUptime;
      this.relativeJitter = relativeJitter;
      this.absoluteJitter = absoluteJitter;
      this.predictionTimeAccuracy = predictionTimeAccuracy;
      this.predictionAmplitudeAccuracy = predictionAmplitudeAccuracy;
      this.recurringRequestSeries = recurringRequestSeries;
      this.intermittentRequests = intermittentRequests;
      this.algorithm = algorithm;
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

   public int getAbsoluteJitter() {
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

   public int getMaximumByterate() {
      return maximumByterate;
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

   public double getRelativeJitter() {
      return relativeJitter;
   }

   public Distribution<Long> getSlotLength() {
      return slotLength;
   }

   public long getTotalTicks() {
      return totalTicks;
   }
}
