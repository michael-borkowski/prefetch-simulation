package at.borkowski.prefetchsimulation.visualiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.visualiser.result.LaTeXVisualisationResult;

public class GenesisVisualiser {
   public static final double DIAGRAM_WIDTH = 15;
   public static final double DIAGRAM_HEIGHT = 10;

   public static final double OFFSET_X = 0.2;
   public static final double OFFSET_Y = 0.2;
   public static final double HEADSPACE_X = 0.6;
   public static final double HEADSPACE_Y = 1;

   public static final double TICK_LENGTH = 0.08;

   public static final String STYLE_REAL = "blue";
   public static final String STYLE_PREDICTED = "blue,densely dotted";

   public static final double LEGEND_WIDTH = 3.53;
   public static final double LEGEND_HEIGHT = 1.15;
   public static final double LEGEND_X = 12;
   public static final double LEGEND_Y = 10.6;

   public static final double LEGEND_PADDING_X = 0.2;
   public static final double LEGEND_PADDING_Y = 0.2;
   public static final double LEGEND_BOX_SIZE = 0.2;
   public static final double LEGEND_COL1_WIDTH = 1.4;
   public static final double LEGEND_ROW_HEIGHT = 0.3;

   public static final String REQ_STYLE = "fill=black!10, draw=black!30,fill opacity=0.5";
   public static final String REQ_TEXT_STYLE = "text=black!60";

   final Genesis genesis;
   final List<String> lines;
   final long xMax, yMax;
   final double xS, yS, xTickStep, yTickStep;

   int nameCounter = 0;
   final Map<Request, String> names = new HashMap<>();

   final long xTickInterval, yTickInterval;

   public static LaTeXVisualisationResult visualise(Genesis genesis) {
      return new GenesisVisualiser(genesis).visualise();
   }

   GenesisVisualiser(Genesis genesis) {
      this.genesis = genesis;

      lines = new LinkedList<>();

      xMax = genesis.getTicks();
      yMax = getMaxByterate(genesis);

      xTickInterval = calcXTickInterval(xMax);
      yTickInterval = calcYTickInterval(yMax);

      xS = (double) (DIAGRAM_WIDTH - OFFSET_X - HEADSPACE_X) / xMax;
      yS = (double) (DIAGRAM_HEIGHT - 2 * OFFSET_Y - HEADSPACE_Y) / yMax;

      xTickStep = xTickInterval * xS;
      yTickStep = yTickInterval * yS;

      createNames(genesis.getRequests());
   }

   LaTeXVisualisationResult visualise() {
      createBase();
      createGenesisRequests(genesis.getRequests());
      createRates();
      createLegend(LEGEND_HEIGHT, false);
      createFinish();

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintWriter pw = new PrintWriter(baos)) {
         for (String line : lines)
            pw.println(line);
         pw.flush();
         return new LaTeXVisualisationResult(baos.toByteArray());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   void createBase() {
      createHeader();
      createSystem(xTickInterval, yTickInterval);
   }

   void createRates() {
      createRates(genesis.getRateReal(), STYLE_REAL);
      createRates(genesis.getRatePredicted(), STYLE_PREDICTED);
   }

   void createFinish() {
      createFooter();
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

   void createLegend(double height, boolean enhancedSpace) {
      lines.add("\\filldraw[fill=white, draw=black] (" + LEGEND_X + "," + (LEGEND_Y - height) + ") rectangle (" + (LEGEND_X + LEGEND_WIDTH) + "," + LEGEND_Y + ");");

      double y = LEGEND_Y - LEGEND_PADDING_Y;
      double box_x = LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH / 2 - LEGEND_BOX_SIZE / 2;

      if (enhancedSpace) {
         y -= 3 * LEGEND_ROW_HEIGHT;
      } else {
         lines.add("\\filldraw[" + REQ_STYLE + "] (" + box_x + "," + y + ") rectangle (" + (box_x + LEGEND_BOX_SIZE) + "," + (y - LEGEND_BOX_SIZE) + ");");
         lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Request}};");
         y -= LEGEND_ROW_HEIGHT;
      }

      double xspace = LEGEND_COL1_WIDTH - LEGEND_PADDING_X;
      double x2 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 1 * xspace / 3;
      double x3 = LEGEND_X + 1.5 * LEGEND_PADDING_X + 2 * xspace / 3;

      lines.add("\\draw[" + STYLE_REAL + "] (" + x2 + "," + (y - LEGEND_BOX_SIZE / 2) + ") -- (" + x3 + "," + (y - LEGEND_BOX_SIZE / 2) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Real Bandwidth}};");
      y -= LEGEND_ROW_HEIGHT;
      lines.add("\\draw[" + STYLE_PREDICTED + "] (" + x2 + "," + (y - LEGEND_BOX_SIZE / 2) + ") -- (" + x3 + "," + (y - LEGEND_BOX_SIZE / 2) + ");");
      lines.add("\\node[anchor=west] at (" + (LEGEND_X + LEGEND_PADDING_X + LEGEND_COL1_WIDTH) + "," + (y - LEGEND_BOX_SIZE / 2) + ") {\\tiny{Prediction}};");
      y -= LEGEND_ROW_HEIGHT;
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

   private void createGenesisRequests(Collection<Request> requests) {
      for (Request request : requests) {
         double xStart = OFFSET_X + xS * (request.getDeadline() - (double) request.getData() / request.getAvailableByterate());
         double xEnd = OFFSET_X + xS * request.getDeadline();
         double xMid = (xStart + xEnd) / 2;
         double yTop = OFFSET_Y + yS * request.getAvailableByterate();

         String style = REQ_STYLE;
         String textStyle = REQ_TEXT_STYLE;
         lines.add("\\filldraw[" + style + "] (" + xStart + "," + OFFSET_Y + ") rectangle (" + xEnd + "," + yTop + ");");
         lines.add("\\node[anchor=south," + textStyle + "] at (" + xMid + "," + yTop + ") {\\tiny\\texttt{" + names.get(request) + "}};");
      }
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
      
      lines.add("\\draw[thick,->] (0,0) -- (" + DIAGRAM_WIDTH + ",0) node[anchor=north] {$t$};");
      lines.add("\\draw[thick,->] (0,0) -- (0," + DIAGRAM_HEIGHT + ") node[anchor=east] {$B$};");
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
}
