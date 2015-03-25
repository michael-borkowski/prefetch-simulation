package at.borkowski.prefetchsimulation.painter;

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
import at.borkowski.prefetchsimulation.painter.result.LaTeXVisualisationResult;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;

public class ResultVisualiser {
   public static final double DIAGRAM_WIDTH = 15;
   public static final double DIAGRAM_HEIGHT = 10;

   public static final double OFFSET_X = 0.2;
   public static final double OFFSET_Y = 0.2;
   public static final double HEADSPACE_X = 0.6;
   public static final double HEADSPACE_Y = 1;

   public static final double TICK_LENGTH = 0.1;

   public static final double SPAN_OFFSET = -0.9;
   public static final double SPAN_HEIGHT = -0.3;
   public static final double SPAN_PADDING_LEFT = 0.6;
   public static final double SPAN_PADDING_RIGHT = 0.1;
   public static final double SPAN_ARROW_LENGTH = 0.5;

   public static final String STYLE_REAL = "blue";
   public static final String STYLE_PREDICTED = "blue,densely dotted";

   public static final double LEGEND_WIDTH = 3.53;
   public static final double LEGEND_HEIGHT = 2.0;
   public static final double LEGEND_X = 12;
   public static final double LEGEND_Y = 8.3;

   public static final double LEGEND_PADDING_X = 0.2;
   public static final double LEGEND_PADDING_Y = 0.2;
   public static final double LEGEND_BOX_SIZE = 0.2;
   public static final double LEGEND_COL1_WIDTH = 1.4;
   public static final double LEGEND_ROW_HEIGHT = 0.3;

   public static final String REQ_CACHE_HIT_STYLE = "fill=green!10, draw=green!30,fill opacity=0.5";
   public static final String REQ_CACHE_MISS_STYLE = "fill=yellow!10, draw=yellow!30,fill opacity=0.5";
   public static final String REQ_UNFINISHED_STYLE = "fill=red!10, draw=red!30,fill opacity=0.5";

   public static final String REQ_CACHE_HIT_TEXT_STYLE = "text=green!60";
   public static final String REQ_CACHE_MISS_TEXT_STYLE = "text=orange!40";
   public static final String REQ_UNFINISHED_TEXT_STYLE = "text=red!60";

   private final Genesis genesis;
   private final PrefetchProfilingResults results;
   private final List<String> lines;
   private final long xMax, yMax;
   private final double xS, yS, xTickStep, yTickStep;

   private int nameCounter = 0;
   private final Map<Request, String> names = new HashMap<>();
   private final Set<Request> cacheHits;
   private final Map<Request, Integer> requestLevels = new HashMap<>();
   private final Map<Integer, Set<Span>> spans = new HashMap<>();

   public static LaTeXVisualisationResult visualise(Genesis genesis, PrefetchProfilingResults results) {
      return new ResultVisualiser(genesis, results).visualise();
   }

   ResultVisualiser(Genesis genesis, PrefetchProfilingResults results) {
      this.genesis = genesis;
      this.results = results;

      lines = new LinkedList<>();

      xMax = genesis.getTicks();
      yMax = getMaxByterate(genesis);

      long xTickInterval = calcXTickInterval(xMax);
      long yTickInterval = calcYTickInterval(yMax);

      xS = (double) (DIAGRAM_WIDTH - OFFSET_X - HEADSPACE_X) / xMax;
      yS = (double) (DIAGRAM_HEIGHT - 2 * OFFSET_Y - HEADSPACE_Y) / yMax;

      xTickStep = xTickInterval * xS;
      yTickStep = yTickInterval * yS;

      createNames(genesis.getRequests());
      cacheHits = results.getCacheHitRequests();

      createSpanLevels(genesis.getRequests());

      createHeader();
      createSystem(xTickInterval, yTickInterval);
   }

