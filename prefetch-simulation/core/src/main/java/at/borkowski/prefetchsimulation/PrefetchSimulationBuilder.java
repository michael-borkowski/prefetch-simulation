package at.borkowski.prefetchsimulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.genesis.Genesis;
import at.borkowski.prefetchsimulation.internal.VirtualPayloadSerializer;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionServiceProvider;
import at.borkowski.prefetchsimulation.members.aux.RateSetter;
import at.borkowski.prefetchsimulation.members.client.FetchClient;
import at.borkowski.prefetchsimulation.members.server.FetchServer;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingResults;
import at.borkowski.prefetchsimulation.profiling.PrefetchProfilingServiceImpl;
import at.borkowski.scovillej.SimulationBuilder;
import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.services.comm.CommunicationServiceBuilder;
import at.borkowski.scovillej.simulation.ServiceProvider;
import at.borkowski.scovillej.simulation.Simulation;

/**
 * A class facilitating the creation of simulations of prefetching scenarios.
 */
public class PrefetchSimulationBuilder {
   private static final String RATE_PHASE = "rate";
   private static final String COMM_PHASE = "comm";

   private static final String SOCKET_NAME = "fetch";

   private static final long BASE_DELAY = 2;
   private final FetchClient fetchClient;
   private final PrefetchProfilingServiceImpl profilingService;
   private final ServiceProvider<CommunicationService> communicationService;
   private Map<Long, Integer> limitsReal = null;
   private Map<Long, Integer> limitsPredicted = null;
   
   private Map<String, String> algorithmConfiguration = new HashMap<>();

   private SimulationBuilder builder = new SimulationBuilder();

   private RateSetter rateSetter;
   private RatePredictionServiceProvider ratePredictionServiceProvider;

   private Simulation simulation = null;

   /**
    * Creates a new builder with all parameters set to default.
    */
   public PrefetchSimulationBuilder() {
      builder.phase(RATE_PHASE);
      builder.phase(Simulation.TICK_PHASE);
      builder.phase(COMM_PHASE);

      CommunicationServiceBuilder commBuilder = new CommunicationServiceBuilder().communicationPhase(COMM_PHASE).delay(SOCKET_NAME, BASE_DELAY).serializer(new VirtualPayloadSerializer());

      builder.service(communicationService = commBuilder.create());
      builder.service(profilingService = new PrefetchProfilingServiceImpl());

      builder.member(new FetchServer(SOCKET_NAME));
      builder.member(fetchClient = new FetchClient(SOCKET_NAME));
   }

   public static PrefetchSimulationBuilder fromGenesis(Genesis genesis) {
      return fromGenesis(genesis, getAlgorithm(genesis));
   }

   private static PrefetchAlgorithm getAlgorithm(Genesis genesis) {
      try {
         PrefetchAlgorithm algorithm = genesis.getAlgorithm().newInstance();
         return algorithm;
      } catch (InstantiationException | IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   private static PrefetchSimulationBuilder fromGenesis(Genesis genesis, PrefetchAlgorithm algorithm) {
      PrefetchSimulationBuilder builder = new PrefetchSimulationBuilder();
      builder.totalTicks(genesis.getTicks());
      builder.requests(genesis.getRequests());
      builder.limitsReal(genesis.getRateReal());
      builder.limitsPredicted(genesis.getRatePredicted());
      builder.algorithm(algorithm);
      builder.algorithmConfiguration(genesis.getAlgorithmConfiguration());
      builder.lookAheadTime(genesis.getLookAheadTime());

      return builder;
   }

   /**
    * Creates the simulation. Must not be called more than once.
    * 
    * @return the simulation
    */
   public Simulation create() {
      if (simulation != null)
         throw new IllegalStateException();

      builder.service(rateSetter = new RateSetter(RATE_PHASE, SOCKET_NAME, limitsReal));
      builder.service(ratePredictionServiceProvider = new RatePredictionServiceProvider(limitsPredicted));
      return simulation = builder.create();
   }

   /**
    * Adds an initial rate limit to the communication socket.
    * 
    * @param byteRate
    *           the rate in bytes per seconds, or <code>null</code> for no limit
    * @return this object
    */
   public PrefetchSimulationBuilder limitReal(Integer byteRate) {
      communicationService.getService().setRates(SOCKET_NAME, byteRate, byteRate);
      return this;
   }

   /**
    * Sets limitsReal at given points in time in the simulation.
    * 
    * @param byteRates
    *           the map of ticks to limitsReal
    * @return this object
    */
   public PrefetchSimulationBuilder limitsReal(Map<Long, Integer> byteRates) {
      this.limitsReal = byteRates;
      return this;
   }

   public PrefetchSimulationBuilder limitsPredicted(Map<Long, Integer> byteRates) {
      this.limitsPredicted = byteRates;
      return this;
   }

   /**
    * Adds a request object to the simulation.
    * 
    * @param request
    *           the request
    * @return this object
    */
   public PrefetchSimulationBuilder request(Request request) {
      fetchClient.addRequests(Arrays.asList(request));
      return this;
   }

   /**
    * Adds requests to the simulation.
    * 
    * @param requests
    *           the requests
    * @return this object
    */
   public PrefetchSimulationBuilder requests(Collection<Request> requests) {
      fetchClient.addRequests(requests);
      return this;
   }

   /**
    * Sets the total number of ticks for this simulation. See
    * {@link SimulationBuilder#totalTicks(long)}.
    * 
    * @param tickCount
    *           the number of total ticks
    * @return this object
    */
   public PrefetchSimulationBuilder totalTicks(long tickCount) {
      builder.totalTicks(tickCount);
      return this;
   }

   /**
    * Sets the scheduling algorithm.
    * 
    * @param algorithm
    *           the scheduling algorithm
    * @return this object
    */
   public PrefetchSimulationBuilder algorithm(PrefetchAlgorithm algorithm) {
      algorithm.configure(algorithmConfiguration);
      fetchClient.getFetchProcessor().setAlgorithm(algorithm);
      return this;
   }

   public void algorithmConfiguration(Map<String, String> algorithmConfiguration) {
      fetchClient.getFetchProcessor().getAlgorithm().configure(this.algorithmConfiguration = algorithmConfiguration);
   }

   public PrefetchSimulationBuilder lookAheadTime(long lookAheadTime) {
      fetchClient.getFetchProcessor().setLookAheadTime(lookAheadTime);
      return this;
   }

   /**
    * Returns the profiling result object.
    * 
    * @return the profiling result object
    */
   public PrefetchProfilingResults getProfiling() {
      return profilingService;
   }

   /**
    * Testability method.
    * 
    * @return the fetch client
    */
   FetchClient test__getFetchClient() {
      return fetchClient;
   }

   /**
    * Testability method.
    * 
    * @return the communication service
    */
   ServiceProvider<CommunicationService> test__getCommunicationService() {
      return communicationService;
   }

   /**
    * Testability method.
    * 
    * @return socket name
    */
   String test__getSocketName() {
      return SOCKET_NAME;
   }

   /**
    * Testability method
    * 
    * @return rate setter
    */
   RateSetter test__getRateSetter() {
      return rateSetter;
   }

   RatePredictionServiceProvider test__getRatePredictionServiceProvider() {
      return ratePredictionServiceProvider;
   }

   static PrefetchSimulationBuilder __test_fromGenesisMockAlgorithm(Genesis genesis, Mocker<PrefetchAlgorithm> mocker) {
      return fromGenesis(genesis, mocker.mock(getAlgorithm(genesis)));
   }

   public interface Mocker<T> {
      T mock(T t);
   }
}
