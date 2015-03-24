package at.borkowski.prefetchsimulation.painter.result;

public class LaTeXVisualisationResult extends VisualisationResult {
   private final byte[] bytes;

   public LaTeXVisualisationResult(byte[] bytes) {
      this.bytes = bytes;
   }

   public byte[] getBytes() {
      return bytes;
   }
}