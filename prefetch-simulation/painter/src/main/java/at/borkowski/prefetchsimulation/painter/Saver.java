package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;
import java.io.OutputStream;

import at.borkowski.prefetchsimulation.painter.result.VisualisationResult;
import at.borkowski.prefetchsimulation.painter.result.XChartVisualisationResultImpl;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.BitmapEncoder.BitmapFormat;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.VectorGraphicsEncoder;
import com.xeiam.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;

import de.erichseifert.vectorgraphics2d.EPSGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;

public class Saver {
   public static void savePNG(VisualisationResult result, String fileName) throws IOException {
      if (!(result instanceof XChartVisualisationResultImpl))
         throw new IllegalArgumentException("invalid kind of paint result");
      BitmapEncoder.saveBitmap(((XChartVisualisationResultImpl) result).getChart(), fileName, BitmapFormat.PNG);
   }

   public static void saveSVG(VisualisationResult result, String fileName) throws IOException {
      if (!(result instanceof XChartVisualisationResultImpl))
         throw new IllegalArgumentException("invalid kind of paint result");
      VectorGraphicsEncoder.saveVectorGraphic(((XChartVisualisationResultImpl) result).getChart(), fileName, VectorGraphicsFormat.SVG);
   }

   public static void saveEPS(VisualisationResult result, String fileName) throws IOException {
      if (!(result instanceof XChartVisualisationResultImpl))
         throw new IllegalArgumentException("invalid kind of paint result");
      VectorGraphicsEncoder.saveVectorGraphic(((XChartVisualisationResultImpl) result).getChart(), fileName, VectorGraphicsFormat.EPS);
   }

   public static void savePNG(VisualisationResult result, OutputStream out) throws IOException {
      if (!(result instanceof XChartVisualisationResultImpl))
         throw new IllegalArgumentException("invalid kind of paint result");
      out.write(BitmapEncoder.getBitmapBytes(((XChartVisualisationResultImpl) result).getChart(), BitmapFormat.PNG));
   }

   public static void saveEPS(VisualisationResult result, OutputStream out) throws IOException {
      if (!(result instanceof XChartVisualisationResultImpl))
         throw new IllegalArgumentException("invalid kind of paint result");
      
      // adapted code from com.xeiam.xchart.VectorGraphicsEncoder (since that class had no means of writing to a stream instead of a file)
      Chart chart = ((XChartVisualisationResultImpl) result).getChart();
      VectorGraphics2D g = new EPSGraphics2D(0.0, 0.0, chart.getWidth(), chart.getHeight());
      chart.paint(g, chart.getWidth(), chart.getHeight());
      out.write(g.getBytes());
      g.dispose();
   }
}
