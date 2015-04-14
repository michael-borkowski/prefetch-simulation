package at.borkowski.prefetchsimulation.regression;

import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.regression.variables.S1RelativeJitter;

public class Main {
   public static void main(String[] args) {
      Map<String, RegressionAnalysis> analyses = new HashMap<>();

      analyses.put("s1", new S1RelativeJitter());

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
