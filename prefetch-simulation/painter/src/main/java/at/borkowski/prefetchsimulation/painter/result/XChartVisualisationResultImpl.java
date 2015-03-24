package at.borkowski.prefetchsimulation.painter.result;

import com.xeiam.xchart.Chart;

public class XChartVisualisationResultImpl extends VisualisationResult {
   private final Chart chart;
   
   public XChartVisualisationResultImpl(Chart chart) {
      this.chart = chart;
   }
   
   public Chart getChart() {
      return chart;
   }
}
