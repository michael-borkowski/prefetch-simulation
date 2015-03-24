package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class NormalLongDistribution implements Distribution<Long> {

   private final long mean, sd;

   public NormalLongDistribution(long mean, long sd) {
      this.mean = mean;
      this.sd = sd;
   }

   @Override
   public Long getValue(RandomSource randomSource) {
      if (sd == 0)
         return mean;
      return (long) (randomSource.nextGaussian() * sd + mean);
   }
   
   @Override
   public Long getMean() {
      return mean;
   }
}
