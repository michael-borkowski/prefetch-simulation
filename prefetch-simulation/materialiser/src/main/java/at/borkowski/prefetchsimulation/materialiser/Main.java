package at.borkowski.prefetchsimulation.materialiser;

import java.util.Random;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.ConfigurationReader;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisGenerator;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;
import at.borkowski.prefetchsimulation.painter.GenesisVisualiser;
import at.borkowski.prefetchsimulation.painter.Saver;

public class Main {
   public static void main(String[] args) throws Exception {
      if (args.length != 1) {
         System.err.println("Must supply exactly one argument (operation), not " + args.length);
         usage();
         return;
      }
      String operation = args[0];

      if (operation.equals("generate-genesis")) {
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
      } else if (operation.equals("png")) {
         Genesis genesis = new GenesisReader(System.in).read();
         Saver.savePNG(GenesisVisualiser.visualise(genesis), System.out);
      } else if (operation.equals("eps")) {
         Genesis genesis = new GenesisReader(System.in).read();
         Saver.saveEPS(GenesisVisualiser.visualise(genesis), System.out);
      } else {
         System.err.println("Unknown operation: " + operation);
         usage();
      }
   }

   private static void usage() {
      System.err.println("Usage: materisaliser <operation>");
      System.err.println();
      System.err.println("      <operation>: generate-genesis | png | eps");
   }
}
