package at.borkowski.prefetchsimulation.genesis;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.RequestSeries;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.util.RepeatableRandom;

public class GenesisGenerator {

   private static Random seedSource = new Random();
   private final RepeatableRandom random;

   private final long totalTicks, lookAheadTime;
   private final Distribution<Integer> absoluteJitter;
   private final Distribution<Double> relativeJitter;
   private final double networkUptime;
   private final Distribution<Double> relativePredictionTimeAccuracy;
   private final Distribution<Double> relativePredictionAmplitudeAccuracy;
   private final Distribution<Long> absolutePredictionTimeAccuracy;
   private final Distribution<Integer> absolutePredictionAmplitudeAccuracy;
   private final Distribution<Integer> byterate;
   private final Distribution<Long> slotLength;
   private final Collection<RequestSeries> recurringSeries;
   private final Collection<Request> intermittentRequests;
   private final Class<? extends PrefetchAlgorithm> algorithm;
   private final Map<String, String> algorithmConfiguration;

   public GenesisGenerator(Configuration configuration) {
      random = new RepeatableRandom(seedSource.nextLong());

      this.totalTicks = configuration.getTotalTicks();
      this.byterate = configuration.getByterate();
      this.slotLength = configuration.getSlotLength();
      this.networkUptime = configuration.getNetworkUptime();
      this.absoluteJitter = configuration.getAbsoluteJitter();
      this.relativeJitter = configuration.getRelativeJitter();
      this.relativePredictionTimeAccuracy = configuration.getRelativePredictionTimeAccuracy();
      this.relativePredictionAmplitudeAccuracy = configuration.getRelativePredictionAmplitudeAccuracy();
      this.absolutePredictionTimeAccuracy = configuration.getAbsolutePredictionTimeAccuracy();
      this.absolutePredictionAmplitudeAccuracy = configuration.getAbsolutePredictionAmplitudeAccuracy();
      this.recurringSeries = configuration.getRecurringRequestSeries();
      this.intermittentRequests = configuration.getIntermittentRequests();
      this.algorithmConfiguration = configuration.getAlgorithmConfiguration();
      this.algorithm = configuration.getAlgorithm();
      this.lookAheadTime = configuration.getLookAheadTime();

      if (configuration.hasSeed())
         random.setSeed(configuration.getSeed());
   }

   public void seed(long seed) {
      random.setSeed(seed);
   }

   private long clamp(long min, long value, long max) {
      if (value < min)
         value = min;
      if (value > max)
         value = max;
      return value;
   }

   private int clamp(int min, int value, int max) {
      if (value < min)
         value = min;
      if (value > max)
         value = max;
      return value;
   }

   public Genesis generate() {
      RepeatableRandom randomNetworkQuality = random.fork();
      RepeatableRandom randomNetworkPrediction = random.fork();
      RepeatableRandom randomNetworkGrain = random.fork();
      RepeatableRandom randomSeries = random.fork();

      Map<Long, Integer> networkQuality = generateNetworkQuality(randomNetworkQuality);
      Map<Long, Integer> prediction = generateNetworkQualityPrediction(randomNetworkPrediction, networkQuality);

      networkQuality = grainNetworkQuality(randomNetworkGrain, networkQuality);

      List<Request> requests = new LinkedList<>();

      requests.addAll(intermittentRequests);

      for (RequestSeries series : recurringSeries)
         generateSeries(randomSeries, requests, series);

      Genesis genesis = new Genesis(totalTicks, requests, networkQuality, prediction, algorithm, algorithmConfiguration, lookAheadTime);
      return genesis;
   }

   private void generateSeries(RepeatableRandom random, List<Request> requests, RequestSeries series) {
      RepeatableRandom randomSize = random.fork();
      RepeatableRandom randomByterate = random.fork();
      RepeatableRandom randomInterval = random.fork();

      long start = series.getStartTick().getValue(random.fork());
      long end = series.getEndTick().getValue(random.fork());

      start = clamp(0, start, totalTicks - 1);
      end = clamp(start, end, totalTicks - 1);

      long current = start;

      while (current <= end) {
         int data = series.getSize().getValue(randomSize);
         int byterate = series.getByterate().getValue(randomByterate);

         data = clamp(1, data, Integer.MAX_VALUE);
         byterate = clamp(1, byterate, Integer.MAX_VALUE);

         requests.add(new Request(current, data, byterate));

         current += clamp(1, series.getInterval().getValue(randomInterval), Long.MAX_VALUE);
      }
   }

   private Map<Long, Integer> generateNetworkQuality(RepeatableRandom random) {
      RepeatableRandom randomByterate = random.fork();
      RepeatableRandom randomUptime = random.fork();
      RepeatableRandom randomLength = random.fork();

      Map<Long, Integer> ret = new HashMap<>();
      long tick = 0;
      int previousRate = -1;

      while (tick < totalTicks) {
         int byterate = this.byterate.getValue(randomByterate);
         if (randomUptime.nextDouble() > networkUptime)
            byterate = 0;

         if (previousRate != -1 && byterate != 0)
            byterate = (2 * byterate + 1 * previousRate) / 3;

         ret.put(tick, byterate);
         tick += clamp(1, slotLength.getValue(randomLength), Long.MAX_VALUE);

         if (byterate != 0)
            previousRate = byterate;
      }

      return ret;
   }

   private Map<Long, Integer> grainNetworkQuality(RepeatableRandom random, Map<Long, Integer> networkQuality) {
      RepeatableRandom randomRelative = random.fork();
      RepeatableRandom randomAbsolute = random.fork();

      Map<Long, Integer> ret = new HashMap<>();
      int lastRate = -1;

      long tickStep = Math.max(1, slotLength.getMean() / 10);

      for (long tick = 0; tick < totalTicks; tick++) {
         if (networkQuality.containsKey(tick))
            lastRate = networkQuality.get(tick);

         double relativeJitter = this.relativeJitter.getValue(randomRelative);
         int absoluteJitter = this.absoluteJitter.getValue(randomAbsolute);

         if (tick % tickStep == 0) {
            int byterate = lastRate;

            if (byterate != 0)
               byterate = (int) ((1D + relativeJitter) * byterate + absoluteJitter);

            ret.put(tick, clamp(0, byterate, Integer.MAX_VALUE));
         }
      }

      return ret;
   }

   private Map<Long, Integer> generateNetworkQualityPrediction(RepeatableRandom random, Map<Long, Integer> networkQuality) {
      RepeatableRandom randomTick = random.fork();
      RepeatableRandom randomAccuracy = random.fork();

      Map<Long, Integer> ret = new HashMap<>();

      for (long tick : networkQuality.keySet()) {
         long predictionTick = 0;
         if (tick != 0) {
            double relativeTime = this.relativePredictionTimeAccuracy.getValue(randomTick);
            long absoluteTime = this.absolutePredictionTimeAccuracy.getValue(randomTick);
            predictionTick = tick + absoluteTime + (long) (relativeTime * slotLength.getMean());
         }

         predictionTick = clamp(0, predictionTick, totalTicks - 1);

         double relativeAmplitude = this.relativePredictionAmplitudeAccuracy.getValue(randomAccuracy);
         int absoluteAmplitude = this.absolutePredictionAmplitudeAccuracy.getValue(randomAccuracy);

         int predictionByterate = networkQuality.get(tick);
         predictionByterate = (int) (predictionByterate * relativeAmplitude) + absoluteAmplitude;
         ret.put(predictionTick, predictionByterate);
      }

      return ret;
   }
}
