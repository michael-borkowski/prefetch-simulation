package at.borkowski.prefetchsimulation.genesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;

public class GenesisReader {
   private final BufferedReader input;

   public static final String CMD_END = "end";
   public static final String CMD_REQUEST = "request";
   public static final String CMD_RATE_REAL = "rate-real";
   public static final String CMD_RATE_PREDICTION = "rate-prediction";
   public static final String CMD_ALGORITHM = "algorithm";
   public static final String CMD_ALGORITHM_PARAMETER = "algorithm-parameter";
   public static final String CMD_LOOK_AHEAD = "look-ahead";

   public GenesisReader(InputStream input) {
      try {
         this.input = new BufferedReader(new InputStreamReader(input, "UTF8"));
      } catch (UnsupportedEncodingException ueEx) {
         throw new RuntimeException(ueEx);
      }
   }

   public Genesis read() throws IOException, GenesisException {
      List<Request> requests = new LinkedList<>();
      Map<Long, Integer> real = new HashMap<>();
      Map<Long, Integer> predicted = new HashMap<>();
      Class<? extends PrefetchAlgorithm> algorithm = NullAlgorithm.class;
      Map<String, String> algorithmConfiguration = new HashMap<>();

      Long lookAhead = null;

      String line;
      long tick = 0, lastTick = -1, end = -1;
      int lineCounter = 0;
      while ((line = this.input.readLine()) != null) {
         lineCounter++;
         line = line.replaceAll("#.*$", "");
         String[] split = line.split("\\s+");

         while (split.length > 0 && split[0].length() == 0)
            split = Arrays.copyOfRange(split, 1, split.length);
         
         if (split.length == 0)
            continue;
         
         try {
            tick = Long.parseLong(split[0]);
         } catch (NumberFormatException nfEx) {
            throw new GenesisException("could not parse tick number on line " + lineCounter + ": " + split[0], nfEx);
         }
         if (tick < 0)
            throw new GenesisException("negative tick on line " + lineCounter + ": " + tick);
         if (tick < lastTick)
            throw new GenesisException("ticks out of order on line " + lineCounter + ": " + tick + " < " + lastTick);
         if (end != -1)
            throw new GenesisException("line " + lineCounter + ": no events after \"" + CMD_END + "\" are allowed");

         if (split[1].equals(CMD_END))
            if (split.length != 2)
               throw new GenesisException("no parameters allowed for \"" + CMD_END + "\"");
            else
               end = tick;
         else if (split[1].equals(CMD_REQUEST))
            requests.add(parseRequest(tick, lineCounter, split));
         else if (split[1].equals(CMD_RATE_REAL))
            parseRate(tick, lineCounter, CMD_RATE_REAL, real, split);
         else if (split[1].equals(CMD_RATE_PREDICTION))
            parseRate(tick, lineCounter, CMD_RATE_PREDICTION, predicted, split);
         else if (split[1].equals(CMD_ALGORITHM))
            algorithm = parseAlgorithm(tick, lineCounter, split);
         else if (split[1].equals(CMD_ALGORITHM_PARAMETER))
            parseAlgorithmParam(tick, lineCounter, split, algorithmConfiguration);
         else if (split[1].equals(CMD_LOOK_AHEAD))
            lookAhead = parseLookAhead(tick, lineCounter, split);
         else
            throw new GenesisException("unknown command: " + split[1]);

         lastTick = tick;
      }

      if (end == -1)
         end = tick;

      if (lookAhead == null)
         lookAhead = end + 1;

      return new Genesis(end + 1, requests, real, predicted, algorithm, algorithmConfiguration, lookAhead);
   }

   private void parseAlgorithmParam(long tick, int lineCounter, String[] split, Map<String, String> algorithmConfiguration) throws GenesisException {
      if (tick != 0)
         throw new GenesisException("line " + lineCounter + ": algorithm parameters must be set at tick 0");
      if (split.length != 4)
         throw new GenesisException("line " + lineCounter + ": usage is \"0 " + CMD_ALGORITHM_PARAMETER + " <key> <value>");

      String k = split[2];
      String v = split[3];
      algorithmConfiguration.put(k, v);
   }

   private Long parseLookAhead(long tick, int lineCounter, String[] split) throws GenesisException {
      if (tick != 0)
         throw new GenesisException("line " + lineCounter + ": look-ahead time must be set at tick 0");
      if (split.length != 3)
         throw new GenesisException("line " + lineCounter + ": usage is \"0 " + CMD_LOOK_AHEAD + " <look-ahead-time>");

      try {
         return Long.parseLong(split[2]);
      } catch (NumberFormatException nfEx) {
         throw new GenesisException("could not parse look-ahead-time on line " + lineCounter + ": " + split[2], nfEx);
      }
   }

   private Class<? extends PrefetchAlgorithm> parseAlgorithm(long tick, int lineCounter, String[] split) throws GenesisException {
      if (tick != 0)
         throw new GenesisException("line " + lineCounter + ": algorithm must be set at tick 0");
      if (split.length != 3)
         throw new GenesisException("line " + lineCounter + ": usage is \"0 " + CMD_ALGORITHM + " <algorithm-class>");

      try {
         @SuppressWarnings("unchecked")
         Class<? extends PrefetchAlgorithm> clazz = (Class<? extends PrefetchAlgorithm>) Class.forName(split[2]);
         return clazz;
      } catch (ClassNotFoundException e) {
         throw new GenesisException("line " + lineCounter + ": class not found: " + split[2], e);
      }
   }

   private void parseRate(long tick, int lineCounter, String cmd, Map<Long, Integer> map, String[] split) throws GenesisException {
      if (split.length != 3)
         throw new GenesisException("line " + lineCounter + ": usage is \"<tick> " + cmd + " <rate>");

      int rate;
      try {
         rate = Integer.parseInt(split[2]);
      } catch (NumberFormatException nfEx) {
         throw new GenesisException("could not parse rate on line " + lineCounter + ": " + split[2], nfEx);
      }

      map.put(tick, rate);
   }

   private Request parseRequest(long tick, int lineCounter, String[] split) throws GenesisException {
      if (split.length != 4)
         throw new GenesisException("line " + lineCounter + ": usage is \"<tick> " + CMD_REQUEST + " <data> <byterate>");

      int data;
      int byterate;

      try {
         data = Integer.parseInt(split[2]);
      } catch (NumberFormatException nfEx) {
         throw new GenesisException("could not parse request data length on line " + lineCounter + ": " + split[2], nfEx);
      }
      try {
         byterate = Integer.parseInt(split[3]);
      } catch (NumberFormatException nfEx) {
         throw new GenesisException("could not parse request byte rate on line " + lineCounter + ": " + split[3], nfEx);
      }

      return new Request(tick, data, byterate);
   }
}
