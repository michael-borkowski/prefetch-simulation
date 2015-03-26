package at.borkowski.prefetchsimulation.util;

public interface RandomSource {

   long nextLong();
   
   double nextGaussian();

   double nextDouble();

}
