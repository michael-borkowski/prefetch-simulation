package at.borkowski.prefetchsimulation.regression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.RespectRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.ConfigurationException;
import at.borkowski.prefetchsimulation.configuration.ConfigurationReader;
import at.borkowski.prefetchsimulation.configuration.RequestSeries;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisGenerator;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;

public class RegressionHandler implements RegressionContext {

   public static final int RUN_COUNT = 10;

   private static final long totalTicks = 36000;
   private static final Distribution<Integer> byterate = Distributions.uniform(30000, 200000);
   private static final Distribution<Long> slotLength = Distributions.normal(120L, 30L);
   private static final double networkUptime = 0.95;
   private static final Distribution<Double> relativeJitter = Distributions.normal(0, 0.05);
   private static final Distribution<Integer> absoluteJitter = Distributions.exact(0);
   private static final Distribution<Double> relativePredictionTimeError = Distributions.normal(0, 0.05);
   private static final Distribution<Double> relativePredictionAmplitudeError = Distributions.normal(0, 0.05);
   private static final Distribution<Long> absolutePredictionTimeError = Distributions.exact(0L);
   private static final Distribution<Integer> absolutePredictionAmplitudeError = Distributions.exact(0);
   private static final Collection<RequestSeries> recurringRequestSeries = createRequestSeries(327750, 20);
   private static final Collection<Request> intermittentRequests = new LinkedList<>();
   private static final Class<? extends PrefetchAlgorithm> algorithm = NullAlgorithm.class;
   private static final Map<String, String> algorithmConfiguration = new HashMap<>();
   private static final long lookAheadTime = 18000;

   private static final Configuration baseConfiguration; //= new Configuration(totalTicks, byterate, slotLength, networkUptime, relativeJitter, absoluteJitter, relativePredictionTimeError, relativePredictionAmplitudeError, absolutePredictionTimeError, absolutePredictionAmplitudeError, recurringRequestSeries, intermittentRequests, algorithm, algorithmConfiguration, lookAheadTime);

   static {
      try (InputStream baseConfigurationStream = RegressionHandler.class.getResourceAsStream("/base-configuration")) {
         ConfigurationReader reader = new ConfigurationReader(baseConfigurationStream);
         baseConfiguration = reader.read();
      } catch (IOException | ConfigurationException e) {
         throw new RuntimeException(e);
      }
   }

   public void execute(RegressionAnalysis analysis) {
      analysis.perform(this);
   }

   private static Collection<RequestSeries> createRequestSeries(long load, int requestCount) {
      List<RequestSeries> ret = new LinkedList<>();

      Distribution<Long> interval = Distributions.normal(300L, 25L);
      Distribution<Integer> size = Distributions.exact((int) (load / requestCount));
      Distribution<Integer> byterate = RegressionHandler.byterate;
      Distribution<Long> startTick = Distributions.exact(1800L);
      Distribution<Long> endTick = Distributions.exact(36000L);

      ret.add(new RequestSeries(interval, size, byterate, startTick, endTick));
      return ret;
   }

   @Override
   public void executeRun(String independentVariableLabel, Configuration configuration) {
      Long previousSeed = null;
      if (configuration.hasSeed())
         previousSeed = configuration.getSeed();

      double rtA = 0, rtB = 0, rtC = 0;
      double daA = 0, daB = 0, daC = 0;
      double hrA = 0, hrB = 0, hrC = 0;
      int count = 0;

      for (int i = 0; i < RUN_COUNT; i++) {
         long seed = 199100 + i;
         configuration.setSeed(seed);

         Genesis genesis = new GenesisGenerator(configuration).generate();
         try {
            new GenesisWriter(new FileOutputStream("/tmp/genesis")).write(genesis);
         } catch (IOException e) {
            e.printStackTrace();
         }
         PrefetchSimulationBuilder builder;

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new NullAlgorithm());
         builder.create().executeToEnd();
         rtA += builder.getProfiling().getResponseTime().getDoubleMedian();
         daA += builder.getProfiling().getDataAge().getDoubleMedian();
         hrA += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new IgnoreRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtB += builder.getProfiling().getResponseTime().getDoubleMedian();
         daB += builder.getProfiling().getDataAge().getDoubleMedian();
         hrB += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new RespectRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtC += builder.getProfiling().getResponseTime().getDoubleMedian();
         daC += builder.getProfiling().getDataAge().getDoubleMedian();
         hrC += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();

         count++;
      }

      rtA /= count;
      rtB /= count;
      rtC /= count;
      daA /= count;
      daB /= count;
      daC /= count;
      hrA /= count;
      hrB /= count;
      hrC /= count;

      System.out.println(independentVariableLabel + "," + rtA + "," + rtB + "," + rtC + "," + daA + "," + daB + "," + daC + "," + hrA + "," + hrB + "," + hrC);

      if (previousSeed == null)
         configuration.clearSeed();
      else
         configuration.setSeed(previousSeed);
   }

   @Override
   public Configuration getBaseConfiguration() {
      return baseConfiguration;
   }

}
