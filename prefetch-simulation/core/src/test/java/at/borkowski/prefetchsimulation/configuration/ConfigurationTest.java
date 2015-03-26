package at.borkowski.prefetchsimulation.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.algorithms.PrefetchAlgorithm;
import at.borkowski.prefetchsimulation.configuration.distributions.Distribution;
import at.borkowski.prefetchsimulation.configuration.distributions.Distributions;

public class ConfigurationTest {

   @Test
   public void test() {
      long totalTicks = 1234;
      Distribution<Integer> byterate = Distributions.exact(4334);
      Distribution<Long> slotLength = Distributions.exact(1010L);
      double networkUptime = 3.1415;
      Distribution<Double> relativeJitter = Distributions.exact(2.71);
      Distribution<Integer> absoluteJitter = Distributions.exact(1337);
      Distribution<Double> relativePredictionTimeError = Distributions.exact(733.2);
      Distribution<Double> relativePredictionAmplitudeError = Distributions.exact(733.1);
      Distribution<Long> absolutePredictionTimeError = Distributions.exact(733L);
      Distribution<Integer> absolutePredictionAmplitudeError = Distributions.exact(733);
      Collection<RequestSeries> recurringRequestSeries = new LinkedList<>();
      Collection<Request> intermittentRequests = new LinkedList<>();
      Class<? extends PrefetchAlgorithm> algorithm = IgnoreRatePredictionAlgorithm.class;
      Map<String, String> algorithmConfiguration = new HashMap<>();
      long lookAheadTime = 271;

      Configuration sut = new Configuration(totalTicks, byterate, slotLength, networkUptime, relativeJitter, absoluteJitter, relativePredictionTimeError, relativePredictionAmplitudeError, absolutePredictionTimeError, absolutePredictionAmplitudeError, recurringRequestSeries, intermittentRequests, algorithm, algorithmConfiguration, lookAheadTime);

      assertEquals(1234, sut.getTotalTicks());
      assertEquals(4334, sut.getByterate().getMean().intValue());
      assertEquals(1010, sut.getSlotLength().getMean().longValue());
      assertEquals(3.1415, sut.getNetworkUptime(), 0.0000001);
      assertEquals(2.71, sut.getRelativeJitter().getMean().doubleValue(), 0.0000001);
      assertEquals(1337, sut.getAbsoluteJitter().getMean().intValue());
      assertEquals(733.2, sut.getRelativePredictionTimeError().getMean(), 0.0000001);
      assertEquals(733.1, sut.getRelativePredictionAmplitudeError().getMean(), 0.0000001);
      assertEquals(733L, sut.getAbsolutePredictionTimeError().getMean().longValue());
      assertEquals(733, sut.getAbsolutePredictionAmplitudeError().getMean().intValue());
      assertSame(recurringRequestSeries, sut.getRecurringRequestSeries());
      assertSame(intermittentRequests, sut.getIntermittentRequests());
      assertEquals(IgnoreRatePredictionAlgorithm.class, sut.getAlgorithm());
      assertSame(algorithmConfiguration, sut.getAlgorithmConfiguration());
      assertEquals(271, sut.getLookAheadTime());

      assertFalse(sut.hasSeed());

      sut.setSeed(42);

      assertEquals(42, sut.getSeed());
      assertTrue(sut.hasSeed());

      sut.clearSeed();

      assertFalse(sut.hasSeed());
   }

}
