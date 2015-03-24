package at.borkowski.prefetchsimulation.members.client;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.NullAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.internal.VirtualPayload;
import at.borkowski.prefetchsimulation.members.aux.RateControlService;
import at.borkowski.prefetchsimulation.members.aux.RatePredictionService;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

/**
 * Represents the fetch sub-processor of {@link FetchClient}. It is responsible
 * for fetching requests.
 */
public class FetchProcessor {

   private final FetchClient owner;
   private RateControlService rateControlService;
   private RatePredictionService ratePredictionService;

   private final Set<Request> toFetch = new HashSet<>();
   private Map<Request, Long> scheduled = new HashMap<>();

   private PrefetchAlgorithm algorithm = new NullAlgorithm();
   private long lookAheadTime = Long.MAX_VALUE;

   private Request current = null;

   public FetchProcessor(FetchClient owner) {
      this.owner = owner;
   }

   public void executePhase(SimulationContext context) throws IOException {
      long tick = context.getCurrentTick();

      if (current != null) {
         VirtualPayload payload = owner.getSocketProcessor().readIfPossible();
         if (payload != null) {
            rateControlService.setRequestSpecificRate(null);
            owner.getCacheProcessor().save(current, tick);
            toFetch.remove(current);
            scheduled.remove(current);

            current = null;
         }
      } else {
         current = null;
         for (Request request : scheduled.keySet())
            if (scheduled.get(request) <= tick && (current == null || scheduled.get(request) < scheduled.get(current)))
               current = request;

         Long scheduledTick = scheduled.remove(current);

         if (current != null) {
            System.out.printf("%d -              requesting %d (%d, %d)\n", tick, current.getData(), scheduledTick, current.getDeadline());
            owner.getSocketProcessor().request(current);
            rateControlService.setRequestSpecificRate(current.getAvailableByterate());
         }
      }

      reschedule(tick, false);
   }

   private void reschedule(long tick, boolean force) {
      HashSet<Request> current = new HashSet<>();
      for (Request req : toFetch)
         if (req.getDeadline() - tick <= lookAheadTime)
            current.add(req);

      boolean newRequest = force;
      if (!force)
         for (Request req : current)
            newRequest |= !scheduled.containsKey(req);

      if (newRequest)
         scheduled = algorithm.schedule(current, ratePredictionService);
   }

   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      rateControlService = context.getService(RateControlService.class);
      ratePredictionService = context.getService(RatePredictionService.class);
      reschedule(0, true);
   }

   public void addRequests(Collection<Request> requests) {
      toFetch.addAll(requests);
   }

   public void setAlgorithm(PrefetchAlgorithm algorithm) {
      this.algorithm = algorithm;
   }

   public void urge(long tick, Request request) {
      scheduled.put(request, tick);
   }

   public PrefetchAlgorithm getAlgorithm() {
      return algorithm;
   }

   public Set<Request> getPendingRequests() {
      return toFetch;
   }

   public long getLookAheadTime() {
      return lookAheadTime;
   }

   public void setLookAheadTime(long lookAheadTime) {
      this.lookAheadTime = lookAheadTime;
   }
}
