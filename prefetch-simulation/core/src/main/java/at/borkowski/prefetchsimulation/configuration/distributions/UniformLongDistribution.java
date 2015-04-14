package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class UniformLongDistribution implements Distribution<Long> {

   private final long min, max, span;

   public UniformLongDistribution(long min, long max) {
      this.min = min;
      this.max = max;
      this.span = max - min;
   }

   @Override
   public Long getValue(RandomSource randomSource) {
      if (span == 0)
         return min;
      long r = ((randomSource.nextLong() % span) + span) % span;
      return min + r;
   }

   @Override
   public Long getMean() {
      return min + (max - min) / 2;
   }

}
