package at.borkowski.prefetchsimulation.runnner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisException;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.scovillej.simulation.Simulation;

public class Main {

   public static void main(String[] args) {
      InputStream genesisSource;
      if (args.length > 1) {
         usage();
         return;
      } else if (args.length == 0 || "-".equals(args[0])) {
         genesisSource = System.in;
      } else {
         try {
            genesisSource = new FileInputStream(args[0]);
         } catch (FileNotFoundException e) {
            e.printStackTrace();
            usage();
            return;
         }
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

      System.out.println();
      System.out.println();
      System.out.println("Misses (due): " + profiling.getOverdue());
      System.out.println("Hits (age):   " + profiling.getCacheHitAges());
      System.out.println("URT:          " + profiling.getURT());
      System.out.println("Stretch:      " + profiling.getStretch());

      System.out.println("End.");
   }

   private static void usage() {
      System.err.println("Usage: runner            reads genesis from standard input");
      System.err.println("       runner -          reads genesis from standard input");
      System.err.println("       runner <filename> reads genesis from filename");
   }

}
