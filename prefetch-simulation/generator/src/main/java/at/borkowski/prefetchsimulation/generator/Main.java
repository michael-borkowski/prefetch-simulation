package at.borkowski.prefetchsimulation.generator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;

public class Main {
   public static void main(String[] args) throws Exception {
      long totalTicks = 1000 * 3600 * 24;
      int maximumByterate = 1000 * 1000;
      long networkQualityPhaseLength = 1000 * 60 * 3;
      double networkUptime = 0.999;
      double networkByterateStability = 0.85;
      double predictionAccuracy = 0.8;
      Collection<RequestSeries> recurringSeries = new LinkedList<>();
      Collection<IntermittentRequest> intermittentRequests = new LinkedList<>();

      GenesisGenerator generator = new GenesisGenerator(totalTicks, maximumByterate, networkQualityPhaseLength, networkUptime, networkByterateStability, predictionAccuracy, recurringSeries, intermittentRequests);
      generator.seed(123);
      Genesis genesis = generator.generate();

      String outputFile = "/home/michael/tmp-genesis";
      try (OutputStream os = new FileOutputStream(outputFile)) {
         new GenesisWriter(os).write(genesis);
      }
   }
}
