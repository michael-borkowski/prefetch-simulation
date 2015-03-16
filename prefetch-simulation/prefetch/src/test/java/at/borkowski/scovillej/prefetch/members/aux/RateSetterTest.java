package at.borkowski.scovillej.prefetch.members.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationEvent;

public class RateSetterTest {

   RateSetter sut;

   CommunicationService communicationService;

   @Before
   public void setUp() throws Exception {
      communicationService = mock(CommunicationService.class);
   }

   @Test
   public void test() {
      Map<Long, Integer> limits = new HashMap<>();

      limits.put(0L, 30);
      limits.put(5L, 80);
      limits.put(10L, Integer.MAX_VALUE);
      limits.put(15L, 1);
      limits.put(20L, 0);
      limits.put(25L, 10);
      limits.put(25L, null);

      Set<Long> todo = new HashSet<>(limits.keySet());

      sut = new RateSetter("phase", "socket", limits);

      SimulationContext context = mock(SimulationContext.class);
      when(context.getService(CommunicationService.class)).thenReturn(communicationService);
      sut.initialize(mock(Simulation.class), context);

      assertNull(sut.getPhaseHandlers());

      Collection<SimulationEvent> events = sut.generateEvents();
      assertEquals(limits.size(), events.size());

      for (SimulationEvent event : events) {
         assertTrue(todo.remove(event.getScheduledTick()));
         assertEquals(1, event.getPhaseSubcription().size());
         assertEquals("phase", event.getPhaseSubcription().toArray()[0]);
         event.executePhase(null);
         verify(communicationService).setRates("socket", limits.get(event.getScheduledTick()), limits.get(event.getScheduledTick()));
      }

      assertTrue(todo.isEmpty());
   }

   @Test
   public void testRequestSpecific() {
      Map<Long, Integer> limits = new HashMap<>();

      limits.put(0L, 30);
      limits.put(5L, 80);
      limits.put(10L, null);
      limits.put(15L, 10);

      sut = new RateSetter("phase", "socket", limits);

      SimulationContext context = mock(SimulationContext.class);
      when(context.getService(CommunicationService.class)).thenReturn(communicationService);
      sut.initialize(mock(Simulation.class), context);

      assertNull(sut.getPhaseHandlers());

      Collection<SimulationEvent> events = sut.generateEvents();
      assertEquals(limits.size(), events.size());

      Map<Long, SimulationEvent> eventMap = new HashMap<>();
      for (SimulationEvent event : events)
         eventMap.put(event.getScheduledTick(), event);

      SimulationEvent event;
      Integer global;
      
      event = eventMap.get(0L);
      event.executePhase(null);
      global = limits.get(event.getScheduledTick());
      verify(communicationService).setRates("socket", global, global);
      reset(communicationService);
      
      sut.setRequestSpecificRate(35);
      verify(communicationService).setRates("socket", global, global);
      reset(communicationService);
      
      event = eventMap.get(5L);
      event.executePhase(null);
      global = limits.get(event.getScheduledTick());
      verify(communicationService).setRates("socket", global, 35);
      reset(communicationService);
      
      event = eventMap.get(10L);
      event.executePhase(null);
      global = limits.get(event.getScheduledTick());
      verify(communicationService).setRates("socket", global, 35);
      reset(communicationService);
      
      event = eventMap.get(15L);
      event.executePhase(null);
      global = limits.get(event.getScheduledTick());
      verify(communicationService).setRates("socket", global, global);
      reset(communicationService);
      
      
   }

}
