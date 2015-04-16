package at.borkowski.prefetchsimulation.regression.variables;

import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.regression.RegressionAnalysis;
import at.borkowski.prefetchsimulation.regression.RegressionContext;

public class S5Alpha implements RegressionAnalysis {

   @Override
   public void perform(RegressionContext context) {
      Configuration base = context.getBaseConfiguration();

      for (int i = 24; i <= 100; i += 2) {
         Map<String, String> algorithmConfiguration = new HashMap<>(base.getAlgorithmConfiguration());
         algorithmConfiguration.put("alpha", String.valueOf(0.01 * i));
         Configuration configuration = new Configuration(base.getTotalTicks(), base.getByterate(), base.getSlotLength(), base.getNetworkUptime(), base.getRelativeJitter(), base.getAbsoluteJitter(), base.getRelativePredictionTimeError(), base.getRelativePredictionAmplitudeError(), base.getAbsolutePredictionTimeError(), base.getAbsolutePredictionAmplitudeError(), base.getRecurringRequestSeries(), base.getIntermittentRequests(), base.getAlgorithm(), algorithmConfiguration, base.getLookAheadTime());
         context.executeRun(String.valueOf(0.01 * i), configuration);
      }
   }

}
