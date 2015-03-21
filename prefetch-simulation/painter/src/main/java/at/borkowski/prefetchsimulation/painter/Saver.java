package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.BitmapEncoder.BitmapFormat;
import com.xeiam.xchart.VectorGraphicsEncoder;
import com.xeiam.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;

public class Saver {
   public static void savePNG(PaintResult result, String fileName) throws IOException {
      BitmapEncoder.saveBitmap(result.getChart(), fileName, BitmapFormat.PNG);
   }
   
   public static void savePDF(PaintResult result, String fileName) throws IOException {
      VectorGraphicsEncoder.saveVectorGraphic(result.getChart(), fileName, VectorGraphicsFormat.PDF);
   }
}
