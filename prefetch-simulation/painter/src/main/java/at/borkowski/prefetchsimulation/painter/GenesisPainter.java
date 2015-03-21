package at.borkowski.prefetchsimulation.painter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.borkowski.prefetchsimulation.genesis.Genesis;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.SeriesMarker;

public class GenesisPainter {

   public static PaintResult paint(Genesis genesis) {
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
      
      chart.getSeriesMap().get("pred").setMarker(SeriesMarker.NONE);
      chart.getSeriesMap().get("real").setMarker(SeriesMarker.NONE);

      chart.getStyleManager().setYAxisMin(0);
      chart.getStyleManager().setYAxisDecimalPattern("#0");

      return new PaintResultImpl(chart);
   }

}
