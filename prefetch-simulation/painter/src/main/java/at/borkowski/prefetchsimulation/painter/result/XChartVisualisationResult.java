package at.borkowski.prefetchsimulation.painter.result;

import com.xeiam.xchart.Chart;

public class XChartVisualisationResult extends VisualisationResult {
   private final Chart chart;
   
   public XChartVisualisationResult(Chart chart) {
      this.chart = chart;
   }
   
   public Chart getChart() {
      return chart;
   }
}
