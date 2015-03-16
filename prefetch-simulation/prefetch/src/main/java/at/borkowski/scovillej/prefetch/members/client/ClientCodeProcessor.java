package at.borkowski.scovillej.prefetch.members.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.simulation.SimulationContext;

/**
 * Represents the client code sub-processor of {@link FetchClient}. It is
 * responsible for simulation client code behavior.
 */
public class ClientCodeProcessor {

   private final FetchClient owner;

   private Set<Request> required = new HashSet<>();
   private Set<Request> missed = new HashSet<>();

   /**
    * Creates a new client code processor with the given owner
    * 
    * @param owner
    *           the owner
    */
   public ClientCodeProcessor(FetchClient owner) {
      this.owner = owner;
   }

   /**
    * Executes the client code processor's business logic.
    * 
    * @param context
    *           the execution context
    */
   public void executePhase(SimulationContext context) {
      long tick = context.getCurrentTick();

      Set<Request> done = new HashSet<>();
      for (Request request : required) {
         if (request.getDeadline() <= tick) {
            if (owner.getCacheProcessor().hasFile(request)) {
               if (request.getDeadline() == tick)
                  owner.getProfilingService().cacheHit(request, tick - owner.getCacheProcessor().getTimestamp(request));
               else
                  owner.getProfilingService().lateArrival(request);
               done.add(request);
            } else if (!missed.contains(request)) {
               owner.getProfilingService().cacheMiss(request);
               owner.getFetchProcessor().urge(tick, request);
               missed.add(request);
            }
         }
      }

      required.removeAll(done);
   }

   /**
    * Adds new requests to the list of required requests
    * 
    * @param requests
    *           the new requests
    */
   public void addRequests(Collection<Request> requests) {
      required.addAll(requests);
   }

}