   LaTeXVisualisationResult visualise() {
      createGenesisRequests(genesis.getRequests());

      createRates(genesis.getRateReal(), STYLE_REAL);
      createRates(genesis.getRatePredicted(), STYLE_PREDICTED);

      createSpans(genesis.getRequests());

      createLegend();

      createFooter();

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

         double from = OFFSET_X + xS * min - SPAN_PADDING_LEFT;
         double to = OFFSET_X + xS * max - SPAN_PADDING_RIGHT;

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

   private void createNames(List<Request> requests) {
      LinkedList<Request> sorted = new LinkedList<>(requests);
      Collections.sort(sorted, new Comparator<Request>() {
         @Override
         public int compare(Request o1, Request o2) {
            return Long.compare(o1.getDeadline(), o2.getDeadline());
         }
      });
      for (Request request : sorted)
         names.put(request, createName(request));
   }

   private String createName(Request request) {
      String s = Integer.toHexString(nameCounter++);
      while (s.length() < 3)
         s = '0' + s;
      return s.substring(0, 3);
   }

   private void createLegend() {
      lines.add("\\filldraw[fill=white, draw=black] (" + LEGEND_X + "," + LEGEND_Y + ") rectangle (" + (LEGEND_X + LEGEND_WIDTH) + "," + (LEGEND_Y + LEGEND_HEIGHT) + ");");

      double y = LEGEND_Y + LEGEND_HEIGHT - LEGEND_PADDING_Y;
      double box_x = LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH / 2 - LEGEND_BOX_SIZE / 2;

      lines.add("\\filldraw[" + REQ_CACHE_HIT_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + LEGEND_BOX_SIZE) + "," + (y - LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Cache Hit}};");
      y -= LEGEND_ROW_HEIGHT;

      lines.add("\\filldraw[" + REQ_CACHE_MISS_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + LEGEND_BOX_SIZE) + "," + (y - LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Cache Miss}};");
      y -= LEGEND_ROW_HEIGHT;

