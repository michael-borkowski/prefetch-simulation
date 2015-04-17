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
      System.out.println("x,URTA,URTB,URTC,DAA,DAB,DAC,HRA,HRB,HRC,URTA_s,URTB_s,URTC_s,DAA_s,DAB_s,DAC_s,HRA_s,HRB_s,HRC_s,t_URT_ac,t_URT_bc,t_DA_ac,t_DA_bc,t_max");
      analysis.perform(this);
   }

   @Override
   public void executeRun(String independentVariableLabel, Configuration configuration) {
      Long previousSeed = null;
      if (configuration.hasSeed())
         previousSeed = configuration.getSeed();

      Quantity rtA = new Quantity();
      Quantity rtB = new Quantity();
      Quantity rtC = new Quantity();
      Quantity daA = new Quantity();
      Quantity daB = new Quantity();
      Quantity daC = new Quantity();
      Quantity hrA = new Quantity();
      Quantity hrB = new Quantity();
      Quantity hrC = new Quantity();

      for (int i = 0; i < runCount; i++) {
         long seed = 199100 + i;
         configuration.setSeed(seed);

         Genesis genesis = new GenesisGenerator(configuration).generate();
         PrefetchSimulationBuilder builder;

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new NullAlgorithm());
         builder.create().executeToEnd();
         rtA.add(builder.getProfiling().getResponseTime().getAverage());
         daA.add(builder.getProfiling().getDataAge().getDoubleMedian());
         hrA.add((double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size());

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new IgnoreRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtB.add(builder.getProfiling().getResponseTime().getAverage());
         daB.add(builder.getProfiling().getDataAge().getDoubleMedian());
         hrB.add((double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size());

         builder = PrefetchSimulationBuilder.fromGenesis(genesis).algorithm(new RespectRatePredictionAlgorithm());
         builder.create().executeToEnd();
         rtC.add(builder.getProfiling().getResponseTime().getAverage());
         daC.add(builder.getProfiling().getDataAge().getDoubleMedian());
         hrC.add((double) builder.getProfiling().getCacheHits().getCount() / genesis.getRequests().size());
      }

      StringBuilder sb = new StringBuilder();
      sb.append(independentVariableLabel);

      sb.append(',');
      sb.append(rtA.getMean());
      sb.append(',');
      sb.append(rtB.getMean());
      sb.append(',');
      sb.append(rtC.getMean());

      sb.append(',');
      sb.append(daA.getMean());
      sb.append(',');
      sb.append(daB.getMean());
      sb.append(',');
      sb.append(daC.getMean());

      sb.append(',');
      sb.append(hrA.getMean());
      sb.append(',');
      sb.append(hrB.getMean());
      sb.append(',');
      sb.append(hrC.getMean());

      sb.append(',');
      sb.append(rtA.getStandardDeviation());
      sb.append(',');
      sb.append(rtB.getStandardDeviation());
      sb.append(',');
      sb.append(rtC.getStandardDeviation());

      sb.append(',');
      sb.append(daA.getStandardDeviation());
      sb.append(',');
      sb.append(daB.getStandardDeviation());
      sb.append(',');
      sb.append(daC.getStandardDeviation());

      sb.append(',');
      sb.append(hrA.getStandardDeviation());
      sb.append(',');
      sb.append(hrB.getStandardDeviation());
      sb.append(',');
      sb.append(hrC.getStandardDeviation());

      double tURT_AC = tTest(rtA, rtC);
      double tURT_BC = tTest(rtB, rtC);
      double tDA_AC = tTest(daA, daC);
      double tDA_BC = tTest(daB, daC);

      sb.append(',');
      sb.append(tURT_AC);
      sb.append(',');
      sb.append(tURT_BC);

      sb.append(',');
      sb.append(tDA_AC);
      sb.append(',');
      sb.append(tDA_BC);

      sb.append(',');
      sb.append(Math.max(Math.max(Math.max(tURT_AC, tURT_BC), tDA_AC), tDA_BC));

      System.out.println(sb.toString());

      if (previousSeed == null)
         configuration.clearSeed();
      else
         configuration.setSeed(previousSeed);
   }

   private double tTest(Quantity a, Quantity b) {
      double n = 0.5 * (a.getCount() + b.getCount());
      double num = a.getMean() - b.getMean();
      double denom = Math.sqrt(0.5 * (sq(a.getStandardDeviation()) + sq(b.getStandardDeviation()))) * Math.sqrt(2 / n);
      return Math.abs(num / denom);
   }

   private double sq(double x) {
      return x * x;
   }

   @Override
   public Configuration getBaseConfiguration() {
      return baseConfiguration;
   }

}
