package at.borkowski.prefetchsimulation.configuration.distributions;

public class Distributions {
   public static <T extends Number> Distribution<T> exactly(T value) {
      return new ExactDistribution<T>(value);
   }
   
   public static Distribution<Integer> uniform(int min, int max) {
      return new UniformIntegerDistribution(min, max);
   }
   
   public static Distribution<Long> uniform(long min, long max) {
      return new UniformLongDistribution(min, max);
   }

   public static Distribution<Long> uniformLong(long min, long max) {
      return uniform(min, max);
   }

   public static Distribution<Integer> uniformInteger(int min, int max) {
      return uniform(min, max);
   }

   public static Distribution<Long> normal(long mean, long sd) {
      return new NormalLongDistribution(mean, sd);
   }

   public static Distribution<Integer> normal(int mean, int sd) {
      return new NormalIntegerDistribution(mean, sd);
   }
}
