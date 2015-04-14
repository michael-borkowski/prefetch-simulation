package at.borkowski.prefetchsimulation.regression.variables;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;
import at.borkowski.prefetchsimulation.regression.RegressionAnalysis;
import at.borkowski.prefetchsimulation.regression.RegressionContext;

public class S1RelativeJitter implements RegressionAnalysis {

   @Override
   public void perform(RegressionContext context) {
      Configuration base = context.getBaseConfiguration();

      for (int i = 0; i <= 400; i += 4) {
         Distribution<Double> relativeJitter = Distributions.normal(0, 0.01 * i);
         Configuration configuration = new Configuration(base.getTotalTicks(), base.getByterate(), base.getSlotLength(), base.getNetworkUptime(), relativeJitter, base.getAbsoluteJitter(), base.getRelativePredictionTimeError(), base.getRelativePredictionAmplitudeError(), base.getAbsolutePredictionTimeError(), base.getAbsolutePredictionAmplitudeError(), base.getRecurringRequestSeries(), base.getIntermittentRequests(), base.getAlgorithm(), base.getAlgorithmConfiguration(), base.getLookAheadTime());
         context.executeRun(String.valueOf(0.01 * i), configuration);
      }
   }

}
