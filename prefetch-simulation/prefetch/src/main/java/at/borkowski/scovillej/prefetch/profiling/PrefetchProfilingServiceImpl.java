package at.borkowski.scovillej.prefetch.profiling;

import java.util.Collection;

import at.borkowski.scovillej.prefetch.Request;
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

   private static String URT = "prefetch-profiling-urt";
   private static String OVERDUE = "prefetch-profiling-overdue";
   private static String AGE = "prefetch-profiling-age";
   private static String MISS = "prefetch-profiling-miss";
   private static String STRETCH = "prefetch-profiling-stretch";

   private Simulation simulation;

   private Series<Long> seriesURT;
   private Series<Long> seriesOverdue;
   private Series<Long> seriesAge;
   private Series<Void> seriesMisses;
   private Series<Double> seriesStretch;

   @Override
   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      this.simulation = simulation;
      seriesURT = context.getSeries(URT, Long.class);
      seriesOverdue = context.getSeries(OVERDUE, Long.class);
      seriesAge = context.getSeries(AGE, Long.class);
      seriesMisses = context.getSeries(MISS, Void.class);
      seriesStretch = context.getSeries(STRETCH, Double.class);
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
   public void fetched(Request request, int actualSize, long tick, long duration) {
      long overdue = simulation.getCurrentTick() - request.getDeadline();
      System.out.printf("%d - fetched %d (overdue %d) (%d B in %d t, %.2f B/t\n", tick, request.getData(), overdue, actualSize, duration, (double) actualSize / duration);
      overdue = Math.max(0, overdue);
      seriesURT.measure(overdue);
      seriesStretch.measure(1000.0 * overdue / request.getData());
   }

   @Override
   public void cacheHit(Request request, long age) {
      System.out.printf("%d - cache hit for %d (age %d)\n", simulation.getCurrentTick(), request.getData(), age);
      seriesAge.measure(age);
   }

   @Override
   public void cacheMiss(Request request) {
      System.out.printf("%d - cache miss for %d\n", simulation.getCurrentTick(), request.getData());
      seriesMisses.measure(null);
   }

   @Override
   public void lateArrival(Request request) {
      long overdue = simulation.getCurrentTick() - request.getDeadline();
      System.out.printf("%d - late arrival for %d (overdue %d)\n", simulation.getCurrentTick(), request.getData(), overdue);
      seriesOverdue.measure(overdue);
   }

   @Override
   public SeriesResult<Long> getCacheHitAges() {
      return simulation.getSeries(AGE, Long.class);
   }

   @Override
   public SeriesResult<Void> getCacheMisses() {
      return simulation.getSeries(MISS, Void.class);
   }

   @Override
   public SeriesResult<Long> getOverdue() {
      return simulation.getSeries(OVERDUE, Long.class);
   }

   @Override
   public SeriesResult<Long> getURT() {
      return simulation.getSeries(URT, Long.class);
   }

   @Override
   public SeriesResult<Double> getStretch() {
      return simulation.getSeries(STRETCH, Double.class);
   }
}
