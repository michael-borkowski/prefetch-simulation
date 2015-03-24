package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class UniformLongDistribution implements Distribution<Long> {

   private final long min, max;

   public UniformLongDistribution(long min, long max) {
      this.min = min;
      this.max = max;
   }

   @Override
   public Long getValue(RandomSource randomSource) {
      if (min == max)
         return min;
      long r = randomSource.nextLong() % (max - min);
      return min + r;
   }

   @Override
   public Long getMean() {
      return min + (max - min) / 2;
   }

}
