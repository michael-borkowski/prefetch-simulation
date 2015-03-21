package at.borkowski.prefetchsimulation.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;

public class ConfigurationReader {
   private final BufferedReader input;

   public static final String CMD_TOTAL_TICKS = "ticks";
   public static final String CMD_MAX_BYTERATE = "max-byterate";
   public static final String CMD_SLOT_LENGTH = "slot-length";
   public static final String CMD_NETWORK_UPTIME = "network-uptime";
   public static final String CMD_RELATIVE_JITTER = "relative-jitter";
   public static final String CMD_ABSOLUTE_JITTER = "absolute-jitter";
   public static final String CMD_PREDICTION_ACCURACY = "prediction-accuracy";
   public static final String CMD_REQUEST_SERIES = "request-series";
   public static final String CMD_REQUEST = "request";
   public static final String CMD_LOOK_AHEAD = "look-ahead";
   public static final String CMD_ALGORITHM = "algorithm";

   public ConfigurationReader(InputStream input) {
      try {
         this.input = new BufferedReader(new InputStreamReader(input, "UTF8"));
      } catch (UnsupportedEncodingException ueEx) {
         throw new RuntimeException(ueEx);
      }
   }

   public Configuration read() throws IOException, ConfigurationException {
      Long totalTicks = null;
      Integer maximumByterate = null;
      Long slotLength = null;
      Double networkUptime = null;
      Double relativeJitter = null;
      Integer absoluteJitter = null;
      Double predictionAccuracy = null;
      Collection<RequestSeries> recurringRequestSeries = new LinkedList<>();
      Collection<Request> intermittentRequests = new LinkedList<>();
      Long lookAheadTime = null;
      Class<? extends PrefetchAlgorithm> algorithm = NullAlgorithm.class;

      String line;
      int lineCounter = 0;
      while ((line = this.input.readLine()) != null) {
         lineCounter++;
         line = line.replaceAll("#.*$", "");
         String[] split = line.split("\\s+");
         if (split.length == 0)
            continue;

         ArrayReader reader = new ArrayReader(split);
         String command;

         do {
            command = reader.next();
         } while (command != null && command.length() == 0);

         if (command == null)
            continue;
         else if (command.equals(CMD_TOTAL_TICKS))
            totalTicks = parseLong(lineCounter, CMD_TOTAL_TICKS, reader);
         else if (command.equals(CMD_MAX_BYTERATE))
            maximumByterate = parseInt(lineCounter, CMD_MAX_BYTERATE, reader);
         else if (command.equals(CMD_SLOT_LENGTH))
            slotLength = parseLong(lineCounter, CMD_SLOT_LENGTH, reader);
         else if (command.equals(CMD_NETWORK_UPTIME))
            networkUptime = parseDouble(lineCounter, CMD_NETWORK_UPTIME, reader);
         else if (command.equals(CMD_RELATIVE_JITTER))
            relativeJitter = parseDouble(lineCounter, CMD_RELATIVE_JITTER, reader);
         else if (command.equals(CMD_ABSOLUTE_JITTER))
            absoluteJitter = parseInt(lineCounter, CMD_ABSOLUTE_JITTER, reader);
         else if (command.equals(CMD_PREDICTION_ACCURACY))
            predictionAccuracy = parseDouble(lineCounter, CMD_PREDICTION_ACCURACY, reader);
         else if (command.equals(CMD_REQUEST_SERIES))
            recurringRequestSeries.add(parseSeries(lineCounter, reader));
         else if (command.equals(CMD_REQUEST))
            intermittentRequests.add(parseRequest(lineCounter, reader));
         else if (command.equals(CMD_LOOK_AHEAD))
            lookAheadTime = parseLong(lineCounter, CMD_LOOK_AHEAD, reader);
         else if (command.equals(CMD_ALGORITHM))
            algorithm = parseAlgorithm(lineCounter, reader);
         else
            throw new ConfigurationException("unknown command: " + command);
      }

      require(totalTicks, "total ticks");
      require(maximumByterate, "maximum byterate");
      require(slotLength, "slot length");
      require(networkUptime, "network uptime");
      require(relativeJitter, "relative jitter");
      require(absoluteJitter, "absolute jitter");
      require(predictionAccuracy, "prediction accuracy");
      require(lookAheadTime, "look ahead time");

      return new Configuration(totalTicks, maximumByterate, slotLength, networkUptime, relativeJitter, absoluteJitter, predictionAccuracy, recurringRequestSeries, intermittentRequests, algorithm, lookAheadTime);
   }

   private Request parseRequest(int lineCounter, ArrayReader reader) throws ConfigurationException {
      Long tick = parseLongParam(lineCounter, "tick", reader);
      Integer byterate = parseIntParam(lineCounter, "byterate", reader);
      Integer data = parseIntParam(lineCounter, "data", reader);
      return new Request(tick, data, byterate);
   }

   private RequestSeries parseSeries(int lineCounter, ArrayReader reader) throws ConfigurationException {
      Distribution<Long> interval = parseDistributionLongParam(lineCounter, "interval", reader);
      Distribution<Integer> size = parseDistributionIntParam(lineCounter, "size", reader);
      Distribution<Integer> byterate = parseDistributionIntParam(lineCounter, "byterate", reader);
      Distribution<Long> startTick = parseDistributionLongParam(lineCounter, "start", reader);
      Distribution<Long> endTick = parseDistributionLongParam(lineCounter, "end", reader);

      return new RequestSeries(interval, size, byterate, startTick, endTick);
   }

