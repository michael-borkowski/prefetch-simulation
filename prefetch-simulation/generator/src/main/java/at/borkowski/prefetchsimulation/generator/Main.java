package at.borkowski.prefetchsimulation.generator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.borkowski.prefetchsimulation.algorithms.RespectRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.genesis.GenesisWriter;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.BitmapEncoder.BitmapFormat;
import com.xeiam.xchart.Chart;

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
      Collection<IntermittentRequest> intermittentRequests = new LinkedList<>();

      GenesisGenerator generator = new GenesisGenerator(totalTicks, maximumByterate, networkQualityPhaseLength, networkUptime, networkByterateStability, networkByterateVariability, predictionAccuracy, recurringSeries, intermittentRequests, RespectRatePredictionAlgorithm.class);
      generator.seed(314159);
      Genesis genesis = generator.generate();

      String outputFile = "/home/michael/tmp-genesis";
      try (OutputStream os = new FileOutputStream(outputFile)) {
         new GenesisWriter(os).write(genesis);
      }

      List<Long> ticks = new LinkedList<>(genesis.getRateReal().keySet());
      Collections.sort(ticks);

      double[] xData = new double[ticks.size()];
      double[] yData = new double[xData.length];
      int i = 0;
      for (long tick : ticks) {
         xData[i] = tick;
         yData[i] = genesis.getRateReal().get(tick);
         i++;
      }

      Chart chart = new Chart(1920, 1080);
      chart.addSeries("real", xData, yData);

      ticks = new LinkedList<>(genesis.getRatePredicted().keySet());
      Collections.sort(ticks);

      xData = new double[ticks.size() * 2 + 1];
      yData = new double[xData.length];
      i = 1;
      xData[0] = 0;
      yData[0] = genesis.getRatePredicted().get(0L);
      for (long tick : ticks) {
         if (i > 1)
            xData[i - 1] = tick;
         xData[i] = tick;
         yData[i + 1] = yData[i] = genesis.getRatePredicted().get(tick);
         i += 2;
      }
      xData[xData.length - 1] = genesis.getTicks() - 1;

      chart.addSeries("pred", xData, yData);

      chart.getStyleManager().setYAxisMax(maximumByterate);
      chart.getStyleManager().setYAxisMin(0);
      chart.getStyleManager().setYAxisDecimalPattern("#0");

      BitmapEncoder.saveBitmap(chart, outputFile, BitmapFormat.PNG);
   }
}
