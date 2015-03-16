package at.borkowski.scovillej.prefetch.members.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.profile.Series;
import at.borkowski.scovillej.services.comm.SimulationSocket;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.spicej.WouldBlockException;

public class ClientProcessorTest {

   ClientProcessor sut;

   FetchServer owner;
   SimulationSocket<VirtualPayload> socket;

   SimulationContext context;
   long tick = 0;

   Integer request = null;
   VirtualPayload response = null;
   boolean close = false, closed = false;


   @Before
   public void setUp() throws Exception {

      socket = new SimulationSocket<VirtualPayload>() {
         @Override
         public void write(VirtualPayload object) throws IOException {
            response = object;
         }

         @Override
         public VirtualPayload read() throws IOException {
            if (close)
               return null;
            if (request == null)
               throw new WouldBlockException();
            VirtualPayload ret = new VirtualPayload(request);
            request = null;
            return ret;
         }

         @Override
         public boolean established() {
            return true;
         }

         @Override
         public void close() {
            closed = true;
         }

         @Override
         public int available() throws IOException {
            return request == null && !close ? 0 : 1;
         }
      };

      owner = mock(FetchServer.class);

      sut = new ClientProcessor(owner, socket);

      context = new SimulationContext() {
         @Override
         public <T> T getService(Class<T> clazz) {
            return null;
         }

         @Override
         public <T> Series<T> getSeries(String symbol, Class<T> clazz) {
            return null;
         }

         @Override
         public long getCurrentTick() {
            return tick;
         }

         @Override
         public String getCurrentPhase() {
            return "tick";
         }
      };
   }

   @Test
   public void test() throws IOException {
      advance();
      advance();
      advance();

      request = 100;

      advance();

      assertNull(request);
      assertEquals(100, response.getSize());

      advance();
      advance();

      close = true;
      
      advance();

      assertTrue(closed);
      verify(owner).deregisterClientProcessor(sut);

   }

   private void advance() throws IOException {
      sut.handle(context);
      tick++;
   }

}
