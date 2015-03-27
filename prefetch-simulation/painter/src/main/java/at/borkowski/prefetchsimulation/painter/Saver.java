package at.borkowski.prefetchsimulation.painter;

import java.io.IOException;
import java.io.OutputStream;

import at.borkowski.prefetchsimulation.painter.result.LaTeXVisualisationResult;

public class Saver {
   public static void saveLaTeX(LaTeXVisualisationResult result, OutputStream out) throws IOException {
      byte[] bytes = result.getBytes();
      out.write(bytes);
   }
}
