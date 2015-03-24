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

   public static final String CMD_SEED = "seed";
   public static final String CMD_TOTAL_TICKS = "ticks";
   public static final String CMD_MAX_BYTERATE = "max-byterate";
   public static final String CMD_SLOT_LENGTH = "slot-length";
   public static final String CMD_NETWORK_UPTIME = "network-uptime";
   public static final String CMD_RELATIVE_JITTER = "relative-jitter";
   public static final String CMD_ABSOLUTE_JITTER = "absolute-jitter";
   public static final String CMD_PREDICTION_TIME_ACCURACY = "prediction-time-accuracy";
   public static final String CMD_PREDICTION_AMPLITUDE_ACCURACY = "prediction-amplitude-accuracy";
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
      Long seed = null;
      Long totalTicks = null;
      Integer maximumByterate = null;
      Long slotLength = null;
      Double networkUptime = null;
      Double relativeJitter = null;
      Integer absoluteJitter = null;
      Double predictionTimeAccuracy = null;
      Double predictionAmplitudeAccuracy = null;
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
         else if (command.equals(CMD_SEED))
            seed = parseLong(lineCounter, CMD_SEED, reader);
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
         else if (command.equals(CMD_PREDICTION_TIME_ACCURACY))
            predictionTimeAccuracy = parseDouble(lineCounter, CMD_PREDICTION_TIME_ACCURACY, reader);
         else if (command.equals(CMD_PREDICTION_AMPLITUDE_ACCURACY))
            predictionAmplitudeAccuracy = parseDouble(lineCounter, CMD_PREDICTION_AMPLITUDE_ACCURACY, reader);
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
      require(predictionTimeAccuracy, "prediction time accuracy");
      require(predictionAmplitudeAccuracy, "prediction amplitude accuracy");
      require(lookAheadTime, "look ahead time");

      Configuration configuration = new Configuration(totalTicks, maximumByterate, slotLength, networkUptime, relativeJitter, absoluteJitter, predictionTimeAccuracy, predictionAmplitudeAccuracy, recurringRequestSeries, intermittentRequests, algorithm, lookAheadTime);
      if (seed != null)
         configuration.setSeed(seed);
      return configuration;
   }

   private Request parseRequest(int lineCounter, ArrayReader reader) throws ConfigurationException {
      Long tick = parseLongParam(lineCounter, "tick", reader);
      Integer byterate = parseIntParam(lineCounter, "byterate", reader);
      Integer data = parseIntParam(lineCounter, "data", reader);
      return new Request(tick, data, byterate);
   }

   private RequestSeries parseSeries(int lineCounter, ArrayReader reader) throws ConfigurationException {
      Distribution<Long> interval = parseDistribution(lineCounter, "interval", reader, Long.class);
      Distribution<Integer> size = parseDistribution(lineCounter, "size", reader, Integer.class);
      Distribution<Integer> byterate = parseDistribution(lineCounter, "byterate", reader, Integer.class);
      Distribution<Long> startTick = parseDistribution(lineCounter, "start", reader, Long.class);
      Distribution<Long> endTick = parseDistribution(lineCounter, "end", reader, Long.class);

      return new RequestSeries(interval, size, byterate, startTick, endTick);
   }

   private <T extends Number> Distribution<T> parseDistribution(int lineCounter, String param, ArrayReader reader, Class<T> clazz) throws ConfigurationException {
      String value = null;
      value = getParam(lineCounter, param, reader);
      if (value == null || value.length() == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      String[] split = value.split("\\/");
      if (split.length == 0)
         throw new ConfigurationException("empty paramter for " + param + " on line " + lineCounter);
      ArrayReader sub = new ArrayReader(split);
      String type = sub.next();
      if (isExact(type) && assertLeft(sub, 1, "exact"))
         return Distributions.exact(parse(lineCounter, "exact", sub.next(), clazz));
      else if (isUniform(type) && assertLeft(sub, 2, "uniform"))
         return Distributions.uniform(parse(lineCounter, "uniform min", sub.next(), clazz), parse(lineCounter, "uniform max", sub.next(), clazz), clazz);
      else if (isNormal(type) && assertLeft(sub, 2, "normal"))
         return Distributions.normal(parse(lineCounter, "normal mean", sub.next(), clazz), parse(lineCounter, "normal sd", sub.next(), clazz), clazz);
      else
         return Distributions.exact(parse(lineCounter, "implicit exact", type, clazz));
   }

   private boolean isExact(String type) {
      return "exact".equals(type) || "exactly".equals(type) || "ex".equals(type) || "=".equals(type);
   }

   private boolean isUniform(String type) {
      return "uniform".equals(type) || "unif".equals(type) || "u".equals(type);
   }

   private boolean isNormal(String type) {
      return "normal".equals(type) || "norm".equals(type) || "n".equals(type) || "gaussian".equals(type) || "gauss".equals(type) || "g".equals(type) || "~".equals(type);
   }

   @SuppressWarnings("unchecked")
   private <T extends Number> T parse(int lineCounter, String command, String param, Class<T> clazz) throws ConfigurationException {
      if (clazz.equals(Long.class))
         return (T) new Long(parseLong(lineCounter, command, param));
      else if (clazz.equals(Integer.class))
         return (T) new Integer(parseInt(lineCounter, command, param));
      else
         throw new RuntimeException("unknown parse class " + clazz);
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
