package at.borkowski.prefetchsimulation.regression.variables;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;
import at.borkowski.prefetchsimulation.regression.RegressionAnalysis;
import at.borkowski.prefetchsimulation.regression.RegressionContext;

public class S2RelativePredictionAmplitudeErrorSigma implements RegressionAnalysis {

   @Override
   public void perform(RegressionContext context) {
      Configuration base = context.getBaseConfiguration();

      for (int i = 0; i <= 400; i += 5) {
         Distribution<Double> relativePredictionAmplitudeError = Distributions.normal(0, 0.01 * i);
         Configuration configuration = new Configuration(base.getTotalTicks(), base.getByterate(), base.getSlotLength(), base.getNetworkUptime(), base.getRelativeJitter(), base.getAbsoluteJitter(), base.getRelativePredictionTimeError(), relativePredictionAmplitudeError, base.getAbsolutePredictionTimeError(), base.getAbsolutePredictionAmplitudeError(), base.getRecurringRequestSeries(), base.getIntermittentRequests(), base.getAlgorithm(), base.getAlgorithmConfiguration(), base.getLookAheadTime());
         context.executeRun(String.valueOf(0.01 * i), configuration);
      }
   }

}
