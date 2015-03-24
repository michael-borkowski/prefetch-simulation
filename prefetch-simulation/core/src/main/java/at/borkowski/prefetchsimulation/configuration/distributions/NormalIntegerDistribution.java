package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class NormalIntegerDistribution implements Distribution<Integer> {

   private final int mean, sd;

   public NormalIntegerDistribution(int mean, int sd) {
      this.mean = mean;
      this.sd = sd;
   }

   @Override
   public Integer getValue(RandomSource randomSource) {
      if (sd == 0)
         return mean;
      return (int) (randomSource.nextGaussian() * sd + mean);
   }
}
