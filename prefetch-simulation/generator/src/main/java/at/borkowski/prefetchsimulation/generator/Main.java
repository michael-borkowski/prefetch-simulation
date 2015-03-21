package at.borkowski.prefetchsimulation.generator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.RespectRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.configuration.Configuration;
import at.borkowski.prefetchsimulation.configuration.RequestSeries;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;
import at.borkowski.prefetchsimulation.painter.GenesisPainter;
import at.borkowski.prefetchsimulation.painter.PaintResult;
import at.borkowski.prefetchsimulation.painter.Saver;

public class Main {
   public static void main(String[] args) throws Exception {
      long totalTicks = 1000 * 3600 * 2;
      int maximumByterate = 1000;
      long networkQualityPhaseLength = 1000 * 60 * 3;
      double networkUptime = 0.95;
      double networkByterateStability = 0.95;
      int networkByterateVariability = 20;
      double predictionAccuracy = 0.8;
      Collection<RequestSeries> recurringSeries = new LinkedList<>();
      Collection<Request> intermittentRequests = new LinkedList<>();
      long lookAheadTime = totalTicks;
      Class<? extends PrefetchAlgorithm> algorithm = RespectRatePredictionAlgorithm.class;

      Configuration configuration = new Configuration(totalTicks, maximumByterate, networkQualityPhaseLength, networkUptime, networkByterateStability, networkByterateVariability, predictionAccuracy, recurringSeries, intermittentRequests, algorithm, lookAheadTime);

      GenesisGenerator generator = new GenesisGenerator(configuration);
      if (!configuration.hasSeed())
         generator.seed(314159);
      Genesis genesis = generator.generate();

      String outputFile = "/home/michael/tmp-genesis";
      try (OutputStream os = new FileOutputStream(outputFile)) {
         new GenesisWriter(os).write(genesis);
      }

      PaintResult paint = GenesisPainter.paint(genesis);
      
      Saver.savePDF(paint, outputFile);
      Saver.savePNG(paint, outputFile);
   }
}
