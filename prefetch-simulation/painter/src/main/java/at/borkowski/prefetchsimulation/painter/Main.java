package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;
import java.io.InputStream;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisException;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.scovillej.simulation.Simulation;

public class Main {
   public static void main(String[] args) throws IOException {
      InputStream genesisSource = System.in;
      if (args.length > 1) {
         usage();
         return;
      }

      Genesis genesis = null;

      try {
         genesis = new GenesisReader(genesisSource).read();
      } catch (IOException | GenesisException cEx) {
         cEx.printStackTrace();
         return;
      } finally {
         if (genesisSource != System.in) {
            try {
               genesisSource.close();
            } catch (IOException e) {
               e.printStackTrace();
               return;
            }
         }
      }

      PrefetchSimulationBuilder builder = PrefetchSimulationBuilder.fromGenesis(genesis);
      Simulation sim = builder.create();
      PrefetchProfilingResults profiling = builder.getProfiling();

      String command = args[0];

      if ("png".equals(command))
         Saver.savePNG(GenesisVisualiser.visualise(genesis), System.out);
      if ("eps".equals(command))
         Saver.saveEPS(GenesisVisualiser.visualise(genesis), System.out);
      if ("tex-timeline".equals(command)) {
         sim.executeToEnd();
         Saver.saveLaTeX(ResultVisualiser.visualise(genesis, profiling), System.out);
      } else
         usage();

      sim.executeToEnd();
   }

   private static void usage() {
      System.err.println("Usage: painter <operation>");
      System.err.println();
      System.err.println("      <operation>: png | eps | tex-timeline");
   }
}
