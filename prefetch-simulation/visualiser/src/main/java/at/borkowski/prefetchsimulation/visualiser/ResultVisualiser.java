package at.borkowski.prefetchsimulation.visualiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.prefetchsimulation.visualiser.result.LaTeXVisualisationResult;

public class ResultVisualiser {
   public static final double SPAN_OFFSET = -0.9;
   public static final double SPAN_HEIGHT = -0.3;
   public static final double SPAN_PADDING_LEFT = 0.6;
   public static final double SPAN_PADDING_RIGHT = 0.1;
   public static final double SPAN_ARROW_LENGTH = 0.5;
   public static final double SPAN_BOX_PADDING = 0.04;

   public static final double LEGEND_HEIGHT = 2.2;

   public static final String REQ_CACHE_HIT_STYLE = "fill=green!10, draw=green!30,fill opacity=0.5";
   public static final String REQ_CACHE_MISS_STYLE = "fill=yellow!10, draw=yellow!30,fill opacity=0.5";
   public static final String REQ_UNFINISHED_STYLE = "fill=red!10, draw=red!30,fill opacity=0.5";

   public static final String REQ_CACHE_HIT_TEXT_STYLE = "text=green!60";
   public static final String REQ_CACHE_MISS_TEXT_STYLE = "text=orange!40";
   public static final String REQ_UNFINISHED_TEXT_STYLE = "text=red!60";

   private final Genesis genesis;
   private final PrefetchProfilingResults results;
   private final List<String> lines;
   private final double xS, yS;

   private final Map<Request, String> names;
   private final Set<Request> cacheHits;
   private final Map<Request, Integer> requestLevels = new HashMap<>();
   private final Map<Integer, Set<Span>> spans = new HashMap<>();

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

