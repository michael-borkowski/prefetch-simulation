package at.borkowski.prefetchsimulation.members.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.impl.VirtualPayload;
import at.borkowski.prefetchsimulation.members.aux.RateControlService;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingService;
import at.borkowski.scovillej.profile.Series;
import at.borkowski.scovillej.simulation.SimulationContext;

public class FetchProcessor_LookAheadTest {

   FetchClient owner;
   FetchProcessor sut;

   Request[] requests;

   Set<Request> scheduledRequests = new HashSet<>();

   SocketProcessor socketProcessor;
   PrefetchProfilingService profilingService;
   CacheProcessor cacheProcessor;

   SimulationContext context;
   long tick = 0;

   VirtualPayload data = null;

   @Before
   public void setUp() throws Exception {
      socketProcessor = mock(SocketProcessor.class);
      profilingService = mock(PrefetchProfilingService.class);
      cacheProcessor = mock(CacheProcessor.class);

      owner = mock(FetchClient.class);

      when(owner.getCacheProcessor()).thenReturn(cacheProcessor);
      when(owner.getSocketProcessor()).thenReturn(socketProcessor);
      when(owner.getProfilingService()).thenReturn(profilingService);
      when(socketProcessor.readIfPossible()).then(returnData());

      sut = new FetchProcessor(owner);
      sut.setLookAheadTime(10);
      sut.setAlgorithm(new PrefetchAlgorithm() {
         @Override
         public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
            Map<Request, Long> ret = new HashMap<>();
            for (Request req : requests)
               ret.put(req, req.getDeadline() + req.getData());
            scheduledRequests.addAll(requests);
            return ret;
         }
      });

      requests = new Request[] { new Request(100, 10, 1), new Request(200, 13, 1) };

      context = new SimulationContext() {
         @SuppressWarnings("unchecked")
         @Override
         public <T> T getService(Class<T> clazz) {
            if (clazz.equals(RateControlService.class))
               return (T) mock(RateControlService.class);
            else if (clazz.equals(RatePredictionService.class))
               return (T) mock(RatePredictionService.class);
            throw new RuntimeException();
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

      sut.addRequests(Arrays.asList(requests));
      sut.initialize(null, context);
   }

   private Answer<VirtualPayload> returnData() {
      return new Answer<VirtualPayload>() {
         public VirtualPayload answer(InvocationOnMock invocation) throws Throwable {
            VirtualPayload ret = data;
            data = null;
            return ret;
         }
      };
   }

   @Test
   public void test() throws IOException {
      advance();

      assertFalse(scheduledRequests.contains(requests[0]));
      assertFalse(scheduledRequests.contains(requests[1]));
      
      advanceUntil(90);

      assertFalse(scheduledRequests.contains(requests[0]));
      assertFalse(scheduledRequests.contains(requests[1]));

      advance();

      assertTrue(scheduledRequests.contains(requests[0]));
      assertFalse(scheduledRequests.contains(requests[1]));

      advanceUntil(190);

      assertTrue(scheduledRequests.contains(requests[0]));
      assertFalse(scheduledRequests.contains(requests[1]));

      advance();

      assertTrue(scheduledRequests.contains(requests[0]));
      assertTrue(scheduledRequests.contains(requests[1]));
   }

   private void advanceUntil(int tick) throws IOException {
      while (this.tick < tick)
         advance();
   }

   private void advance() throws IOException {
      sut.executePhase(context);
      tick++;
   }

}
