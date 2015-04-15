package at.borkowski.prefetchsimulation.regression.variables;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.regression.RegressionAnalysis;
import at.borkowski.prefetchsimulation.regression.RegressionContext;

public class S4LookAheadTime implements RegressionAnalysis {

   @Override
   public void perform(RegressionContext context) {
      Configuration base = context.getBaseConfiguration();

      for (long lookAheadTime = 0; lookAheadTime <= 400; lookAheadTime += 8) {
         Configuration configuration = new Configuration(base.getTotalTicks(), base.getByterate(), base.getSlotLength(), base.getNetworkUptime(), base.getRelativeJitter(), base.getAbsoluteJitter(), base.getRelativePredictionTimeError(), base.getRelativePredictionAmplitudeError(), base.getAbsolutePredictionTimeError(), base.getAbsolutePredictionAmplitudeError(), base.getRecurringRequestSeries(), base.getIntermittentRequests(), base.getAlgorithm(), base.getAlgorithmConfiguration(), lookAheadTime);
         context.executeRun(String.valueOf(lookAheadTime), configuration);
      }
   }

}
