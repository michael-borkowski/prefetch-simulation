package at.borkowski.prefetchsimulation.painter;

import com.xeiam.xchart.Chart;

class PaintResultImpl implements PaintResult {
   private final Chart chart;
   
   PaintResultImpl(Chart chart) {
      this.chart = chart;
   }
   
   @Override
   public Chart getChart() {
      return chart;
   }
}
