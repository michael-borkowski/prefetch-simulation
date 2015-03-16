package at.borkowski.scovillej.prefetch.members.client;

import java.util.Arrays;
import java.util.Collection;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.profiling.PrefetchProfilingService;
import at.borkowski.scovillej.simulation.PhaseHandler;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationEvent;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;
import at.borkowski.scovillej.simulation.SimulationMember;

/**
 * Represents the client member of the simulation, which performs prefetching
 * and caching. This component is usually the prefetching middleware in real
 * applications; in this simulation component, the client code is simulated by a
 * sub-processor (see {@link ClientCodeProcessor}).
 */
public class FetchClient implements SimulationMember, PhaseHandler {

   private final SocketProcessor socketProcessor;
   private final ClientCodeProcessor clientCodeProcessor;
   private final CacheProcessor cacheProcessor;
   private final FetchProcessor fetchProcessor;

   private PrefetchProfilingService profiling;

   /**
    * Testability constructor.
    * 
    * @param socketProcessor
    *           the (mocked) socket processor
    * @param clientCodeProcessor
    *           the (mocked) client code processor
    * @param cacheProcessor
    *           the (mocked) cache processor
    * @param fetchProcessor
    *           the (mocked) fetch processor
    */
   FetchClient(SocketProcessor socketProcessor, ClientCodeProcessor clientCodeProcessor, CacheProcessor cacheProcessor, FetchProcessor fetchProcessor) {
      this.socketProcessor = socketProcessor;
      this.clientCodeProcessor = clientCodeProcessor;
      this.cacheProcessor = cacheProcessor;
      this.fetchProcessor = fetchProcessor;
   }

   /**
    * Creates a new fetch client.
    * 
    * @param socketName the socket to use
    */
   public FetchClient(String socketName) {
      socketProcessor = new SocketProcessor(socketName);
      clientCodeProcessor = new ClientCodeProcessor(this);
      cacheProcessor = new CacheProcessor();
      fetchProcessor = new FetchProcessor(this);
   }

   @Override
   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      socketProcessor.initialize(simulation, context);
      fetchProcessor.initialize(simulation, context);

      profiling = context.getService(PrefetchProfilingService.class);
   }

   @Override
   public Collection<SimulationEvent> generateEvents() {
      return null;
   }

   @Override
   public Collection<PhaseHandler> getPhaseHandlers() {
      return Arrays.asList(this);
   }

   @Override
   public void executePhase(SimulationContext context) {
      try {
         socketProcessor.executePhase(context);

         if (!socketProcessor.isReady())
            return;

         clientCodeProcessor.executePhase(context);
         fetchProcessor.executePhase(context);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public Collection<String> getPhaseSubcription() {
      return null;
   }

   /**
    * Adds requests to the client.
    * 
    * @param requests
    *           the new requests
    */
   public void addRequests(Collection<Request> requests) {
      clientCodeProcessor.addRequests(requests);
      fetchProcessor.addRequests(requests);
   }

   /**
    * Returns the profiling service for this client, using which profiling can
    * be performed.
    * 
    * @return the profiling service
    */
   public PrefetchProfilingService getProfilingService() {
      return profiling;
   }

   /**
    * Returns the cache sub-processor.
    * 
    * @return the cache sub-processor
    */
   public CacheProcessor getCacheProcessor() {
      return cacheProcessor;
   }

   /**
    * Returns the socket sub-processor.
    * 
    * @return the socket sub-processsor
    */
   public SocketProcessor getSocketProcessor() {
      return socketProcessor;
   }

   /**
    * Returns the fetch sub-processor.
    * 
    * @return the fetch sub-processor.
    */
   public FetchProcessor getFetchProcessor() {
      return fetchProcessor;
   }
}
