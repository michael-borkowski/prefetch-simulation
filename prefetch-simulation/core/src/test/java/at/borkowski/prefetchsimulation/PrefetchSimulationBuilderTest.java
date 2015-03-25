package at.borkowski.prefetchsimulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.prefetchsimulation.PrefetchSimulationBuilder.Mocker;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationEvent;

public class PrefetchSimulationBuilderTest {

   PrefetchSimulationBuilder sut;

   Class<? extends PrefetchAlgorithm> algorithm;
   HashMap<String, String> algorithmConfiguration = new HashMap<>();

   HashMap<Long, Integer> limitsReal = new HashMap<>();
   HashMap<Long, Integer> limitsPredicted = new HashMap<>();
   Request request0;
   List<Request> requests = new LinkedList<>();
   List<Request> allRequests = new LinkedList<>();

   PrefetchAlgorithm algorithmInstance;

   @Before
   public void setUp() throws Exception {
      algorithm = NullAlgorithm.class;

      sut = new PrefetchSimulationBuilder();

      limitsReal.put(10L, 100);
      limitsPredicted.put(10L, 95);

      request0 = new Request(1, 2, 3);
      requests.add(new Request(4, 5, 6));
      requests.add(new Request(7, 8, 9));

      allRequests.add(request0);
      allRequests.addAll(requests);
   }

   @Test
   public void test() throws InstantiationException, IllegalAccessException {
      sut.algorithm(algorithmInstance = spy(algorithm.newInstance()));
      sut.algorithmConfiguration(algorithmConfiguration);
      sut.limitReal(13);
      sut.limitsReal(limitsReal);
      sut.limitsPredicted(limitsPredicted);
      sut.request(request0);
      sut.requests(requests);
      sut.totalTicks(10000);

      Simulation simulation = sut.create();

      assertNotNull(sut.getProfiling());

      assertSame(algorithmInstance.getClass(), sut.test__getFetchClient().getFetchProcessor().getAlgorithm().getClass());
      verify(algorithmInstance).configure(algorithmConfiguration);
      assertEquals(13, sut.test__getCommunicationService().getService().getUplinkRate(sut.test__getSocketName()).intValue());
      assertEquals(13, sut.test__getCommunicationService().getService().getDownlinkRate(sut.test__getSocketName()).intValue());

      Collection<SimulationEvent> rateEvents = sut.test__getRateSetter().generateEvents();
      assertEquals(1, rateEvents.size());
      SimulationEvent event = rateEvents.iterator().next();
      assertEquals(10L, event.getScheduledTick());
      RatePredictionService ratePredictionService = sut.test__getRatePredictionServiceProvider().getService();
      assertEquals(null, ratePredictionService.predict(0));
      assertEquals(null, ratePredictionService.predict(9));
      assertEquals(95, ratePredictionService.predict(10).intValue());

      event.executePhase(null);
      assertEquals(100, sut.test__getCommunicationService().getService().getUplinkRate(sut.test__getSocketName()).intValue());
      assertEquals(100, sut.test__getCommunicationService().getService().getDownlinkRate(sut.test__getSocketName()).intValue());

      Request[] pendingRequests = sut.test__getFetchClient().getFetchProcessor().getPendingRequests().toArray(new Request[0]);
      assertEquals(allRequests.size(), pendingRequests.length);
      for (Request expected : allRequests) {
         boolean found = false;
         for (Request r : pendingRequests)
            if (expected == r)
               found = true;
         assertTrue(found);
      }
      assertEquals(10000, simulation.getTotalTicks());
   }

   @Test
   public void testFromGenesis() {
      Genesis genesis = new Genesis(10000, allRequests, limitsReal, limitsPredicted, algorithm, algorithmConfiguration, 5000);
      PrefetchSimulationBuilder sut = PrefetchSimulationBuilder.__test_fromGenesisMockAlgorithm(genesis, spyAlgorithm());
      algorithmInstance = sut.test__getFetchClient().getFetchProcessor().getAlgorithm();
      sut.limitReal(13);
      Simulation simulation = sut.create();

      assertNotNull(sut.getProfiling());

      verify(algorithmInstance).configure(algorithmConfiguration);
      assertEquals(5000, sut.test__getFetchClient().getFetchProcessor().getLookAheadTime());
      assertEquals(13, sut.test__getCommunicationService().getService().getUplinkRate(sut.test__getSocketName()).intValue());
      assertEquals(13, sut.test__getCommunicationService().getService().getDownlinkRate(sut.test__getSocketName()).intValue());

      Collection<SimulationEvent> rateEvents = sut.test__getRateSetter().generateEvents();
      assertEquals(1, rateEvents.size());
      SimulationEvent event = rateEvents.iterator().next();
      assertEquals(10L, event.getScheduledTick());
      RatePredictionService ratePredictionService = sut.test__getRatePredictionServiceProvider().getService();
      assertEquals(null, ratePredictionService.predict(0));
      assertEquals(null, ratePredictionService.predict(9));
      assertEquals(95, ratePredictionService.predict(10).intValue());

      event.executePhase(null);
      assertEquals(100, sut.test__getCommunicationService().getService().getUplinkRate(sut.test__getSocketName()).intValue());
      assertEquals(100, sut.test__getCommunicationService().getService().getDownlinkRate(sut.test__getSocketName()).intValue());

      Request[] pendingRequests = sut.test__getFetchClient().getFetchProcessor().getPendingRequests().toArray(new Request[0]);
      assertEquals(allRequests.size(), pendingRequests.length);
      for (Request expected : allRequests) {
         boolean found = false;
         for (Request r : pendingRequests)
            if (expected == r)
               found = true;
         assertTrue(found);
      }
      assertEquals(10000, simulation.getTotalTicks());
   }

   private Mocker<PrefetchAlgorithm> spyAlgorithm() {
      return new Mocker<PrefetchAlgorithm>() {
         @Override
         public PrefetchAlgorithm mock(PrefetchAlgorithm t) {
            return spy(t);
         }
      };
   }
}
