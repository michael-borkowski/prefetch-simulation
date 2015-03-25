package at.borkowski.prefetchsimulation.genesis;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;

public class GenesisWriterTest {

   ByteArrayOutputStream bos;
   GenesisWriter sut;

   @Before
   public void setUp() throws Exception {
      bos = new ByteArrayOutputStream();
      sut = new GenesisWriter(bos);
   }

   private String[] parse() {
      String string;
      try {
         string = new String(bos.toByteArray(), "UTF8");
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }

      return string.split("\\n");
   }

   @Test
   public void testRates() throws IOException {
      long ticks = 10000;
      long lookAhead = 8000;
      List<Request> requests = new LinkedList<>();
      Map<Long, Integer> rateReal = new HashMap<>();
      Map<Long, Integer> ratePredicted = new HashMap<>();
      Class<? extends PrefetchAlgorithm> algorithm = IgnoreRatePredictionAlgorithm.class;
      Map<String, String> algorithmConfiguration = new HashMap<>();

      rateReal.put(0L, 10);
      rateReal.put(1L, 11);
      ratePredicted.put(1L, 10);
      ratePredicted.put(2L, 20);
      ratePredicted.put(3L, 23);

      sut.write(new Genesis(ticks, requests, rateReal, ratePredicted, algorithm, algorithmConfiguration, lookAhead));

      List<String> expected = new LinkedList<>();
      expected.add("0 algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      expected.add("0 look-ahead 8000");
      expected.add("0 rate-real 10");
      expected.add("1 rate-real 11");
      expected.add("1 rate-prediction 10");
      expected.add("2 rate-prediction 20");
      expected.add("3 rate-prediction 23");
      expected.add("9999 end");
      String[] expectedArray = expected.toArray(new String[0]);

      assertArrayEquals(expectedArray, parse());
   }

   @Test
   public void testRequests() throws IOException {
      long ticks = 10000;
      long lookAhead = 8000;
      List<Request> requests = new LinkedList<>();
      Map<Long, Integer> rateReal = new HashMap<>();
      Map<Long, Integer> ratePredicted = new HashMap<>();
      Class<? extends PrefetchAlgorithm> algorithm = IgnoreRatePredictionAlgorithm.class;
      Map<String, String> algorithmConfiguration = new HashMap<>();

      requests.add(new Request(10, 20, 30));
      requests.add(new Request(11, 21, 31));
      requests.add(new Request(21, 22, 32));

      sut.write(new Genesis(ticks, requests, rateReal, ratePredicted, algorithm, algorithmConfiguration, lookAhead));

      List<String> expected = new LinkedList<>();
      expected.add("0 algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      expected.add("0 look-ahead 8000");
      expected.add("10 request 20 30");
      expected.add("11 request 21 31");
      expected.add("21 request 22 32");
      expected.add("9999 end");
      String[] expectedArray = expected.toArray(new String[0]);

      assertArrayEquals(expectedArray, parse());
   }

   @Test
   public void testAlgorithmConfiguration() throws IOException {
      long ticks = 10000;
      long lookAhead = 8000;
      List<Request> requests = new LinkedList<>();
      Map<Long, Integer> rateReal = new HashMap<>();
      Map<Long, Integer> ratePredicted = new HashMap<>();
      Class<? extends PrefetchAlgorithm> algorithm = IgnoreRatePredictionAlgorithm.class;
      Map<String, String> algorithmConfiguration = new HashMap<>();

      algorithmConfiguration.put("key1", "value1");
      algorithmConfiguration.put("key2", "value2");
      algorithmConfiguration.put("key3", "value3");

      sut.write(new Genesis(ticks, requests, rateReal, ratePredicted, algorithm, algorithmConfiguration, lookAhead));

      List<String> expected = new LinkedList<>();
      expected.add("0 algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      expected.add("0 algorithm-parameter key1 value1");
      expected.add("0 algorithm-parameter key2 value2");
      expected.add("0 algorithm-parameter key3 value3");
      expected.add("0 look-ahead 8000");
      expected.add("9999 end");
      String[] expectedArray = expected.toArray(new String[0]);

      assertArrayEquals(expectedArray, parse());
   }

}
