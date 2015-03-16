package at.borkowski.prefetchsimulation.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.RespectRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.genesis.Genesis;

public class GenesisGenerator {

   private final Random random;

   private final long totalTicks, networkQualityPhaseLength;
   private final int maximumByterate;
   private final double networkUptime, networkByterateStability, predictionAccuracy;
   private final Collection<RequestSeries> recurringSeries;
   private final Collection<IntermittentRequest> intermittentRequests;

   public GenesisGenerator(long totalTicks, int maximumByterate, long networkQualityPhaseLength, double networkUptime, double networkByterateStability, double predictionAccuracy, Collection<RequestSeries> recurringSeries, Collection<IntermittentRequest> intermittentRequests) {
      random = new Random();

      this.totalTicks = totalTicks;
      this.maximumByterate = maximumByterate;
      this.networkQualityPhaseLength = networkQualityPhaseLength;
      this.networkUptime = networkUptime;
      this.networkByterateStability = networkByterateStability;
      this.predictionAccuracy = predictionAccuracy;
      this.recurringSeries = recurringSeries;
      this.intermittentRequests = intermittentRequests;
   }

   public void seed(long seed) {
      random.setSeed(seed);
   }

   public Genesis generate() {
      Map<Long, Integer> networkQuality = generateNetworkQuality();
      Map<Long, Integer> prediction = generateNetworkQualityPrediction(networkQuality);

      networkQuality = grainNetworkQuality(networkQuality);

      List<Request> requests = new LinkedList<>();
      Genesis genesis = new Genesis(totalTicks, requests, networkQuality, prediction, new RespectRatePredictionAlgorithm());
      return genesis;
   }

   private Map<Long, Integer> generateNetworkQuality() {
      Map<Long, Integer> ret = new HashMap<>();
      long tick = 0;

      while (tick < totalTicks) {
         int byterate;
         if (random.nextDouble() > networkUptime)
            byterate = 0;
         else
            byterate = random.nextInt(maximumByterate);

         ret.put(tick, byterate);
         tick += (nextDouble(0.5, 1.5)) * networkQualityPhaseLength;
      }

      return ret;
   }

   private Map<Long, Integer> grainNetworkQuality(Map<Long, Integer> networkQuality) {
      Map<Long, Integer> ret = new HashMap<>();
      int lastRate = -1;

      long tickStep = Math.max(1, networkQualityPhaseLength / 20);

      for (long tick = 0; tick < totalTicks; tick += tickStep) {
         if (networkQuality.containsKey(tick))
            lastRate = networkQuality.get(tick);
         ret.put(tick, (int) (nextDouble(networkByterateStability, 2D - networkByterateStability) * lastRate));
      }

      return ret;
   }

   private double nextDouble(double min, double max) {
      return min + (random.nextDouble() * (max - min));
   }

   private Map<Long, Integer> generateNetworkQualityPrediction(Map<Long, Integer> networkQuality) {
      Map<Long, Integer> ret = new HashMap<>();

      for (long tick : networkQuality.keySet()) {
         long predictionTick = 0;
         if (tick != 0)
            predictionTick = tick; //(long) (tick + nextDouble(-0.2, +0.2) * networkQualityPhaseLength);

         int predictionByterate = (int) (nextDouble(predictionAccuracy, 2D - predictionAccuracy) * networkQuality.get(tick));
         ret.put(predictionTick, predictionByterate);
      }

      return ret;
   }
}
