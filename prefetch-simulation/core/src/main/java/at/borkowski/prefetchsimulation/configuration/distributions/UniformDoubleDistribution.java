package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class UniformDoubleDistribution implements Distribution<Double> {

   private final double min, max;

   public UniformDoubleDistribution(double min, double max) {
      this.min = min;
      this.max = max;
   }

   @Override
   public Double getValue(RandomSource randomSource) {
      if (min == max)
         return min;
      double r = randomSource.nextDouble() * (max - min);
      return min + r;
   }

   @Override
   public Double getMean() {
      return min + (max - min) / 2;
   }

}
