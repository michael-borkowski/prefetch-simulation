package at.borkowski.prefetchsimulation.configuration;

import java.util.Collection;

import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;

public class Configuration {
   private final long totalTicks, slotLength, lookAheadTime;
   private final int maximumByterate, absoluteJitter;
   private final double networkUptime, relativeJitter, predictionAccuracy;
   private final Collection<RequestSeries> recurringRequestSeries;
   private final Collection<IntermittentRequest> intermittentRequests;
   private final Class<? extends PrefetchAlgorithm> algorithm;
   
   private Long seed;

   public Configuration(long totalTicks, int maximumByterate, long slotLength, double networkUptime, double relativeJitter, int absoluteJitter, double predictionAccuracy, Collection<RequestSeries> recurringRequestSeries, Collection<IntermittentRequest> intermittentRequests, Class<? extends PrefetchAlgorithm> algorithm, long lookAheadTime) {
      this.totalTicks = totalTicks;
      this.maximumByterate = maximumByterate;
      this.slotLength = slotLength;
      this.networkUptime = networkUptime;
      this.relativeJitter = relativeJitter;
      this.absoluteJitter = absoluteJitter;
      this.predictionAccuracy = predictionAccuracy;
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

   public Collection<IntermittentRequest> getIntermittentRequests() {
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

   public double getPredictionAccuracy() {
      return predictionAccuracy;
   }

   public Collection<RequestSeries> getRecurringRequestSeries() {
      return recurringRequestSeries;
   }

   public double getRelativeJitter() {
      return relativeJitter;
   }

   public long getSlotLength() {
      return slotLength;
   }

   public long getTotalTicks() {
      return totalTicks;
   }
}
