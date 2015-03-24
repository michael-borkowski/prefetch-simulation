package at.borkowski.prefetchsimulation.genesis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;

public class GenesisWriter {

   private final BufferedWriter output;

   public GenesisWriter(OutputStream output) {
      try {
         this.output = new BufferedWriter(new OutputStreamWriter(output, "UTF8"));
      } catch (UnsupportedEncodingException ueEx) {
         throw new RuntimeException(ueEx);
      }
   }

   public void write(Genesis genesis) throws IOException {
      Map<Long, Collection<String>> lines = new HashMap<>();

      add(lines, 0, GenesisReader.CMD_ALGORITHM, genesis.getAlgorithm().getName());
      add(lines, 0, GenesisReader.CMD_LOOK_AHEAD, String.valueOf(genesis.getLookAheadTime()));

      addRates(lines, genesis.getRateReal(), GenesisReader.CMD_RATE_REAL);
      addRates(lines, genesis.getRatePredicted(), GenesisReader.CMD_RATE_PREDICTION);

      add(lines, genesis.getTicks() - 1, GenesisReader.CMD_END);

      for (Request request : genesis.getRequests())
         add(lines, request.getDeadline(), GenesisReader.CMD_REQUEST, String.valueOf(request.getData()), String.valueOf(request.getAvailableByterate()));

      List<Long> ticks = new LinkedList<>();
      for (long tick : lines.keySet())
         if (!ticks.contains(tick))
            ticks.add(tick);

      Collections.sort(ticks);
      for (long tick : ticks)
         for (String line : lines.get(tick))
            output.write(line);

      output.flush();
   }

   private void addRates(Map<Long, Collection<String>> lines, Map<Long, Integer> rates, String command) {
      for (long tick : rates.keySet())
         add(lines, tick, command, String.valueOf(rates.get(tick)));
   }

   private void add(Map<Long, Collection<String>> lines, long tick, String... tokens) {
      StringBuilder sb = new StringBuilder();
      sb.append(tick);
      for (String token : tokens) {
         sb.append(' ');
         sb.append(token);
      }
      sb.append('\n');
      String line = sb.toString();

      if (!lines.containsKey(tick))
         lines.put(tick, new LinkedList<>());
      lines.get(tick).add(line);
   }
}
