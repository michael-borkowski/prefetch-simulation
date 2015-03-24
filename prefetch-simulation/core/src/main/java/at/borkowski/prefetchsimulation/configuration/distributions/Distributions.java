package at.borkowski.prefetchsimulation.configuration.distributions;

public class Distributions {
   public static <T extends Number> Distribution<T> exact(T value) {
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

   @SuppressWarnings("unchecked")
   public static <T extends Number> Distribution<T> uniform(T min, T max, Class<T> clazz) {
      if (clazz.equals(Integer.class))
         return (Distribution<T>) uniform((Integer) min, (Integer) max);
      if (clazz.equals(Long.class))
         return (Distribution<T>) uniform((Long) min, (Long) max);
      else
         throw new RuntimeException("unknown parse class " + clazz);
   }

   @SuppressWarnings("unchecked")
   public static <T extends Number> Distribution<T> normal(T min, T max, Class<T> clazz) {
      if (clazz.equals(Integer.class))
         return (Distribution<T>) normal((Integer) min, (Integer) max);
      if (clazz.equals(Long.class))
         return (Distribution<T>) normal((Long) min, (Long) max);
      else
         throw new RuntimeException("unknown parse class " + clazz);
   }
}
