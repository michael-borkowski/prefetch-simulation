package at.borkowski.scovillej.prefetch.members.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.services.comm.SimulationSocket;
import at.borkowski.scovillej.simulation.SimulationContext;

public class SocketProcessorTest {

   SocketProcessor sut;
   CommunicationService communicationService;
   SimulationContext context;
   SimulationSocket<VirtualPayload> socket;

   long tick = 0;
   boolean established = false;
   VirtualPayload data = null;

   @SuppressWarnings("unchecked")
   @Before
   public void setUp() throws Exception {
      sut = new SocketProcessor("socket");
      communicationService = mock(CommunicationService.class);
      context = mock(SimulationContext.class);
      socket = mock(SimulationSocket.class);

      when(context.getService(CommunicationService.class)).thenReturn(communicationService);
      when(context.getCurrentTick()).then(returnCurrentTick());

      when(communicationService.beginConnect("socket", VirtualPayload.class)).thenReturn(socket);
      when(socket.established()).then(returnEstablished());
      when(socket.available()).then(returnAvailable());
      when(socket.read()).then(returnData());
      doAnswer(writeData()).when(socket).write(any(VirtualPayload.class));

      sut.initialize(null, context);
   }

   private Answer<Void> writeData() {
      return new Answer<Void>() {
         @Override
         public Void answer(InvocationOnMock invocation) throws Throwable {
            data = (VirtualPayload) invocation.getArguments()[0];
            return null;
         }
      };
   }

   private Answer<Integer> returnAvailable() {
      return new Answer<Integer>() {
         @Override
         public Integer answer(InvocationOnMock invocation) throws Throwable {
            return data != null ? 1 : 0;
         }
      };
   }

   private Answer<VirtualPayload> returnData() {
      return new Answer<VirtualPayload>() {
         @Override
         public VirtualPayload answer(InvocationOnMock invocation) throws Throwable {
            return data;
         }
      };
   }

   private Answer<Long> returnCurrentTick() {
      return new Answer<Long>() {
         @Override
         public Long answer(InvocationOnMock invocation) throws Throwable {
            return tick;
         }
      };
   }

   private Answer<Boolean> returnEstablished() {
      return new Answer<Boolean>() {
         @Override
         public Boolean answer(InvocationOnMock invocation) throws Throwable {
            return established;
         }
      };
   }

   @Test
   public void testIsReady() throws IOException {
      assertFalse(sut.isReady());

      advance();

      verify(communicationService).beginConnect("socket", VirtualPayload.class);

      advance();

      assertFalse(sut.isReady());

      established = true;

      advance();

      assertTrue(sut.isReady());
   }

   @Test
   public void testReadNoData() throws IOException {
      established = true;
      advance();
      advance();
      advance();

      assertNull(sut.readIfPossible());
   }

   @Test
   public void testReadData() throws IOException {
      established = true;
      advance();
      advance();
      advance();

      data = new VirtualPayload(10);

      assertSame(data, sut.readIfPossible());
   }

   @Test
   public void testWritedata() throws IOException {
      established = true;
      advance();
      advance();

      sut.request(new Request(10, 20, 30));

      assertEquals(20, data.getSize());
   }

   private void advance() throws IOException {
      sut.executePhase(context);
      tick++;
   }
}
