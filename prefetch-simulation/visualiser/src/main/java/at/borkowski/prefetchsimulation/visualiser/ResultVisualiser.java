package at.borkowski.prefetchsimulation.visualiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.prefetchsimulation.visualiser.result.LaTeXVisualisationResult;

public class ResultVisualiser {
   public static final double LEGEND_HEIGHT = 2.1;

   public static final String REQ_CACHE_HIT_STYLE = "draw=green!80!black, opacity=0.9, thick";
   public static final String REQ_CACHE_MISS_STYLE = "draw=yellow!80!black, opacity=0.9, thick";
   public static final String REQ_UNFINISHED_STYLE = "draw=red!80!black, opacity=0.9, thick";

   public static final String REQ_CACHE_HIT_TEXT_STYLE = "text=green!80!black";
   public static final String REQ_CACHE_MISS_TEXT_STYLE = "text=yellow!80!black";
   public static final String REQ_UNFINISHED_TEXT_STYLE = "text=red!80!black";

   private final Genesis genesis;
   private final PrefetchProfilingResults results;
   private final List<String> lines;
   private final double xS, yS;

   private final Map<Request, String> names;
   private final Set<Request> cacheHits;

   private final GenesisVisualiser genesisVisualiser;

   public static LaTeXVisualisationResult visualise(Genesis genesis, PrefetchProfilingResults results) {
      return new ResultVisualiser(genesis, results).visualise();
   }

   ResultVisualiser(Genesis genesis, PrefetchProfilingResults results) {
      this.genesis = genesis;
      this.results = results;

      this.genesisVisualiser = new GenesisVisualiser(genesis);
      this.lines = genesisVisualiser.lines;

      genesisVisualiser.createBase();

      xS = genesisVisualiser.xS;
      yS = genesisVisualiser.yS;

      names = genesisVisualiser.names;
      cacheHits = results.getCacheHitRequests();
   }