      createSpanLevels(genesis.getRequests());
   }

   LaTeXVisualisationResult visualise() {
      createGenesisRequests(genesis.getRequests());

      genesisVisualiser.createRates();
      createSpans(genesis.getRequests());
      genesisVisualiser.createLegend(LEGEND_HEIGHT, true);
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

   private void createSpanLevels(List<Request> requests) {
      LinkedList<Request> sorted = new LinkedList<>(requests);
      Collections.sort(sorted, new Comparator<Request>() {
         @Override
         public int compare(Request o1, Request o2) {
            return -Long.compare(o1.getDeadline(), o2.getDeadline());
         }
      });

      for (Request request : sorted) {
         Long planFrom = results.getScheduledStart(request);
         long planTo = request.getDeadline();
         Long wasFrom = results.getFetchStart(request);
         Long wasTo = results.getFetchFinish(request);

         long max = max(planFrom, planTo, wasFrom, wasTo);
         long min = min(planFrom, planTo, wasFrom, wasTo);

         double from = GenesisVisualiser.OFFSET_X + xS * min - SPAN_PADDING_LEFT;
         double to = GenesisVisualiser.OFFSET_X + xS * max - SPAN_PADDING_RIGHT;

         if (wasTo == null)
            to += SPAN_ARROW_LENGTH;

         int level = getLevelFor(new Span(from, to));
         requestLevels.put(request, level);
      }
   }

   private int getLevelFor(Span span) {
      int level = 0;
      boolean conflict;
      do {
         conflict = false;
         if (!spans.containsKey(level))
            spans.put(level, new HashSet<>());

         for (Span other : spans.get(level))
            if (other.a < span.a)
               conflict |= other.b > span.a;
            else
               conflict |= span.b > other.a;

         if (conflict)
            level++;
      } while (conflict);
      spans.get(level).add(span);
      return level;
   }

   private long min(Long a, long b, Long c, Long d) {
      long min = b;
      if (a != null && a < min)
         min = a;
      if (c != null && c < min)
         min = c;
      if (d != null && d < min)
         min = d;
      return min;
   }

   private long max(Long a, long b, Long c, Long d) {
      long max = b;
      if (a != null && a > max)
         max = a;
      if (c != null && c > max)
         max = c;
      if (d != null && d > max)
         max = d;
      return max;
   }

   private void enhanceLegend() {
      double y = GenesisVisualiser.LEGEND_Y - GenesisVisualiser.LEGEND_PADDING_Y;
      double box_x = GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH / 2 - GenesisVisualiser.LEGEND_BOX_SIZE / 2;

      lines.add("\\filldraw[" + REQ_CACHE_HIT_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Cache Hit}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;

      lines.add("\\filldraw[" + REQ_CACHE_MISS_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Cache Miss}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;

      lines.add("\\filldraw[" + REQ_UNFINISHED_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + GenesisVisualiser.LEGEND_BOX_SIZE) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + (y - GenesisVisualiser.LEGEND_BOX_SIZE / 2) + ") {\\tiny{Unfinished}};");
      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT;

      double xspace = GenesisVisualiser.LEGEND_COL1_WIDTH - GenesisVisualiser.LEGEND_PADDING_X;
      double x1 = GenesisVisualiser.LEGEND_X + 1.5 * GenesisVisualiser.LEGEND_PADDING_X + 0 * xspace / 3;
      double x2 = GenesisVisualiser.LEGEND_X + 1.5 * GenesisVisualiser.LEGEND_PADDING_X + 1 * xspace / 3;
      double x3 = GenesisVisualiser.LEGEND_X + 1.5 * GenesisVisualiser.LEGEND_PADDING_X + 2 * xspace / 3;
      double x4 = GenesisVisualiser.LEGEND_X + 1.5 * GenesisVisualiser.LEGEND_PADDING_X + 3 * xspace / 3;

      y -= 2.0 * GenesisVisualiser.LEGEND_ROW_HEIGHT;

      double yLine = y - GenesisVisualiser.LEGEND_BOX_SIZE / 2;

      lines.add("\\filldraw[fill=black!5!white, draw=black!20!white, opacity=0.3] (" + (x1 - SPAN_BOX_PADDING) + "," + (yLine - GenesisVisualiser.TICK_LENGTH - SPAN_BOX_PADDING) + ") rectangle (" + (x4 + SPAN_BOX_PADDING) + "," + (yLine + GenesisVisualiser.TICK_LENGTH + SPAN_BOX_PADDING) + ");");
      lines.add("\\draw[opacity=0.1] (" + x1 + "," + (yLine + GenesisVisualiser.TICK_LENGTH) + ") -- (" + x1 + "," + (yLine - GenesisVisualiser.TICK_LENGTH) + ");");
      lines.add("\\draw[opacity=0.1] (" + x4 + "," + (yLine + GenesisVisualiser.TICK_LENGTH) + ") -- (" + x4 + "," + (yLine - GenesisVisualiser.TICK_LENGTH) + ");");
      lines.add("\\draw[opacity=0.1] (" + x1 + "," + yLine + ") -- (" + x4 + "," + yLine + ");");
      lines.add("\\draw (" + x2 + "," + (yLine + GenesisVisualiser.TICK_LENGTH) + ") -- (" + x2 + "," + (yLine - GenesisVisualiser.TICK_LENGTH) + ");");
      lines.add("\\draw (" + x3 + "," + (yLine + GenesisVisualiser.TICK_LENGTH) + ") -- (" + x3 + "," + (yLine - GenesisVisualiser.TICK_LENGTH) + ");");
      lines.add("\\draw (" + x2 + "," + yLine + ") -- (" + x3 + "," + yLine + ");");
      lines.add("\\node[anchor=west] at (" + (GenesisVisualiser.LEGEND_X + GenesisVisualiser.LEGEND_PADDING_X + GenesisVisualiser.LEGEND_COL1_WIDTH) + "," + yLine + ") {\\tiny{Request Timing}};");

      y -= GenesisVisualiser.LEGEND_ROW_HEIGHT * 1.7;
      lines.add("\\node[anchor=south] at (" + x1 + "," + y + ") {\\tiny{$\\lambda$}};");
      lines.add("\\node[anchor=south] at (" + x4 + "," + y + ") {\\tiny{$\\tau$}};");
      lines.add("\\node[anchor=south] at (" + (x2 + x3) / 2 + "," + y + ") {\\scalebox{.4}{transmit}};");

   }

   private void createSpans(List<Request> requests) {
      for (Request request : requests) {
         int level = requestLevels.get(request);

         Long planFrom = results.getScheduledStart(request);
         long planTo = request.getDeadline();
         Long wasFrom = results.getFetchStart(request);
         Long wasTo = results.getFetchFinish(request);
         
         boolean arrow = wasFrom != null && wasTo == null;

         long min = min(planFrom, planTo, wasFrom, wasTo);
         double xMin = GenesisVisualiser.OFFSET_X + xS * min;
         long max = max(planFrom, planTo, wasFrom, wasTo);
         double xMax = GenesisVisualiser.OFFSET_X + xS * max + (arrow ? SPAN_ARROW_LENGTH : 0);

         double yLevel = SPAN_OFFSET + SPAN_HEIGHT * level;
         
         lines.add("\\filldraw[fill=black!5!white, draw=black!20!white, opacity=0.3] (" + (xMin - SPAN_BOX_PADDING) + "," + (yLevel - GenesisVisualiser.TICK_LENGTH - SPAN_BOX_PADDING) + ") rectangle (" + (xMax + SPAN_BOX_PADDING) + "," + (yLevel + GenesisVisualiser.TICK_LENGTH + SPAN_BOX_PADDING) + ");");

         double xPlanTo = GenesisVisualiser.OFFSET_X + xS * planTo;
         lines.add("\\draw[opacity=0.1] (" + xPlanTo + "," + (yLevel - GenesisVisualiser.TICK_LENGTH) + ") -- (" + xPlanTo + "," + (yLevel + GenesisVisualiser.TICK_LENGTH) + ");"); // node[anchor=south] {\\tiny$\\tau$};");

         if (planFrom != null) {
            double xPlanFrom = GenesisVisualiser.OFFSET_X + xS * planFrom;
            lines.add("\\draw[opacity=0.1] (" + xPlanFrom + "," + (yLevel - GenesisVisualiser.TICK_LENGTH) + ") -- (" + xPlanFrom + "," + (yLevel + GenesisVisualiser.TICK_LENGTH) + ");"); // node[anchor=south] {\\tiny$\\lambda$};");
            lines.add("\\draw[opacity=0.1] (" + xPlanFrom + "," + yLevel + ") -- (" + xPlanTo + "," + yLevel + ");");
         }

         if (wasFrom != null) {
            double xWasFrom = GenesisVisualiser.OFFSET_X + xS * wasFrom;
            lines.add("\\draw (" + xWasFrom + "," + (yLevel - GenesisVisualiser.TICK_LENGTH) + ") -- (" + xWasFrom + "," + (yLevel + GenesisVisualiser.TICK_LENGTH) + ");");
         }
         if (wasTo != null) {
            double xWasTo = GenesisVisualiser.OFFSET_X + xS * wasTo;
            lines.add("\\draw (" + xWasTo + "," + (yLevel - GenesisVisualiser.TICK_LENGTH) + ") -- (" + xWasTo + "," + (yLevel + GenesisVisualiser.TICK_LENGTH) + ");");
         }
         if (wasFrom != null && wasTo != null) {
            double xWasFrom = GenesisVisualiser.OFFSET_X + xS * wasFrom;
            double xWasTo = GenesisVisualiser.OFFSET_X + xS * wasTo;
            lines.add("\\draw (" + xWasFrom + "," + yLevel + ") -- (" + xWasTo + "," + yLevel + ");");
         }
         
         if (arrow) {
            double xWasFrom = GenesisVisualiser.OFFSET_X + xS * wasFrom;
            double xArrowTo = xPlanTo > xWasFrom ? xPlanTo : xWasFrom;
            lines.add("\\draw[->] (" + xWasFrom + "," + yLevel + ") -- (" + (xArrowTo + SPAN_ARROW_LENGTH) + "," + yLevel + ");");
         }

         lines.add("\\node[anchor=east] at (" + xMin + "," + yLevel + ") {\\tiny\\texttt{" + names.get(request) + "}};");
      }
   }

   private void createGenesisRequests(Collection<Request> requests) {
      for (Request request : requests) {
         double xStart = GenesisVisualiser.OFFSET_X + xS * (request.getDeadline() - (double) request.getData() / request.getAvailableByterate());
         double xEnd = GenesisVisualiser.OFFSET_X + xS * request.getDeadline();
         double xMid = (xStart + xEnd) / 2;
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
         lines.add("\\filldraw[" + style + "] (" + xStart + "," + GenesisVisualiser.OFFSET_Y + ") rectangle (" + xEnd + "," + yTop + ");");
         lines.add("\\node[anchor=south," + textStyle + "] at (" + xMid + "," + yTop + ") {\\tiny\\texttt{" + names.get(request) + "}};");
      }
   }

   private boolean isTolerable(Request request) {
      if (cacheHits.contains(request))
         return true;
      if (results.getFetchFinish(request) == null)
         return false;
      return true;
   }

   private class Span {
      public double a, b;

      public Span(double a, double b) {
         this.a = a;
         this.b = b;
      }
   }
}
