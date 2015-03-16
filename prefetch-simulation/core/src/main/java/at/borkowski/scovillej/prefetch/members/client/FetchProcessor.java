package at.borkowski.scovillej.prefetch.members.client;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.algorithms.NullAlgorithm;
import at.borkowski.scovillej.prefetch.algorithms.PrefetchAlgorithm;
import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.prefetch.members.aux.RateControlService;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionService;
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

   private long currentStart;
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
            long duration = tick - currentStart;
            owner.getProfilingService().fetched(current, payload.getSize(), tick, duration);
            owner.getCacheProcessor().save(current, tick);

            current = null;
         }
      } else {
         current = null;
         for (Request request : scheduled.keySet())
            if (scheduled.get(request) <= tick && (current == null || scheduled.get(request) < scheduled.get(current)))
               current = request;
         
         scheduled.remove(current);

         if (current != null) {
            System.out.printf("%d -              requesting %d (%d, %d)\n", tick, current.getData(), scheduled.get(current), current.getDeadline());
            owner.getSocketProcessor().request(current);
            rateControlService.setRequestSpecificRate(current.getAvailableByterate());
            currentStart = tick;
         }
      }
   }

   private void reschedule() {
      scheduled = algorithm.schedule(toFetch, ratePredictionService);
   }

   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      rateControlService = context.getService(RateControlService.class);
      ratePredictionService = context.getService(RatePredictionService.class);
      reschedule();
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
}
