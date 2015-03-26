package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class NormalDoubleDistribution implements Distribution<Double> {

   private final double mean, sd;

   public NormalDoubleDistribution(double mean, double sd) {
      this.mean = mean;
      this.sd = sd;
   }

   @Override
   public Double getValue(RandomSource randomSource) {
      if (sd == 0)
         return mean;
      return (double) (randomSource.nextGaussian() * sd + mean);
   }
   
   @Override
   public Double getMean() {
      return mean;
   }
}