   private Distribution<Long> parseDistributionLongParam(int lineCounter, String param, ArrayReader reader) throws ConfigurationException {
      String value = null;
      value = getParam(lineCounter, param, reader);
      if (value == null || value.length() == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      String[] split = value.split("\\/");
      if (split.length == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      ArrayReader sub = new ArrayReader(split);
      String type = sub.next();
      if (type.equals("exact") && assertLeft(sub, 1, "exact"))
         return Distributions.exactly(parseLong(lineCounter, "exact", sub.next()));
      else if (type.equals("uniform") && assertLeft(sub, 2, "uniform"))
         return Distributions.uniformLong(parseLong(lineCounter, "uniform min", sub.next()), parseLong(lineCounter, "uniform max", sub.next()));
      else
         return Distributions.exactly(parseLong(lineCounter, "implicit exact", type));
   }

   private Distribution<Integer> parseDistributionIntParam(int lineCounter, String param, ArrayReader reader) throws ConfigurationException {
      String value = null;
      value = getParam(lineCounter, param, reader);
      if (value == null || value.length() == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      String[] split = value.split("\\/");
      if (split.length == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      ArrayReader sub = new ArrayReader(split);
      String type = sub.next();
      if (type.equals("exact") && assertLeft(sub, 1, "exact"))
         return Distributions.exactly(parseInt(lineCounter, "exact", sub.next()));
      else if (type.equals("uniform") && assertLeft(sub, 2, "uniform"))
         return Distributions.uniformInteger(parseInt(lineCounter, "uniform min", sub.next()), parseInt(lineCounter, "uniform max", sub.next()));
      else
         return Distributions.exactly(parseInt(lineCounter, "implicit exact", type));
   }

   private long parseLong(int lineCounter, String command, String param) throws ConfigurationException {
      try {
         return Long.parseLong(param);
      } catch (NumberFormatException nfEx) {
         throw new ConfigurationException("could not parse parameter for " + command + " on line " + lineCounter + ": " + param, nfEx);
      }
   }

   private int parseInt(int lineCounter, String command, String param) throws ConfigurationException {
      try {
         return Integer.parseInt(param);
      } catch (NumberFormatException nfEx) {
         throw new ConfigurationException("could not parse parameter for " + command + " on line " + lineCounter + ": " + param, nfEx);
      }
   }

   private double parseDouble(int lineCounter, String command, String param) throws ConfigurationException {
      try {
         return Double.parseDouble(param);
      } catch (NumberFormatException nfEx) {
         throw new ConfigurationException("could not parse parameter for " + command + " on line " + lineCounter + ": " + param, nfEx);
      }
   }

   private boolean assertLeft(ArrayReader reader, int count, String parameter) throws ConfigurationException {
      if (reader.getRemaining() != count)
         throw new ConfigurationException("expected " + count + " parameters for " + parameter + ", got " + reader.getRemaining());
      return true;
   }

   private long parseLongParam(int lineCounter, String param, ArrayReader reader) throws ConfigurationException {
      return parseLong(lineCounter, param, getParam(lineCounter, param, reader));
   }

   private int parseIntParam(int lineCounter, String param, ArrayReader reader) throws ConfigurationException {
      return parseInt(lineCounter, param, getParam(lineCounter, param, reader));
   }

   private String getParam(int lineCouter, String param, ArrayReader reader) throws ConfigurationException {
      ArrayReader clone = new ArrayReader(reader);

      while (true) {
         String key = clone.next();

         if (key == null)
            break;
         if (param.equals(key))
            return clone.next();
      }

      throw new ConfigurationException("line" + lineCouter + ": parameter " + param + " not found");
   }

   private Double parseDouble(int lineCounter, String command, ArrayReader reader) throws ConfigurationException {
      String param = reader.next();
      if (param == null)
         throw new ConfigurationException("line " + lineCounter + ": usage is \"" + command + " <parameter>");
      return parseDouble(lineCounter, command, param);
   }

   private Long parseLong(int lineCounter, String command, ArrayReader reader) throws ConfigurationException {
      String param = reader.next();
      if (param == null)
         throw new ConfigurationException("line " + lineCounter + ": usage is \"" + command + " <parameter>");
      return parseLong(lineCounter, command, param);
   }

   private Integer parseInt(int lineCounter, String command, ArrayReader reader) throws ConfigurationException {
      String param = reader.next();
      if (param == null)
         throw new ConfigurationException("line " + lineCounter + ": usage is \"" + command + " <parameter>");
      return parseInt(lineCounter, command, param);
   }

   private void require(Object parameter, String name) throws ConfigurationException {
      if (parameter == null)
         throw new ConfigurationException("required parameter missing: " + name);
   }

   private Class<? extends PrefetchAlgorithm> parseAlgorithm(int lineCounter, ArrayReader reader) throws ConfigurationException {
      String param = reader.next();
      if (param == null)
         throw new ConfigurationException("line " + lineCounter + ": usage is \"" + CMD_ALGORITHM + " <algorithm-class>");

      try {
         @SuppressWarnings("unchecked")
         Class<? extends PrefetchAlgorithm> clazz = (Class<? extends PrefetchAlgorithm>) Class.forName(param);
         return clazz;
      } catch (ClassNotFoundException e) {
         throw new ConfigurationException("line " + lineCounter + ": class not found: " + param, e);
      }
   }

   private class ArrayReader {
      private final String[] array;
      private int next = 0;

      public ArrayReader(String[] array) {
         this.array = array;
      }

      public int getRemaining() {
         return array.length - next;
      }

      public ArrayReader(ArrayReader that) {
         this.array = that.array;
         this.next = that.next;
      }

      public String next() {
         if (next >= array.length)
            return null;
         else
            return array[next++];
      }

   }
}