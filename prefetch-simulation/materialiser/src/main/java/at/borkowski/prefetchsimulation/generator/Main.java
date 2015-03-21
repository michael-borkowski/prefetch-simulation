package at.borkowski.prefetchsimulation.generator;

import java.io.IOException;
import java.io.InputStream;

import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.ConfigurationException;
import at.borkowski.prefetchsimulation.configuration.ConfigurationReader;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisException;
import at.borkowski.prefetchsimulation.genesis.GenesisReader;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;
import at.borkowski.prefetchsimulation.painter.GenesisPainter;
import at.borkowski.prefetchsimulation.painter.PaintResult;
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
         Genesis genesis = createGenesis(System.in);
         GenesisWriter genesisWriter = new GenesisWriter(System.out);
         genesisWriter.write(genesis);
      } else if (operation.equals("draw-png")) {
         Genesis genesis = readGenesis(System.in);
         PaintResult result = GenesisPainter.paint(genesis);
         Saver.savePNG(result, System.out);
      } else {
         System.err.println("Unknown operation: " + operation);
         usage();
      }
   }

   private static Genesis createGenesis(InputStream in) throws IOException, ConfigurationException {
      ConfigurationReader configurationReader = new ConfigurationReader(in);
      Configuration configuration = configurationReader.read();

      GenesisGenerator generator = new GenesisGenerator(configuration);
      return generator.generate();
   }

   private static Genesis readGenesis(InputStream in) throws IOException, GenesisException {
      return new GenesisReader(in).read();
   }

   private static void usage() {
      System.err.println("Usage: materisaliser <operation>");
      System.err.println();
      System.err.println("      <operation>: generate-genesis | draw-png");
   }
}
