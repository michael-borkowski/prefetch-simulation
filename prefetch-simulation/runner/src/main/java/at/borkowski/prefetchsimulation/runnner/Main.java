package at.borkowski.prefetchsimulation.runnner;

import java.io.IOException;
import java.util.Formatter;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisException;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.scovillej.simulation.Simulation;

public class Main {

   public static void main(String[] args) throws IOException {
      try {
         if (args.length != 0) {
            System.err.println("No operations supported");
            System.exit(1);
            return;
         }

         Genesis genesis = null;

         try {
            genesis = new GenesisReader(System.in).read();
         } catch (IOException | GenesisException cEx) {
            cEx.printStackTrace();
            return;
         }

         PrefetchSimulationBuilder builder = PrefetchSimulationBuilder.fromGenesis(genesis);
         Simulation sim = builder.create();
         PrefetchProfilingResults profiling = builder.getProfiling();

         sim.executeToEnd();

         System.out.println("RT:          " + profiling.getResponseTime());
         System.out.println("DA:          " + profiling.getDataAge());
         System.out.println("DV:          " + profiling.getDataVolume());
         System.out.println("Hit Rate:    " + profiling.getCacheHits().getCount() + " / " + genesis.getRequests().size() + " (" + formatHitRate(profiling.getCacheHits().getCount(), genesis.getRequests().size()) + ")");
      } catch (Throwable t) {
         System.err.println(t);
         t.printStackTrace();
         System.exit(1);
      }
   }

   private static String formatHitRate(long count, int size) {
      try (Formatter f = new Formatter()) {
         return f.format("%2.1f", (100D * count / size)).toString();
      }
   }
}
