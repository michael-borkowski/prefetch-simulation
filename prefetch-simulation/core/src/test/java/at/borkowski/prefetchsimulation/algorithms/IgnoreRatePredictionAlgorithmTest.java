package at.borkowski.prefetchsimulation.algorithms;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;

public class IgnoreRatePredictionAlgorithmTest {

   IgnoreRatePredictionAlgorithm sut = new IgnoreRatePredictionAlgorithm();

   @Test
   public void testBasic() {
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(3000, 200, 8));

      Map<Request, Long> schedules = sut.schedule(req, null);

      assertEquals(3000 - (200 / 8) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(2)).longValue());
      assertEquals(2000 - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(1)).longValue());
      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }

   @Test
   public void testPartialOverlap() {
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(2100, 200, 2));

      Map<Request, Long> schedules = sut.schedule(req, null);

      long last;
      assertEquals(last = 2100 - (200 / 2) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(2)).longValue());
      assertEquals(last - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(1)).longValue());
      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }

   @Test
   public void testFullOverlap1() {
      List<Request> req = new LinkedList<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(2000, 300, 4));

      Map<Request, Long> schedules = sut.schedule(req, null);

      long x, y;
      x = schedules.get(req.get(2)).longValue();
      y = schedules.get(req.get(1)).longValue();

      long a = Math.min(x, y);
      long b = Math.max(x, y);
      
      long last;
      assertEquals(last = 2000 - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, b);
      assertEquals(last - (300 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, a);

      assertEquals(1000 - (100 / 4) - IgnoreRatePredictionAlgorithm.CONNECTION_OVERHEAD - 1, schedules.get(req.get(0)).longValue());
   }

}
