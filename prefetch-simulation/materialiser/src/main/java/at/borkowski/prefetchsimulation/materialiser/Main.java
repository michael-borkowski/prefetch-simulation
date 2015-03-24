package at.borkowski.prefetchsimulation.materialiser;

import java.util.Random;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.ConfigurationReader;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisGenerator;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;

public class Main {
   public static void main(String[] args) throws Exception {
      if (args.length != 0) {
         System.err.println("No operations supported");
         return;
      }

      long seed = new Random().nextLong();

      ConfigurationReader configurationReader = new ConfigurationReader(System.in);
      Configuration configuration = configurationReader.read();
      GenesisGenerator generator = new GenesisGenerator(configuration);
      if (configuration.hasSeed())
         seed = configuration.getSeed();
      else
         generator.seed(seed);
      Genesis genesis = generator.generate();

      System.out.println("# materialised using seed: " + seed);

      GenesisWriter genesisWriter = new GenesisWriter(System.out);
      genesisWriter.write(genesis);
   }
}
