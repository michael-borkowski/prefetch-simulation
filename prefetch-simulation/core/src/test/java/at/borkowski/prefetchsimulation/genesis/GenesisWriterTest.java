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
   public void test() throws IOException {
      long ticks = 10000;
      List<Request> requests = new LinkedList<>();
      Map<Long, Integer> rateReal = new HashMap<>();
      Map<Long, Integer> ratePredicted = new HashMap<>();
      PrefetchAlgorithm algorithm = new IgnoreRatePredictionAlgorithm();

      rateReal.put(0L, 10);
      rateReal.put(1L, 11);
      ratePredicted.put(1L, 10);
      ratePredicted.put(2L, 20);
      ratePredicted.put(3L, 23);

      sut.write(new Genesis(ticks, requests, rateReal, ratePredicted, algorithm));

      List<String> expected = new LinkedList<>();
      expected.add("0 algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      expected.add("0 rate-real 10");
      expected.add("1 rate-real 11");
      expected.add("1 rate-prediction 10");
      expected.add("2 rate-prediction 20");
      expected.add("3 rate-prediction 23");
      expected.add("9999 end");
      String[] expectedArray = expected.toArray(new String[0]);

      assertArrayEquals(expectedArray, parse());
   }

}
