package at.borkowski.scovillej.prefetch.algorithms;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.Request;

public class NullAlgorithmTest {

   NullAlgorithm sut;

   @Before
   public void setUp() throws Exception {
      sut = new NullAlgorithm();
   }

   @Test
   public void test() {
      Set<Request> req = new HashSet<>();
      req.add(new Request(1000, 100, 4));
      req.add(new Request(2000, 300, 4));
      req.add(new Request(3000, 10, 5));
      req.add(new Request(3010, 300, 5));

      Map<Request, Long> schedules = sut.schedule(req, null);
      assertTrue(schedules.isEmpty());
   }
}
