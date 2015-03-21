package at.borkowski.prefetchsimulation.configuration;

import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;

public class RequestSeries {
   private final Distribution<Long> interval, startTick, endTick;
   private final Distribution<Integer> size, byterate;

   public RequestSeries(Distribution<Long> interval, Distribution<Integer> size, Distribution<Integer> byterate, Distribution<Long> startTick, Distribution<Long> endTick) {
      this.interval = interval;
      this.size = size;
      this.startTick = startTick;
      this.endTick = endTick;
      this.byterate = byterate;
   }

   public Distribution<Long> getEndTick() {
      return endTick;
   }

   public Distribution<Long> getInterval() {
      return interval;
   }

   public Distribution<Integer> getSize() {
      return size;
   }

   public Distribution<Long> getStartTick() {
      return startTick;
   }

   public Distribution<Integer> getByterate() {
      return byterate;
   }
}
