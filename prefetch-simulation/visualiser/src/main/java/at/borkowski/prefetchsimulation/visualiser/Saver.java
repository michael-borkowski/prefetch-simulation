package at.borkowski.prefetchsimulation.visualiser;

import java.io.IOException;
import java.io.OutputStream;

import at.borkowski.prefetchsimulation.visualiser.result.LaTeXVisualisationResult;

public class Saver {
   public static void saveLaTeX(LaTeXVisualisationResult result, OutputStream out) throws IOException {
      byte[] bytes = result.getBytes();
      out.write(bytes);
   }
}
