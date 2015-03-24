package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;
import java.io.OutputStream;

import at.borkowski.prefetchsimulation.painter.result.LaTeXVisualisationResult;
import at.borkowski.prefetchsimulation.painter.result.XChartVisualisationResult;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.BitmapEncoder.BitmapFormat;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.VectorGraphicsEncoder;
import com.xeiam.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;

import de.erichseifert.vectorgraphics2d.EPSGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;

public class Saver {
   public static void savePNG(XChartVisualisationResult result, String fileName) throws IOException {
      BitmapEncoder.saveBitmap(result.getChart(), fileName, BitmapFormat.PNG);
   }

   public static void saveSVG(XChartVisualisationResult result, String fileName) throws IOException {
      VectorGraphicsEncoder.saveVectorGraphic(result.getChart(), fileName, VectorGraphicsFormat.SVG);
   }

   public static void saveEPS(XChartVisualisationResult result, String fileName) throws IOException {
      VectorGraphicsEncoder.saveVectorGraphic(result.getChart(), fileName, VectorGraphicsFormat.EPS);
   }

   public static void savePNG(XChartVisualisationResult result, OutputStream out) throws IOException {
      out.write(BitmapEncoder.getBitmapBytes(result.getChart(), BitmapFormat.PNG));
   }

   public static void saveEPS(XChartVisualisationResult result, OutputStream out) throws IOException {
      // adapted code from com.xeiam.xchart.VectorGraphicsEncoder (since that class had no means of writing to a stream instead of a file)
      Chart chart = result.getChart();
      VectorGraphics2D g = new EPSGraphics2D(0.0, 0.0, chart.getWidth(), chart.getHeight());
      chart.paint(g, chart.getWidth(), chart.getHeight());
      out.write(g.getBytes());
      g.dispose();
   }
   
   public static void saveLaTeX(LaTeXVisualisationResult result, OutputStream out) throws IOException {
      byte[] bytes = result.getBytes();
      out.write(bytes);
   }
}
