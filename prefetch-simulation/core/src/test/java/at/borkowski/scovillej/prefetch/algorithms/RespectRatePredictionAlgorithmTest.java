package at.borkowski.scovillej.prefetch.algorithms;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionServiceProvider;

public class RespectRatePredictionAlgorithmTest {

   RespectRatePredictionAlgorithm sut = new RespectRatePredictionAlgorithm();
   
   Map<Long, Integer> rates = new HashMap<>();

   @Test
   public void testBasic() {
      rates.put(1500L, 5);
      rates.put(2500L, 1);
      RatePredictionServiceProvider provider = new RatePredictionServiceProvider(rates);
      
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(3000, 200, 8));

      Map<Request, Long> schedules = sut.schedule(req, provider);

      assertEquals(3000 - (200 / 1) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(2)).longValue());
      assertEquals(2000 - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(1)).longValue());
      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }

   @Test
   public void testPartialOverlap() {
      rates.put(1500L, 5);
      rates.put(2050L, 1);
      RatePredictionServiceProvider provider = new RatePredictionServiceProvider(rates);
      
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(2100, 200, 2));

      Map<Request, Long> schedules = sut.schedule(req, provider);

      long last;
      assertEquals(last = 2100 - (49 / 1) - (151 / 2) + 1 - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(2)).longValue());
      assertEquals(last - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(1)).longValue());
      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }

   @Test
   public void testFullOverlap1() {
      rates.put(500L, 5);
      rates.put(1950L, 1);
      RatePredictionServiceProvider provider = new RatePredictionServiceProvider(rates);
      
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(2000, 300, 4));

      Map<Request, Long> schedules = sut.schedule(req, provider);

      long x, y;
      x = schedules.get(req.get(2)).longValue();
      y = schedules.get(req.get(1)).longValue();

      long a = Math.min(x, y);
      long b = Math.max(x, y);
      
      long last;
      assertEquals(last = 2000 - (49 / 1) - (251 / 4) + 2 - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, b);
      assertEquals(last - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, a);

      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }
}
