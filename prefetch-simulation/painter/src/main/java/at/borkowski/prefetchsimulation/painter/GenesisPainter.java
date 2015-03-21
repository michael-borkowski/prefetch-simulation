package at.borkowski.prefetchsimulation.painter;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.genesis.Genesis;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager;
import com.xeiam.xchart.StyleManager.ChartTheme;
import com.xeiam.xchart.StyleManager.LegendPosition;

public class GenesisPainter {

   public static PaintResult paint(Genesis genesis) {
      List<Long> ticks = new LinkedList<>(genesis.getRateReal().keySet());
      Collections.sort(ticks);

      double[] xDataReal = new double[ticks.size()];
      double[] yDataReal = new double[xDataReal.length];
      int i = 0;
      for (long tick : ticks) {
         xDataReal[i] = tick;
         yDataReal[i] = genesis.getRateReal().get(tick);
         i++;
      }

      ticks = new LinkedList<>(genesis.getRatePredicted().keySet());
      Collections.sort(ticks);

      double[] xDataPredicted = new double[ticks.size() * 2 + 1];
      double[] yDataPredicted = new double[xDataPredicted.length];
      i = 1;
      xDataPredicted[0] = 0;
      yDataPredicted[0] = genesis.getRatePredicted().get(0L);
      for (long tick : ticks) {
         if (i > 1)
            xDataPredicted[i - 1] = tick;
         xDataPredicted[i] = tick;
         yDataPredicted[i + 1] = yDataPredicted[i] = genesis.getRatePredicted().get(tick);
         i += 2;
      }
      xDataPredicted[xDataPredicted.length - 1] = genesis.getTicks() - 1;
      
      List<Request> requests = genesis.getRequests();
      double[] xDataRequests = new double[requests.size()];
      double[] yDataRequests = new double[xDataRequests.length];
      i = 0;
      for(Request request : requests) {
         xDataRequests[i] = request.getDeadline();
         yDataRequests[i] = 0; //request.getAvailableByterate();
         i++;
      }

      Chart chart = new ChartBuilder().width(800).height(600).theme(ChartTheme.Matlab).build();
      chart.setXAxisTitle("ticks");
      chart.setYAxisTitle("bandwidth");

      Series seriesReal = chart.addSeries("B real", xDataReal, yDataReal);
      Series seriesPred = chart.addSeries("B predicted", xDataPredicted, yDataPredicted);
      Series seriesRequests = chart.addSeries("Requests", xDataRequests, yDataRequests);

      seriesReal.setLineColor(Color.BLUE);
      seriesPred.setMarker(SeriesMarker.NONE);
      
      seriesPred.setLineColor(Color.ORANGE);
      seriesReal.setMarker(SeriesMarker.NONE);
      
      seriesRequests.setLineStyle(SeriesLineStyle.NONE);
      seriesRequests.setMarkerColor(Color.GREEN);
      seriesRequests.setMarker(SeriesMarker.CIRCLE);
      
      StyleManager style = chart.getStyleManager();
      style.setChartBackgroundColor(Color.WHITE);

      style.setYAxisMin(0);
      style.setYAxisDecimalPattern("#0");
      style.setLegendPosition(LegendPosition.InsideNE);

      return new PaintResultImpl(chart);
   }

}
