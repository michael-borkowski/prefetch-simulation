package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class UniformIntegerDistribution implements Distribution<Integer> {

   private final int min, max, span;

   public UniformIntegerDistribution(int min, int max) {
      this.min = min;
      this.max = max;
      this.span = max - min;
   }

   @Override
   public Integer getValue(RandomSource randomSource) {
      if (span == 0)
         return min;
      long r = ((randomSource.nextLong() % span) + span) % span;
      return (int) (min + r);
   }

   @Override
   public Integer getMean() {
      return min + (max - min) / 2;
   }

}
