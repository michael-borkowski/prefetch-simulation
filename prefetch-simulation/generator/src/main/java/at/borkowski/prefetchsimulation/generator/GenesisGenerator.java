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
      ReconstructibleRandom randomNetworkQuality = random.fork();
      ReconstructibleRandom randomNetworkPrediction = random.fork();
      ReconstructibleRandom randomNetworkGrain = random.fork();
      
      Map<Long, Integer> networkQuality = generateNetworkQuality(randomNetworkQuality);
      Map<Long, Integer> prediction = generateNetworkQualityPrediction(randomNetworkPrediction, networkQuality);

      networkQuality = grainNetworkQuality(randomNetworkGrain, networkQuality);

      List<Request> requests = new LinkedList<>();
      Genesis genesis = new Genesis(totalTicks, requests, networkQuality, prediction, new RespectRatePredictionAlgorithm());
      return genesis;
   }

   private Map<Long, Integer> generateNetworkQuality(ReconstructibleRandom random) {
      ReconstructibleRandom randomByterate = random.fork();
      ReconstructibleRandom randomUptime = random.fork();
      ReconstructibleRandom randomLength = random.fork();
      
      Map<Long, Integer> ret = new HashMap<>();
      long tick = 0;
      int previousRate = -1;

      while (tick < totalTicks) {
         int byterate = randomByterate.nextInt(maximumByterate);
         if (randomUptime.nextDouble() > networkUptime)
            byterate = 0;

         if (previousRate != -1 && byterate != 0)
            byterate = (1 * byterate + 2 * previousRate) / 3;

         ret.put(tick, byterate);
         tick += (nextDouble(randomLength, 0.1, 3)) * networkQualityPhaseLength;

         if (byterate != 0)
            previousRate = byterate;
      }

      return ret;
   }

   private Map<Long, Integer> grainNetworkQuality(ReconstructibleRandom random, Map<Long, Integer> networkQuality) {
      ReconstructibleRandom randomStability = random.fork();
      ReconstructibleRandom randomVariability = random.fork();
      
      Map<Long, Integer> ret = new HashMap<>();
      int lastRate = -1;

      long tickStep = Math.max(1, networkQualityPhaseLength / 10);

      for (long tick = 0; tick < totalTicks; tick++) {
         if (networkQuality.containsKey(tick))
            lastRate = networkQuality.get(tick);

         double stability = nextDouble(randomStability, networkByterateStability, 2D - networkByterateStability);
         int variability = nextInt(randomVariability, -networkByterateVariability, +networkByterateVariability);

         if (tick % tickStep == 0) {
            int byterate = lastRate;

            if (byterate != 0)
               byterate = (int) (stability * byterate + variability);

            ret.put(tick, (int) Math.min(maximumByterate, Math.max(0, byterate)));
         }
      }

      return ret;
   }

   private Map<Long, Integer> generateNetworkQualityPrediction(ReconstructibleRandom random, Map<Long, Integer> networkQuality) {
      ReconstructibleRandom randomTick = random.fork();
      ReconstructibleRandom randomAccuracy = random.fork();
      
      Map<Long, Integer> ret = new HashMap<>();

      for (long tick : networkQuality.keySet()) {
         long predictionTick = 0;
         if (tick != 0)
            predictionTick = (long) (tick + nextDouble(randomTick, -0.5, +0.5) * networkQualityPhaseLength);

         int predictionByterate = (int) (nextDouble(randomAccuracy, predictionAccuracy, 2D - predictionAccuracy) * networkQuality.get(tick));
         ret.put(predictionTick, predictionByterate);
      }

      return ret;
   }

   private double nextDouble(ReconstructibleRandom random, double min, double max) {
      return min + random.nextDouble() * (max - min);
   }

   private int nextInt(ReconstructibleRandom random, int min, int max) {
      return min + random.nextInt(max - min);
   }
}
