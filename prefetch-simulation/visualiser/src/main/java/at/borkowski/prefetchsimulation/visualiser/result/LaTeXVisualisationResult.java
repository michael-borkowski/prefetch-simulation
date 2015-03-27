package at.borkowski.prefetchsimulation.visualiser.result;

public class LaTeXVisualisationResult {
   private final byte[] bytes;

   public LaTeXVisualisationResult(byte[] bytes) {
      this.bytes = bytes;
   }

   public byte[] getBytes() {
      return bytes;
   }
}
