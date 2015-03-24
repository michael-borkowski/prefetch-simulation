package at.borkowski.prefetchsimulation.painter;

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

public class Playground {

   public static void main(String[] args) throws FileNotFoundException {

      InputStream genesisSource = new FileInputStream("/home/michael/projects/da_projects/prefetch-simulation/simulations/2/genesis");
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

      ResultVisualiser.visualise(genesis, profiling);
   }

}
