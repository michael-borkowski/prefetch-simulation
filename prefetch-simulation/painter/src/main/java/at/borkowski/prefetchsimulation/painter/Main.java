package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;
import java.io.InputStream;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.scovillej.simulation.Simulation;

public class Main {
   public static void main(String[] args) throws IOException {
      try {
         InputStream genesisSource = System.in;
         if (args.length != 1) {
            usage();
            System.exit(1);
            return;
         }

         Genesis genesis = null;

         try {
            genesis = new GenesisReader(genesisSource).read();
         } finally {
            if (genesisSource != System.in)
               genesisSource.close();
         }

         PrefetchSimulationBuilder builder = PrefetchSimulationBuilder.fromGenesis(genesis);
         Simulation sim = builder.create();
         PrefetchProfilingResults profiling = builder.getProfiling();

         String command = args[0];

         if ("png".equals(command))
            Saver.savePNG(GenesisVisualiser.visualise(genesis), System.out);
         else if ("eps".equals(command))
            Saver.saveEPS(GenesisVisualiser.visualise(genesis), System.out);
         else if ("tex-timeline".equals(command)) {
            sim.executeToEnd();
            Saver.saveLaTeX(ResultVisualiser.visualise(genesis, profiling), System.out);
         } else {
            System.err.println("Unknown operation: " + command);
            usage();
         }

         sim.executeToEnd();
      } catch (Throwable t) {
         System.err.println(t);
         t.printStackTrace();
         System.exit(1);
      }
   }

   private static void usage() {
      System.err.println("Usage: painter <operation>");
      System.err.println();
      System.err.println("      <operation>: png | eps | tex-timeline");
   }
}
