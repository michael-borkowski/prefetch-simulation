package at.borkowski.prefetchsimulation.regression.variables;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;
import at.borkowski.prefetchsimulation.regression.RegressionAnalysis;
import at.borkowski.prefetchsimulation.regression.RegressionContext;

public class S8RelativePredictionTimeErrorMu implements RegressionAnalysis {

   @Override
   public void perform(RegressionContext context) {
      Configuration base = context.getBaseConfiguration();

      for (int i = -100; i <= 100; i += 4) {
         Distribution<Double> relativePredictionTimeError = Distributions.normal(0.01 * i, 0.01);
         Configuration configuration = new Configuration(base.getTotalTicks(), base.getByterate(), base.getSlotLength(), base.getNetworkUptime(), base.getRelativeJitter(), base.getAbsoluteJitter(), relativePredictionTimeError, base.getRelativePredictionAmplitudeError(), base.getAbsolutePredictionTimeError(), base.getAbsolutePredictionAmplitudeError(), base.getRecurringRequestSeries(), base.getIntermittentRequests(), base.getAlgorithm(), base.getAlgorithmConfiguration(), base.getLookAheadTime());
         context.executeRun(String.valueOf(0.01 * i), configuration);
      }
   }

}
