package at.borkowski.prefetchsimulation.regression;

import java.util.LinkedList;
import java.util.List;

public class Quantity {
   private final List<Double> values = new LinkedList<>();

   public void add(double value) {
      values.add(value);
   }

   public double getMean() {
      double sum = 0;
      for (double value : values)
         sum += value;
      return sum / values.size();
   }

   public double getStandardDeviation() {
      double mean = getMean();
      double sum = 0;

      for (double value : values)
         sum += Math.pow(value - mean, 2);

      return Math.sqrt(sum / values.size());
   }
   
   public double getLowerSD() {
      return getMean() - getStandardDeviation();
   }
   
   public double getUpperSD() {
      return getMean() + getStandardDeviation();
   }

   public int getCount() {
      return values.size();
   }
}
