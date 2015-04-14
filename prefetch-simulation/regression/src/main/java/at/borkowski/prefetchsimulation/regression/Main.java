package at.borkowski.prefetchsimulation.regression;

import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.regression.variables.S1RelativeJitter;
import at.borkowski.prefetchsimulation.regression.variables.S2RelativePredictionAmplitudeError;
import at.borkowski.prefetchsimulation.regression.variables.S3RelativePredictionTimeError;
import at.borkowski.prefetchsimulation.regression.variables.S4LookAheadTime;
import at.borkowski.prefetchsimulation.regression.variables.S5Alpha;

public class Main {
   public static void main(String[] args) {
      Map<String, RegressionAnalysis> analyses = new HashMap<>();

      analyses.put("s1", new S1RelativeJitter());
      analyses.put("s2", new S2RelativePredictionAmplitudeError());
      analyses.put("s3", new S3RelativePredictionTimeError());
      analyses.put("s4", new S4LookAheadTime());
      analyses.put("s5", new S5Alpha());

      String name = null;
      if (args.length == 1) {
         name = args[0].toLowerCase();
         if (!analyses.containsKey(name)) {
            System.err.println("Unknown analysis: " + name);
            System.exit(1);
            return;
         }
      } else if (args.length > 1) {
         System.err.println("Must provide either 0 or 1 parameter (the variable ID, eg. S1)");
         System.exit(1);
         return;
      }
      if (name == null) {
         for (String analysisName : analyses.keySet())
            doAnalysis(analysisName, analyses.get(analysisName));
      } else
         doAnalysis(name, analyses.get(name));

   }

   private static void doAnalysis(String name, RegressionAnalysis regressionAnalysis) {
      RegressionHandler context = new RegressionHandler(500);

      System.out.println();
      System.out.println();
      System.out.println();
      System.out.println("# " + name);
      context.execute(regressionAnalysis);
   }
}
