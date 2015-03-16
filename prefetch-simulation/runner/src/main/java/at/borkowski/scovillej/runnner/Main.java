package at.borkowski.scovillej.runnner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import at.borkowski.scovillej.prefetch.PrefetchSimulationBuilder;
import at.borkowski.scovillej.prefetch.configuration.ConfigurationException;
import at.borkowski.scovillej.prefetch.configuration.ConfigurationReader;
import at.borkowski.scovillej.prefetch.configuration.model.Configuration;
import at.borkowski.scovillej.prefetch.profiling.PrefetchProfilingResults;
import at.borkowski.scovillej.simulation.Simulation;

public class Main {

   public static void main(String[] args) {
      InputStream configurationSource;
      if (args.length > 1) {
         usage();
         return;
      } else if (args.length == 0 || "-".equals(args[0])) {
         configurationSource = System.in;
      } else {
         try {
            configurationSource = new FileInputStream(args[0]);
         } catch (FileNotFoundException e) {
            e.printStackTrace();
            usage();
            return;
         }
      }

      Configuration configuration = null;

      try {
         configuration = new ConfigurationReader(configurationSource).read();
      } catch (IOException | ConfigurationException cEx) {
         cEx.printStackTrace();
         return;
      } finally {
         if (configurationSource != System.in) {
            try {
               configurationSource.close();
            } catch (IOException e) {
               e.printStackTrace();
               return;
            }
         }
      }

      PrefetchSimulationBuilder builder = PrefetchSimulationBuilder.fromConfiguration(configuration);
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
      System.err.println("Usage: runner            reads configuration from standard input");
      System.err.println("       runner -          reads configuration from standard input");
      System.err.println("       runner <filename> reads configuration from filename");
   }

}
