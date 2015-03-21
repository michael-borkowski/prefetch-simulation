package at.borkowski.prefetchsimulation.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RepeatableRandomTest {

   @Test
   public void test() {
      RepeatableRandom sut = new RepeatableRandom(80);

      RepeatableRandom forked = sut.fork();
      assertEquals(6966677935912594397L, forked.nextLong());

      assertEquals(0.4822504028505822D, forked.nextDouble(), Double.MIN_VALUE);
      assertEquals(96, forked.nextInt(123));
      assertEquals(29, forked.nextInt(32));
      assertEquals(-5469509210919517232L, forked.nextLong());
   }

   @Test
   public void testSeed() {
      RepeatableRandom sut = new RepeatableRandom(20);
      sut.setSeed(80);

      RepeatableRandom forked = sut.fork();
      assertEquals(6966677935912594397L, forked.nextLong());

      assertEquals(0.4822504028505822D, forked.nextDouble(), Double.MIN_VALUE);
      assertEquals(96, forked.nextInt(123));
      assertEquals(29, forked.nextInt(32));
      assertEquals(-5469509210919517232L, forked.nextLong());
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void testZeroBound() {
      RepeatableRandom sut = new RepeatableRandom(80);
      sut.nextInt(0);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void testNegativeBound() {
      RepeatableRandom sut = new RepeatableRandom(80);
      sut.nextInt(-42);
   }
}
