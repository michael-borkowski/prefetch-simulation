package at.borkowski.scovillej.prefetch.profiling;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import at.borkowski.scovillej.impl.series.DoubleSeriesImpl;
import at.borkowski.scovillej.impl.series.LongSeriesImpl;
import at.borkowski.scovillej.impl.series.VoidSeriesImpl;
import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.profile.SeriesProvider;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;

public class PrefetchProfilingServiceImplTest {

   Simulation simulation;
   SimulationContext context;

   PrefetchProfilingServiceImpl sut;

   Map<String, SeriesProvider<?>> series = new HashMap<>();
   Map<String, Class<?>> types = new HashMap<>();

   @Before
   public void setUp() throws Exception {
      simulation = mock(Simulation.class);
      context = mock(SimulationContext.class);

      when(context.getSeries(anyString(), (Class<?>) any())).then(handleGetSeriesFromContext());
      when(simulation.getSeries(anyString(), (Class<?>) any())).then(handleGetSeriesFromContext());

      sut = new PrefetchProfilingServiceImpl();
      sut.initialize(simulation, context);
   }

   private Answer<SeriesProvider<?>> handleGetSeriesFromContext() {
      return new Answer<SeriesProvider<?>>() {
         @Override
         public SeriesProvider<?> answer(InvocationOnMock invocation) throws Throwable {
            String name = (String) invocation.getArguments()[0];
            Class<?> clazz = (Class<?>) invocation.getArguments()[1];

            if (!series.containsKey(name)) {
               series.put(name, createSeries(clazz));
               types.put(name, clazz);

               series.get(name).initialize(simulation);
            }

            if (clazz.equals(types.get(name)))
               return series.get(name);
            else
               throw new RuntimeException("invalid type: " + clazz + " != " + types.get(name));
         }
      };
   }

   private SeriesProvider<?> createSeries(Class<?> clazz) {
      if (clazz.equals(Long.class))
         return new LongSeriesImpl();
      else if (clazz.equals(Void.class))
         return new VoidSeriesImpl();
      else if (clazz.equals(Double.class))
         return new DoubleSeriesImpl();
      else
         throw new RuntimeException("unsupported type: " + clazz);
   }

   @Test
   public void testBasicGetters() {
      assertNull(sut.getPhaseHandlers());
      assertNull(sut.generateEvents());
      assertSame(sut, sut.getService());
      assertEquals(PrefetchProfilingService.class, sut.getServiceClass());
   }

   @Test
   public void testFetched() throws Exception {
      sut.fetched(new Request(1, 2, 3), 4, 5, 6);
      assertEquals(1, sut.getURT().getCount());
      assertEquals(1, sut.getStretch().getCount());
   }

   @Test
   public void testCacheHit() throws Exception {
      sut.cacheHit(new Request(1, 2, 3), 4);
      assertEquals(1, sut.getCacheHitAges().getCount());
   }

   @Test
   public void testCacheMiss() throws Exception {
      sut.cacheMiss(new Request(1, 2, 3));
      assertEquals(1, sut.getCacheMisses().getCount());
   }

   @Test
   public void testLateArrival() throws Exception {
      sut.lateArrival(new Request(1, 2, 3));
      assertEquals(1, sut.getOverdue().getCount());
   }

}
