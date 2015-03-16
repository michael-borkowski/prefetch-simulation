package at.borkowski.scovillej.prefetch.members.client;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.profiling.PrefetchProfilingService;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;

public class FetchClientTest {

   FetchClient sut;

   SocketProcessor socketProcessor;
   ClientCodeProcessor clientCodeProcessor;
   CacheProcessor cacheProcessor;
   FetchProcessor fetchProcessor;

   PrefetchProfilingService profilingService;
   SimulationContext context;

   @Before
   public void setUp() throws Exception {
      socketProcessor = mock(SocketProcessor.class);
      clientCodeProcessor = mock(ClientCodeProcessor.class);
      cacheProcessor = mock(CacheProcessor.class);
      fetchProcessor = mock(FetchProcessor.class);

      profilingService = mock(PrefetchProfilingService.class);
      context = mock(SimulationContext.class);

      sut = new FetchClient(socketProcessor, clientCodeProcessor, cacheProcessor, fetchProcessor);

      when(context.getService(PrefetchProfilingService.class)).thenReturn(profilingService);
   }

   @Test
   public void testBasicGetters() throws Exception {
      sut.initialize(null, context);

      assertSame(cacheProcessor, sut.getCacheProcessor());
      assertSame(fetchProcessor, sut.getFetchProcessor());
      assertSame(socketProcessor, sut.getSocketProcessor());
      assertSame(sut.getProfilingService(), sut.getProfilingService());
      assertNotEquals(0, sut.getPhaseHandlers().size());
      assertNull(sut.getPhaseSubcription());
      assertNull(sut.generateEvents());
   }

   @Test
   public void testInitialize() throws Exception {
      Simulation simulation = mock(Simulation.class);
      sut.initialize(simulation, context);

      verify(socketProcessor).initialize(simulation, context);
      verify(fetchProcessor).initialize(simulation, context);
   }

   @Test
   public void testExecutePhase_notReady() throws IOException {
      sut.initialize(null, context);
      sut.executePhase(context);

      verify(socketProcessor).executePhase(context);
      verify(fetchProcessor, never()).executePhase(any(SimulationContext.class));
      verify(clientCodeProcessor, never()).executePhase(any(SimulationContext.class));
   }

   @Test
   public void testExecutePhase_ready() throws IOException {
      sut.initialize(null, context);
      when(socketProcessor.isReady()).thenReturn(true);
      sut.executePhase(context);

      verify(socketProcessor).executePhase(context);
      verify(fetchProcessor).executePhase(any(SimulationContext.class));
      verify(clientCodeProcessor).executePhase(any(SimulationContext.class));
   }

   @Test
   public void testAddRequests() throws Exception {
      List<Request> requests = new LinkedList<>();
      requests.add(new Request(1, 2, 3));
      requests.add(new Request(4, 5, 6));
      sut.addRequests(requests);

      verify(fetchProcessor).addRequests(requests);
      verify(clientCodeProcessor).addRequests(requests);
   }

}
