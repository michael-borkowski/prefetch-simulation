package at.borkowski.scovillej.prefetch.members.aux;

import java.util.Collection;
import java.util.Map;

import at.borkowski.scovillej.simulation.PhaseHandler;
import at.borkowski.scovillej.simulation.ServiceProvider;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationEvent;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

public class RatePredictionServiceProvider implements ServiceProvider<RatePredictionService>, RatePredictionService {
   private final Map<Long, Integer> predictions;

   public RatePredictionServiceProvider(Map<Long, Integer> predictions) {
      this.predictions = predictions;
   }

   @Override
   public Collection<SimulationEvent> generateEvents() {
      return null;
   }

   @Override
   public Collection<PhaseHandler> getPhaseHandlers() {
      return null;
   }

   @Override
   public RatePredictionService getService() {
      return this;
   }

   @Override
   public Class<RatePredictionService> getServiceClass() {
      return RatePredictionService.class;
   }

   @Override
   public void initialize(Simulation simulation, SimulationInitializationContext context) {}

   @Override
   public Integer predict(long tick) {
      Long latestBeforeTick = null;

      for (long key : predictions.keySet())
         if (key <= tick && (latestBeforeTick == null || key > latestBeforeTick))
            latestBeforeTick = key;

      if (latestBeforeTick == null)
         return null;
      else
         return predictions.get(latestBeforeTick);
   }
}