      lines.add("\\filldraw[" + REQ_UNFINISHED_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + LEGEND_BOX_SIZE) + "," + (y - LEGEND_BOX_SIZE) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Unfinished}};");
      y -= LEGEND_ROW_HEIGHT;

      double xspace = LEGEND_COL1_WIDTH - LEGEND_PADDING_X;
      double x1 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 0 * xspace / 3;
      double x2 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 1 * xspace / 3;
      double x3 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 2 * xspace / 3;
      double x4 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 3 * xspace / 3;

      lines.add("\\draw[" + STYLE_REAL + "] (" + x2 + "," + (y - LEGEND_BOX_SIZE / 2) + ") -- (" + x3 + "," + (y - LEGEND_BOX_SIZE / 2) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Real Bandwidth}};");
      y -= LEGEND_ROW_HEIGHT;
      lines.add("\\draw[" + STYLE_PREDICTED + "] (" + x2 + "," + (y - LEGEND_BOX_SIZE / 2) + ") -- (" + x3 + "," + (y - LEGEND_BOX_SIZE / 2) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Prediction}};");
      y -= LEGEND_ROW_HEIGHT;

      lines.add("\\draw[densely dotted] (" + x1 + "," + (y + TICK_LENGTH) + ") -- (" + x1 + "," + (y - TICK_LENGTH) + ");");
      lines.add("\\draw[densely dotted] (" + x4 + "," + (y + TICK_LENGTH) + ") -- (" + x4 + "," + (y - TICK_LENGTH) + ");");
      lines.add("\\draw[densely dotted] (" + x1 + "," + y + ") -- (" + x4 + "," + y + ");");
      lines.add("\\draw (" + x2 + "," + (y + TICK_LENGTH) + ") -- (" + x2 + "," + (y - TICK_LENGTH) + ");");
      lines.add("\\draw (" + x3 + "," + (y + TICK_LENGTH) + ") -- (" + x3 + "," + (y - TICK_LENGTH) + ");");
      lines.add("\\draw (" + x2 + "," + y + ") -- (" + x3 + "," + y + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Request Timing}};");

      y -= LEGEND_ROW_HEIGHT * 1.2;
      lines.add("\\node[anchor=south] at (" + x1 + "," + y + ") {\\tiny{$\\lambda$}};");
      lines.add("\\node[anchor=south] at (" + x4 + "," + y + ") {\\tiny{$\\tau$}};");
      lines.add("\\node[anchor=south] at (" + (x2 + x3) / 2 + "," + y + ") {\\scalebox{.4}{transmit}};");

   }

   private void createHeader() {
      lines.add("\\documentclass{standalone}");
      lines.add("\\usepackage{tikz}");
      lines.add("\\usepackage{amsmath}");
      lines.add("\\usepackage[utf8]{inputenc}");
      lines.add("\\begin{document}");
      lines.add("\\begin{tikzpicture}");
   }

   private void createFooter() {
      lines.add("\\end{tikzpicture}");
      lines.add("\\end{document}");
   }

   private void createSpans(List<Request> requests) {
      for (Request request : requests) {
         int level = requestLevels.get(request);

         Long planFrom = results.getScheduledStart(request);
         long planTo = request.getDeadline();
         Long wasFrom = results.getFetchStart(request);
         Long wasTo = results.getFetchFinish(request);

         long min = min(planFrom, planTo, wasFrom, wasTo);
         double xMin = OFFSET_X + xS * min;

         double yLevel = SPAN_OFFSET + SPAN_HEIGHT * level;

         double xPlanTo = OFFSET_X + xS * planTo;
         lines.add("\\draw[densely dotted] (" + xPlanTo + "," + (yLevel - TICK_LENGTH) + ") -- (" + xPlanTo + "," + (yLevel + TICK_LENGTH) + ");"); // node[anchor=south] {\\tiny$\\tau$};");

         if (planFrom != null) {
            double xPlanFrom = OFFSET_X + xS * planFrom;
            lines.add("\\draw[densely dotted] (" + xPlanFrom + "," + (yLevel - TICK_LENGTH) + ") -- (" + xPlanFrom + "," + (yLevel + TICK_LENGTH) + ");"); // node[anchor=south] {\\tiny$\\lambda$};");
            lines.add("\\draw[densely dotted] (" + xPlanFrom + "," + yLevel + ") -- (" + xPlanTo + "," + yLevel + ");");
         }

         if (wasFrom != null) {
            double xWasFrom = OFFSET_X + xS * wasFrom;
            lines.add("\\draw (" + xWasFrom + "," + (yLevel - TICK_LENGTH) + ") -- (" + xWasFrom + "," + (yLevel + TICK_LENGTH) + ");");
         }
         if (wasTo != null) {
            double xWasTo = OFFSET_X + xS * wasTo;
            lines.add("\\draw (" + xWasTo + "," + (yLevel - TICK_LENGTH) + ") -- (" + xWasTo + "," + (yLevel + TICK_LENGTH) + ");");
         }
         if (wasFrom != null && wasTo != null) {
            double xWasFrom = OFFSET_X + xS * wasFrom;
            double xWasTo = OFFSET_X + xS * wasTo;
            lines.add("\\draw (" + xWasFrom + "," + yLevel + ") -- (" + xWasTo + "," + yLevel + ");");
         } else if (wasFrom != null) {
            double xWasFrom = OFFSET_X + xS * wasFrom;
            double xArrowTo = xPlanTo > xWasFrom ? xPlanTo : xWasFrom;
            lines.add("\\draw[->] (" + xWasFrom + "," + yLevel + ") -- (" + (xArrowTo + SPAN_ARROW_LENGTH) + "," + yLevel + ");");
         }

         lines.add("\\node[anchor=east] at (" + xMin + "," + yLevel + ") {\\tiny\\texttt{" + names.get(request) + "}};");
      }
   }

   private void createGenesisRequests(Collection<Request> requests) {
      for (Request request : requests) {
         double xStart = OFFSET_X + xS * (request.getDeadline() - (double) request.getData() / request.getAvailableByterate());
         double xEnd = OFFSET_X + xS * request.getDeadline();
         double xMid = (xStart + xEnd) / 2;
         double yTop = OFFSET_Y + yS * request.getAvailableByterate();

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
         lines.add("\\filldraw[" + style + "] (" + xStart + "," + OFFSET_Y + ") rectangle (" + xEnd + "," + yTop + ");");
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

   private void createRates(Map<Long, Integer> rates, String param) {
      List<Long> ticks = new LinkedList<>(rates.keySet());
      Collections.sort(ticks);

      StringBuilder line = new StringBuilder();

      boolean first = false;

      line.append("\\draw[" + param + "] ");
      double prevY = 0;
      for (Long tick : ticks) {
         double x = OFFSET_X + xS * tick;
         double y = OFFSET_Y + yS * rates.get(tick);

         if (first)
            line.append("-- (" + x + "," + prevY + ") --");
         line.append("(" + x + "," + y + ")");

         prevY = y;
         first = true;
      }
      lines.add(line.append(";").toString());
   }

   private void createSystem(long xValueStep, long yValueStep) {
      lines.add("\\draw[thick,->] (0,0) -- (" + DIAGRAM_WIDTH + ",0) node[anchor=north] {$t$};");
      lines.add("\\draw[thick,->] (0,0) -- (0," + DIAGRAM_HEIGHT + ") node[anchor=east] {$B$};");

      double xMax = DIAGRAM_WIDTH - OFFSET_X;
      double yMax = DIAGRAM_HEIGHT - OFFSET_Y;

      long xTicks = (long) (xMax / xTickStep) + 1; // +1 because we also render 0
      long yTicks = (long) (yMax / yTickStep) + 1; // +1 because we also render 0

      for (int tick = 0; tick < xTicks; tick++) {
         double x = OFFSET_X + xTickStep * tick;
         long valueX = tick * xValueStep;
         if (x > DIAGRAM_WIDTH - HEADSPACE_X / 2)
            break;

         lines.add("\\draw (" + x + "," + TICK_LENGTH + ") -- (" + x + ",-" + TICK_LENGTH + ") node [anchor=north] {" + valueX + "};");
         lines.add("\\draw[black!5] (" + x + ",0) -- (" + x + "," + DIAGRAM_HEIGHT + ");");
      }

      for (int tick = 0; tick < yTicks; tick++) {
         double y = OFFSET_Y + yTickStep * tick;
         long valueY = tick * yValueStep;
         if (y > DIAGRAM_HEIGHT - HEADSPACE_Y / 2)
            break;

         lines.add("\\draw (" + TICK_LENGTH + "," + y + ") -- (-" + TICK_LENGTH + "," + y + ") node [anchor=east] {" + valueY + "};");
         lines.add("\\draw[black!5] (0," + y + ") -- (" + DIAGRAM_WIDTH + "," + y + ");");
      }
   }

   private static int getMaxByterate(Genesis genesis) {
      int max = 0;

      for (int value : genesis.getRatePredicted().values())
         max = Math.max(max, value);
      for (int value : genesis.getRateReal().values())
         max = Math.max(max, value);
      for (Request r : genesis.getRequests())
         max = Math.max(max, r.getAvailableByterate());

      return max;
   }

   static long calcXTickInterval(long ticks) {
      // we want some tick interval 10^x so that ticks / (10^x) is not more than 10
      // ticks / (10^x) < 10
      // ticks < 10 * 10^x
      // ticks < 10^(x+1)
      // log10(ticks) < x+1
      // log10(ticks) - 1 < x

      // x > log10(ticks) - 1

      double x = Math.log10(ticks);
      int interval = (int) (Math.pow(10, (int) x));
      if ((ticks / interval) < 6)
         interval /= 2;
      return interval;
   }

   static long calcYTickInterval(long rate) {
      double x = Math.log10(rate);
      int interval = (int) (Math.pow(10, (int) x));
      if ((rate / interval) < 5)
         interval /= 2;
      return interval;
   }

   private class Span {
      public double a, b;

      public Span(double a, double b) {
         this.a = a;
         this.b = b;
      }
   }
}
