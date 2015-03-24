package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public class ExactDistribution<T extends Number> implements Distribution<T> {

   private final T value;

   public ExactDistribution(T value) {
      this.value = value;
   }

   @Override
   public T getValue(RandomSource randomSource) {
      return value;
   }
   
   @Override
   public T getMean() {
      return value;
   }

}
