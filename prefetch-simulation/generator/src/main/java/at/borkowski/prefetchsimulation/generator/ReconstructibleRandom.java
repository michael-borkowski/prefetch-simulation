/*
 * This is an adapted version of the java.util.Random implementation by Oracle.
 * 
 * Original Copyright: Copyright (c) 1995, 2013, Oracle and/or its affiliates.
 * All rights reserved. ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to
 * license terms.
 */

package at.borkowski.prefetchsimulation.generator;

public final class ReconstructibleRandom {

   private long seed;

   private static final long multiplier = 0x5DEECE66DL;
   private static final long addend = 0xBL;
   private static final long mask = (1L << 48) - 1;

   private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)

   // IllegalArgumentException messages
   static final String BadBound = "bound must be positive";
   static final String BadRange = "bound must be greater than origin";
   static final String BadSize = "size must be non-negative";

   public ReconstructibleRandom(long seed) {
      this.seed = initialScramble(seed);
   }

   private static long initialScramble(long seed) {
      return (seed ^ multiplier) & mask;
   }

   public void setSeed(long seed) {
      this.seed = initialScramble(seed);
   }

   private int next(int bits) {
      seed = (seed * multiplier + addend) & mask;
      return (int) (seed >>> (48 - bits));
   }

   public int nextInt(int bound) {
      if (bound <= 0)
         throw new IllegalArgumentException(BadBound);

      int r = next(31);
      int m = bound - 1;
      if ((bound & m) == 0)  // i.e., bound is a power of 2
         r = (int) ((bound * (long) r) >> 31);
      else {
         for (int u = r; u - (r = u % bound) + m < 0; u = next(31))
            ;
      }
      return r;
   }

   public long nextLong() {
      // it's okay that the bottom word remains signed.
      return ((long) (next(32)) << 32) + next(32);
   }

   public double nextDouble() {
      return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT;
   }

   public ReconstructibleRandom fork() {
      return new ReconstructibleRandom(nextLong());
   }
}
