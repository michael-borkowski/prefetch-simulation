package at.borkowski.prefetchsimulation.configuration.distributions;

public class Distributions {
   public <T extends Number> Distribution<T> exactly(T value) {
      return new ExactDistribution<T>(value);
   }
   
   public Distribution<Integer> uniform(int min, int max) {
      return new UniformIntegerDistribution(min, max);
   }
   
   public Distribution<Long> uniform(long min, long max) {
      return new UniformLongDistribution(min, max);
   }
}
