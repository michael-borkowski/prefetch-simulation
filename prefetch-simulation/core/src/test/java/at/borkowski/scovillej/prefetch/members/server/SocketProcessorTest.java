package at.borkowski.scovillej.prefetch.members.server;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.profile.Series;
import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.services.comm.SimulationServerSocket;
import at.borkowski.scovillej.services.comm.SimulationSocket;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;
import at.borkowski.spicej.WouldBlockException;

public class SocketProcessorTest {

   SocketProcessor sut;

   FetchServer owner;
   CommunicationService communicationService;

   SimulationServerSocket<VirtualPayload> serverSocket;
   SimulationSocket<VirtualPayload> clientSocket;

   SimulationContext context;

   long tick = 0;
   boolean available = false;

   @SuppressWarnings("unchecked")
   @Before
   public void setUp() throws Exception {
      owner = mock(FetchServer.class);
      communicationService = mock(CommunicationService.class);

      clientSocket = mock(SimulationSocket.class);

      serverSocket = new SimulationServerSocket<VirtualPayload>() {
         @Override
         public SimulationSocket<VirtualPayload> accept() throws IOException {
            if (available) {
               available = false;
               return clientSocket;
            }
            throw new WouldBlockException();
         }

         @Override
         public int available() {
            return available ? 1 : 0;
         }

         @Override
         public void close() {}
      };
      doReturn(serverSocket).when(communicationService).createServerSocket("socket", VirtualPayload.class);

      sut = new SocketProcessor(owner, "socket");

      sut.initialize(null, new SimulationInitializationContext() {
         @Override
         public <T> T getService(Class<T> clazz) {
            return (T) communicationService;
         }

         @Override
         public <T> Series<T> getSeries(String symbol, Class<T> clazz) {
            return null;
         }
      });

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

      verify(communicationService).createServerSocket("socket", VirtualPayload.class);

      advance();
      advance();
      advance();

      available = true;

      advance();

      assertFalse(available);
      verify(owner).registerClientProcessor(any(ClientProcessor.class));

      advance();
      advance();
      advance();

      available = true;

      advance();

      assertFalse(available);
      verify(owner, times(2)).registerClientProcessor(any(ClientProcessor.class));
   }

   private void advance() throws IOException {
      sut.executePhase(context);
      tick++;
   }

}
