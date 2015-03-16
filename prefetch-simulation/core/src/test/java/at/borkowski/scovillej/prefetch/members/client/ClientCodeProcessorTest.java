package at.borkowski.scovillej.prefetch.members.client;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.profiling.PrefetchProfilingService;
import at.borkowski.scovillej.profile.Series;
import at.borkowski.scovillej.simulation.SimulationContext;

public class ClientCodeProcessorTest {

   FetchClient owner;
   ClientCodeProcessor sut;
   PrefetchProfilingService profiling;
   CacheProcessor cacheProcessor;
   FetchProcessor fetchProcessor;

   SimulationContext context;

   long tick = 0;

   @Before
   public void setUp() throws Exception {
      owner = mock(FetchClient.class);
      profiling = mock(PrefetchProfilingService.class);
      cacheProcessor = mock(CacheProcessor.class);
      fetchProcessor = mock(FetchProcessor.class);

      when(owner.getProfilingService()).thenReturn(profiling);
      when(owner.getCacheProcessor()).thenReturn(cacheProcessor);
      when(owner.getFetchProcessor()).thenReturn(fetchProcessor);

      sut = new ClientCodeProcessor(owner);

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
      Request[] requests = { new Request(10, 20, 10), new Request(20, 20, 10), new Request(30, 20, 10) };

      sut.addRequests(Arrays.asList(requests));

      advance();

      // request 0

      advance(9);

      verify(profiling, never()).cacheHit(any(Request.class), any(Long.class));
      verify(profiling, never()).cacheMiss(any(Request.class));
      verify(profiling, never()).lateArrival(any(Request.class));

      advance();

      verify(profiling, never()).cacheHit(any(Request.class), any(Long.class));
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, never()).lateArrival(any(Request.class));

      // request 1

      when(cacheProcessor.hasFile(requests[1])).thenReturn(true);
      when(cacheProcessor.getTimestamp(requests[1])).thenReturn(19L);
      verify(profiling, never()).lateArrival(any(Request.class));

      advance(9);

      verify(profiling, never()).cacheHit(any(Request.class), any(Long.class));
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, never()).lateArrival(any(Request.class));

      advance();

      verify(profiling, times(1)).cacheHit(requests[1], 1L);
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, never()).lateArrival(any(Request.class));

      // request 2

      advance(9);

      verify(profiling, times(1)).cacheHit(requests[1], 1L);
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, never()).lateArrival(any(Request.class));

      advance();

      verify(profiling, times(1)).cacheHit(requests[1], 1L);
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, times(1)).cacheMiss(requests[2]);
      verify(profiling, never()).lateArrival(any(Request.class));
      
      advance();

      when(cacheProcessor.hasFile(requests[2])).thenReturn(true);
      when(cacheProcessor.getTimestamp(requests[2])).thenReturn(31L);
      
      advance();

      verify(profiling, times(1)).cacheHit(requests[1], 1L);
      verify(profiling, times(1)).cacheMiss(requests[0]);
      verify(profiling, times(1)).cacheMiss(requests[2]);
      verify(profiling, times(1)).lateArrival(requests[2]);
   }

   private void advance(int count) throws IOException {
      for (int i = 0; i < count; i++)
         advance();
   }

   private void advance() throws IOException {
      sut.executePhase(context);
      tick++;
   }

}
