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

   private static Random seedSource = new Random();
   private final ReconstructibleRandom random;

   private final long totalTicks, networkQualityPhaseLength;
   private final int maximumByterate, networkByterateVariability;
   private final double networkUptime, networkByterateStability, predictionAccuracy;
   private final Collection<RequestSeries> recurringSeries;
   private final Collection<IntermittentRequest> intermittentRequests;

   public GenesisGenerator(long totalTicks, int maximumByterate, long networkQualityPhaseLength, double networkUptime, double networkByterateStability, int networkByterateVariability, double predictionAccuracy, Collection<RequestSeries> recurringSeries, Collection<IntermittentRequest> intermittentRequests) {
      random = new ReconstructibleRandom(seedSource.nextLong());

      this.totalTicks = totalTicks;
      this.maximumByterate = maximumByterate;
      this.networkQualityPhaseLength = networkQualityPhaseLength;
      this.networkUptime = networkUptime;
      this.networkByterateVariability = networkByterateVariability;
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
      int previousRate = -1;

      while (tick < totalTicks) {
         int byterate = random.nextInt(maximumByterate);
         if (random.nextDouble() > networkUptime)
            byterate = 0;

         if (previousRate != -1 && byterate != 0)
            byterate = (1 * byterate + 2 * previousRate) / 3;

         ret.put(tick, byterate);
         tick += (nextDouble(0.1, 3)) * networkQualityPhaseLength;

         if (byterate != 0)
            previousRate = byterate;
      }

      return ret;
   }

   private Map<Long, Integer> grainNetworkQuality(Map<Long, Integer> networkQuality) {
      Map<Long, Integer> ret = new HashMap<>();
      int lastRate = -1;

      long tickStep = Math.max(1, networkQualityPhaseLength / 10);

      for (long tick = 0; tick < totalTicks; tick++) {
         if (networkQuality.containsKey(tick))
            lastRate = networkQuality.get(tick);

         if (tick % tickStep == 0)
            ret.put(tick, (int) Math.max(0, nextDouble(networkByterateStability, 2D - networkByterateStability) * lastRate + nextInt(-networkByterateVariability, +networkByterateVariability)));
      }

      return ret;
   }

   private double nextDouble(double min, double max) {
      return min + random.nextDouble() * (max - min);
   }

   private int nextInt(int min, int max) {
      return min + random.nextInt(max - min);
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