   LaTeXVisualisationResult visualise() {
      createGenesisRequests(genesis.getRequests());

      genesisVisualiser.createRates();
      genesisVisualiser.createLegend(LEGEND_HEIGHT, true, "Actual Request Fetching");
      enhanceLegend();
      genesisVisualiser.createFinish();

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintWriter pw = new PrintWriter(baos)) {
         for (String line : lines)
            pw.println(line);
         pw.flush();
         return new LaTeXVisualisationResult(baos.toByteArray());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void enhanceLegend() {
      double y = GenesisVisualiser.LEGEND_Y - GenesisVisualiser.LEGEND_PADDING_Y;
      double box_x = GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH / 2 - GenesisVisualiser.LEGEND_BOX_SIZE / 2;

      lines.add("\\draw[" + REQ_CACHE_HIT_STYLE + "] (" + box_x + "," + y + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\draw[" + REQ_CACHE_HIT_STYLE + "] (" + box_x + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + y + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Satisfied Deadline}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;

      lines.add("\\draw[" + REQ_CACHE_MISS_STYLE + "] (" + box_x + "," + y + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\draw[" + REQ_CACHE_MISS_STYLE + "] (" + box_x + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + y + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Missed Deadline}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;

      lines.add("\\draw[" + REQ_UNFINISHED_STYLE + "] (" + box_x + "," + y + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\draw[" + REQ_UNFINISHED_STYLE + "] (" + box_x + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ") -- (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + y + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Unfinished}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;
   }

   private void createGenesisRequests(Collection<Request> requests) {
      for (Request request : requests) {
         Long wasFrom = results.getFetchStart(request);
         Long wasTo = results.getFetchFinish(request);

         boolean unfinished = wasFrom != null && wasTo == null;

         if (wasFrom == null)
            continue;
         double xWasFrom = GenesisVisualiser.OFFSET_X + xS * wasFrom;

         if (unfinished)
            wasTo = genesis.getTicks();

         long currentTick = wasFrom;
         double currentBw = Math.min(request.getAvailableByterate(), byteRateAt(currentTick));
         double currentY = GenesisVisualiser.OFFSET_Y + yS * currentBw;

         lines.add("\\draw [" + GenesisVisualiser.REQ_STYLE + "] (" + xWasFrom + "," + GenesisVisualiser.OFFSET_Y + ") -- (" + xWasFrom + "," + currentY + ")");

         while (currentTick <= wasTo) {
            currentTick++;
            double newBw = Math.min(request.getAvailableByterate(), byteRateAt(currentTick));
            if (newBw != currentBw) {
               double newY = GenesisVisualiser.OFFSET_Y + yS * newBw;
               double tickX = GenesisVisualiser.OFFSET_X + xS * currentTick;
               lines.add("-- (" + tickX + "," + currentY + ") -- (" + tickX + "," + newY + ")");
               currentBw = newBw;
               currentY = newY;
            }
         }

         double tickX = GenesisVisualiser.OFFSET_X + xS * currentTick;
         lines.add("-- (" + tickX + "," + currentY + ") -- (" + tickX + "," + currentY + ")");
         lines.add("-- (" + tickX + "," + GenesisVisualiser.OFFSET_Y + ")");
         lines.add("-- cycle;");

         double xMid = 0.5 * (xWasFrom + tickX);

         lines.add("\\node [anchor=south," + GenesisVisualiser.REQ_TEXT_STYLE + "] at (" + xMid + "," + (GenesisVisualiser.OFFSET_Y + GenesisVisualiser.TICK_LENGTH) + ") {\\tiny\\texttt{" + names.get(request) + "}};");
         lines.add("\\draw [" + GenesisVisualiser.REQ_STYLE + ", <->, densely dotted] (" + xWasFrom + ", " + (GenesisVisualiser.OFFSET_Y + 2 * GenesisVisualiser.TICK_LENGTH) + ") -- (" + tickX + ", " + (GenesisVisualiser.OFFSET_Y + 2 * GenesisVisualiser.TICK_LENGTH) + ");");
      }

      for (Request request : requests) {
         double xEnd = GenesisVisualiser.OFFSET_X + xS * request.getDeadline();
         double yTop = GenesisVisualiser.OFFSET_Y + yS * request.getAvailableByterate();

         String style, textStyle;
         if (cacheHits.contains(request)) {
            style = REQ_CACHE_HIT_STYLE;
            textStyle = REQ_CACHE_HIT_TEXT_STYLE;
         } else if (isTolerable(request)) {
            style = REQ_CACHE_MISS_STYLE;
            textStyle = REQ_CACHE_MISS_TEXT_STYLE;
         } else {
            style = REQ_UNFINISHED_STYLE;
            textStyle = REQ_UNFINISHED_TEXT_STYLE;
         }
         lines.add("\\node[anchor=south," + textStyle + "] at (" + xEnd + "," + yTop + ") {\\tiny\\texttt{" + names.get(request) + "}};");
         lines.add("\\draw[" + style + "] (" + (xEnd - GenesisVisualiser.TICK_LENGTH) + ", " + (yTop - GenesisVisualiser.TICK_LENGTH) + ") -- (" + (xEnd + GenesisVisualiser.TICK_LENGTH) + ", " + (yTop + GenesisVisualiser.TICK_LENGTH) + ");");
         lines.add("\\draw[" + style + "] (" + (xEnd - GenesisVisualiser.TICK_LENGTH) + ", " + (yTop + GenesisVisualiser.TICK_LENGTH) + ") -- (" + (xEnd + GenesisVisualiser.TICK_LENGTH) + ", " + (yTop - GenesisVisualiser.TICK_LENGTH) + ");");
      }
   }

   private double byteRateAt(long tick) {
      List<Long> ticks = new LinkedList<>(genesis.getRateReal().keySet());
      Collections.sort(ticks);

      Long prev = null;
      for (long t : ticks)
         if (t > tick)
            break;
         else
            prev = t;

      if (prev == null)
         throw new RuntimeException("limited bandwidth expected");
      return genesis.getRateReal().get(prev);
   }

   private boolean isTolerable(Request request) {
      if (cacheHits.contains(request))
         return true;
      if (results.getFetchFinish(request) == null)
         return false;
      return true;
   }
}
