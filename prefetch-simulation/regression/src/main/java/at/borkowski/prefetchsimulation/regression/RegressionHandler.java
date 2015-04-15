package at.borkowski.prefetchsimulation.regression;

import java.io.IOException;
import java.io.InputStream;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.RespectRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.ConfigurationException;
import at.borkowski.prefetchsimulation.configuration.ConfigurationReader;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisGenerator;

public class RegressionHandler implements RegressionContext {

   private static final Configuration baseConfiguration;
   
   private final int runCount;

   static {
      try (InputStream baseConfigurationStream = RegressionHandler.class.getResourceAsStream("/base-configuration")) {
         ConfigurationReader reader = new ConfigurationReader(baseConfigurationStream);
         baseConfiguration = reader.read();
      } catch (IOException | ConfigurationException e) {
         throw new RuntimeException(e);
      }
   }
   
   public RegressionHandler(int runCout) {
      this.runCount = runCout;
   }

   public void execute(RegressionAnalysis analysis) {
      analysis.perform(this);
   }

   @Override
   public void executeRun(String independentVariableLabel, Configuration configuration) {
      Long previousSeed = null;
      if (configuration.hasSeed())
         previousSeed = configuration.getSeed();

      double rtA = 0, rtB = 0, rtC = 0;
      double daA = 0, daB = 0, daC = 0;
      double hrA = 0, hrB = 0, hrC = 0;

      for (int i = 0; i < runCount; i++) {
         long seed = 199100 + i;
         configuration.setSeed(seed);

         Genesis genesis = new GenesisGenerator(configuration).generate();
         PrefetchSimulationBuilder builder;

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new NullAlgorithm());
         builder.create().executeToEnd();
         rtA += builder.getProfiling().getResponseTime().getAverage();
         daA += builder.getProfiling().getDataAge().getDoubleMedian();
         hrA += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new IgnoreRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtB += builder.getProfiling().getResponseTime().getAverage();
         daB += builder.getProfiling().getDataAge().getDoubleMedian();
         hrB += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new RespectRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtC += builder.getProfiling().getResponseTime().getAverage();
         daC += builder.getProfiling().getDataAge().getDoubleMedian();
         hrC += (double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size();
      }

      rtA /= runCount;
      rtB /= runCount;
      rtC /= runCount;
      daA /= runCount;
      daB /= runCount;
      daC /= runCount;
      hrA /= runCount;
      hrB /= runCount;
      hrC /= runCount;

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
