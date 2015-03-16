package at.borkowski.scovillej.prefetch.members.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;

public class FetchServerTest {

   SocketProcessor socketProcessor;

   FetchServer sut;

   @Before
   public void setUp() throws Exception {
      socketProcessor = mock(SocketProcessor.class);

      sut = new FetchServer(socketProcessor);
   }

   @Test
   public void testBasicGetters() {
      assertNotEquals(0, sut.getPhaseHandlers().size());
      assertNull(sut.getPhaseSubcription());
      assertNull(sut.generateEvents());
   }

   @Test
   public void testName() throws Exception {
      Simulation simulation = mock(Simulation.class);
      SimulationContext context = mock(SimulationContext.class);
      sut.initialize(simulation, context);
      verify(socketProcessor).initialize(simulation, context);
   }

   @Test
   public void testExecutePhase() throws IOException {
      Simulation simulation = mock(Simulation.class);
      SimulationContext context = mock(SimulationContext.class);
      sut.initialize(simulation, context);

      sut.executePhase(context);
      verify(socketProcessor).executePhase(context);
   }

   @Test
   public void testExecutePhaseSubHandlers() throws IOException {
      Simulation simulation = mock(Simulation.class);
      SimulationContext context = mock(SimulationContext.class);
      sut.initialize(simulation, context);

      ClientProcessor clientProcessor = mock(ClientProcessor.class);
      sut.registerClientProcessor(clientProcessor);

      sut.executePhase(context);
      verify(socketProcessor).executePhase(context);
      verify(clientProcessor, times(1)).handle(context);
      
      sut.deregisterClientProcessor(clientProcessor);

      sut.executePhase(context);
      verify(clientProcessor, times(1)).handle(context);
      
   }
}
