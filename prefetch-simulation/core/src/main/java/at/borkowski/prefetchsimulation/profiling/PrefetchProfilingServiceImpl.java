package at.borkowski.prefetchsimulation.profiling;

import java.util.Collection;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.scovillej.profile.Series;
import at.borkowski.scovillej.profile.SeriesResult;
import at.borkowski.scovillej.simulation.PhaseHandler;
import at.borkowski.scovillej.simulation.ServiceProvider;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationEvent;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

/**
 * Implements the prefetch profiling (see {@link PrefetchProfilingService},
 * {@link PrefetchProfilingResults}).
 * 
 * @author michael
 *
 */
public class PrefetchProfilingServiceImpl implements PrefetchProfilingService, PrefetchProfilingResults, ServiceProvider<PrefetchProfilingService> {

   private static String RESPONSE_TIME = "prefetch-profiling-response-time";
   private static String DATA_AGE = "prefetch-profiling-data-age";
   private static String DATA_VOLUME = "prefetch-profiling-data-volume";
   private static String HIT = "prefetch-profiling-cache-hit";

   private Simulation simulation;

   private Series<Long> seriesResponseTime;
   private Series<Long> seriesDataAge;
   private Series<Long> seriesDataVolume;
   private Series<Void> seriesHits;

   @Override
   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      this.simulation = simulation;
      seriesResponseTime = context.getSeries(RESPONSE_TIME, Long.class);
      seriesDataAge = context.getSeries(DATA_AGE, Long.class);
      seriesDataVolume = context.getSeries(DATA_VOLUME, Long.class);
      seriesHits = context.getSeries(HIT, Void.class);
   }

   @Override
   public Collection<PhaseHandler> getPhaseHandlers() {
      return null;
   }

   @Override
   public Collection<SimulationEvent> generateEvents() {
      return null;
   }

   @Override
   public PrefetchProfilingService getService() {
      return this;
   }

   @Override
   public Class<PrefetchProfilingService> getServiceClass() {
      return PrefetchProfilingService.class;
   }

   @Override
   public void cacheHit(Request request) {
      seriesHits.measure(null);
   }

   public void arrival(Request request, long responseTime, long dataAge, int dataVolume) {
      System.out.printf("%d - arrival rt %d da %d dv %d\n", simulation.getCurrentTick(), responseTime, dataAge, dataVolume);
      seriesResponseTime.measure(responseTime);
      seriesDataAge.measure(dataAge);
      seriesDataVolume.measure((long) dataVolume);
   }

   @Override
   public SeriesResult<Long> getResponseTime() {
      return simulation.getSeries(RESPONSE_TIME, Long.class);
   }

   @Override
   public SeriesResult<Long> getDataAge() {
      return simulation.getSeries(DATA_AGE, Long.class);
   }

   @Override
   public SeriesResult<Long> getDataVolume() {
      return simulation.getSeries(DATA_VOLUME, Long.class);
   }

   @Override
   public SeriesResult<Void> getCacheHits() {
      return simulation.getSeries(HIT, Void.class);
   }
}
