package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class UniformIntegerDistribution implements Distribution<Integer> {

   private final int min, max;

   public UniformIntegerDistribution(int min, int max) {
      this.min = min;
      this.max = max;
   }

   @Override
   public Integer getValue(RandomSource randomSource) {
      if (min == max)
         return min;
      long r = randomSource.nextLong() % (max - min);
      return (int) (min + r);
   }

   @Override
   public Integer getMean() {
      return min + (max - min) / 2;
   }

}
