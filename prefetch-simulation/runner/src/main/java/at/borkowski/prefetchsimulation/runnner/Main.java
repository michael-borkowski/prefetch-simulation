package at.borkowski.prefetchsimulation.runnner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisException;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.painter.ResultVisualiser;
import at.borkowski.prefetchsimulation.painter.Saver;
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

      sim.executeToEnd();

      System.err.println();
      System.err.println();
      System.err.println("RT:          " + profiling.getResponseTime());
      System.err.println("DA:          " + profiling.getDataAge());
      System.err.println("DV:          " + profiling.getDataVolume());
      System.err.println("Hit Rate:    " + profiling.getCacheHits().getCount() + " / " + genesis.getRequests().size() + " (" + formatHitRate(profiling.getCacheHits().getCount(), genesis.getRequests().size()) + ")");

      if (args.length == 1) {
         String command = args[0];

         if ("latex-timeline".equals(command))
            Saver.saveLaTeX(ResultVisualiser.visualise(genesis, profiling), System.out);
         else
            usage();
      }

      System.err.println("End.");
   }

   private static String formatHitRate(long count, int size) {
      try (Formatter f = new Formatter()) {
         return f.format("%2.1f", (100D * count / size)).toString();
      }
   }

   private static void usage() {
      System.err.println("Usage: runner [<output>]");
      System.err.println();
      System.err.println("      [<output>]: latex-timeline");
   }

}
