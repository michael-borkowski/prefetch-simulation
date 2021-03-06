package at.borkowski.prefetchsimulation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import at.borkowski.prefetchsimulation.Request;

public class RequestTest {

   @Test
   public void test() {
      Request r = new Request(1234, 4321, 1337);
      assertEquals(1234, r.getDeadline());
      assertEquals(4321, r.getData());
      assertEquals(1337, r.getAvailableByterate());
   }

}
