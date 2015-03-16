package at.borkowski.scovillej.prefetch.members.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.Request;

public class CacheProcessorTest {

   CacheProcessor sut;

   @Before
   public void setUp() throws Exception {
      sut = new CacheProcessor();
   }

   @Test
   public void test() {
      Request request = new Request(1, 2, 3);
      assertFalse(sut.hasFile(request));

      sut.save(request, 123);

      assertTrue(sut.hasFile(request));
      assertEquals(123, sut.getTimestamp(request));
   }

}
