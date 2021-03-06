package at.borkowski.prefetchsimulation.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import at.borkowski.prefetchsimulation.Request;
import at.borkowski.prefetchsimulation.algorithms.IgnoreRatePredictionAlgorithm;
import at.borkowski.prefetchsimulation.util.RandomSource;

public class ConfigurationReaderTest {

   StringBuilder sb = new StringBuilder();
   ConfigurationReader sut;
   RandomSource fixedRandom;

   @Before
   public void setUp() {
      fixedRandom = new RandomSource() {

         @Override
         public long nextLong() {
            return 314159;
         }

         @Override
         public double nextGaussian() {
            return 0.31415;
         }

         @Override
         public double nextDouble() {
            return 0.271;
         }
      };
   }

   private void line(String line) {
      sb.append(line);
      sb.append('\n');
   }

   private void buildSut() throws Exception {
      sut = new ConfigurationReader(new ByteArrayInputStream(sb.toString().getBytes("UTF8")));
      sb.setLength(0);
   }

   @Test
   public void testBasicWithComments() throws Exception {
      line("# comment");
      line("");
      line("   # comment after whitespace");
      line("\t\t\t#comment after tabs");
      line("\t \t #comment after mixed whitespace");
      line("  # comment containing # hash symbol # and another");
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      line("look-ahead 1");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(10, configuration.getTotalTicks());
      assertEquals(11, configuration.getByterate().getMean().intValue());
      assertEquals(12, configuration.getSlotLength().getMean().longValue());
      assertEquals(0.95, configuration.getNetworkUptime(), 0.00001);
      assertEquals(0.1, configuration.getRelativeJitter().getMean().doubleValue(), 0.00001);
      assertEquals(13, configuration.getAbsoluteJitter().getMean().intValue());
      assertEquals(0.5, configuration.getRelativePredictionTimeError().getMean(), 0.00001);
      assertEquals(0.8, configuration.getRelativePredictionAmplitudeError().getMean(), 0.00001);
      assertEquals(400, configuration.getAbsolutePredictionTimeError().getMean().longValue());
      assertEquals(-20, configuration.getAbsolutePredictionAmplitudeError().getMean().intValue());
      assertEquals(1, configuration.getLookAheadTime());
      assertEquals(IgnoreRatePredictionAlgorithm.class, configuration.getAlgorithm());
   }

   @Test
   public void testWhitespace() throws Exception {
      line("ticks 10");
      line("  byterate 11");
      line("\t\tslot-length 12");
      line("\t   \tnetwork-uptime     0.95");
      line("relative-jitter\t0.1");
      line("\tabsolute-jitter  \t13");
      line("relative-prediction-time-error 0.5");
      line("\t  \trelative-prediction-amplitude-error   \t 0.8");
      line("\t\tabsolute-prediction-time-error\t400");
      line("      \t absolute-prediction-amplitude-error\t\t-20");
      line("look-ahead 1");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(10, configuration.getTotalTicks());
      assertEquals(11, configuration.getByterate().getMean().intValue());
      assertEquals(12, configuration.getSlotLength().getMean().longValue());
      assertEquals(0.95, configuration.getNetworkUptime(), 0.00001);
      assertEquals(0.1, configuration.getRelativeJitter().getMean().doubleValue(), 0.00001);
      assertEquals(13, configuration.getAbsoluteJitter().getMean().intValue());
      assertEquals(0.5, configuration.getRelativePredictionTimeError().getMean(), 0.00001);
      assertEquals(0.8, configuration.getRelativePredictionAmplitudeError().getMean(), 0.00001);
      assertEquals(400, configuration.getAbsolutePredictionTimeError().getMean().longValue());
      assertEquals(-20, configuration.getAbsolutePredictionAmplitudeError().getMean().intValue());
      assertEquals(1, configuration.getLookAheadTime());
   }

   @Test
   public void testIntermittent() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("request tick 10 byterate 11 data 12");
      line("request byterate 21 tick 20 data 22");
      line("request bla blu bli data 32 tick 30 byterate 31");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(3, configuration.getIntermittentRequests().size());
      List<Request> requests = new LinkedList<>(configuration.getIntermittentRequests());
      requests.sort(new Comparator<Request>() {
         @Override
         public int compare(Request o1, Request o2) {
            return Long.compare(o1.getDeadline(), o2.getDeadline());
         }
      });

      assertEquals(10, requests.get(0).getDeadline());
      assertEquals(11, requests.get(0).getAvailableByterate());
      assertEquals(12, requests.get(0).getData());

      assertEquals(20, requests.get(1).getDeadline());
      assertEquals(21, requests.get(1).getAvailableByterate());
      assertEquals(22, requests.get(1).getData());

      assertEquals(30, requests.get(2).getDeadline());
      assertEquals(31, requests.get(2).getAvailableByterate());
      assertEquals(32, requests.get(2).getData());
   }

   @Test
   public void testSeedPresent() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("seed 31337");
      buildSut();

      Configuration configuration = sut.read();

      assertTrue(configuration.hasSeed());
      assertEquals(31337, configuration.getSeed());
   }

   @Test
   public void testSeedAbsent() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      buildSut();

      Configuration configuration = sut.read();

      assertFalse(configuration.hasSeed());
   }

   @Test
   public void testSeriesExact1() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("request-series interval 10 size 11 byterate 12 start 13 end 14");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getRecurringRequestSeries().size());
      RequestSeries series = configuration.getRecurringRequestSeries().iterator().next();

      assertEquals(10, series.getInterval().getValue(fixedRandom).longValue());
      assertEquals(11, series.getSize().getValue(fixedRandom).longValue());
      assertEquals(12, series.getByterate().getValue(fixedRandom).longValue());
      assertEquals(13, series.getStartTick().getValue(fixedRandom).longValue());
      assertEquals(14, series.getEndTick().getValue(fixedRandom).longValue());
   }

   @Test
   public void testSeriesExact2() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("request-series interval exact/10 size exact/11 byterate exact/12 start exact/13 end exact/14");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getRecurringRequestSeries().size());
      RequestSeries series = configuration.getRecurringRequestSeries().iterator().next();

      assertEquals(10, series.getInterval().getValue(fixedRandom).longValue());
      assertEquals(11, series.getSize().getValue(fixedRandom).longValue());
      assertEquals(12, series.getByterate().getValue(fixedRandom).longValue());
      assertEquals(13, series.getStartTick().getValue(fixedRandom).longValue());
      assertEquals(14, series.getEndTick().getValue(fixedRandom).longValue());
   }

   @Test
   public void testSeriesUniform() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("request-series interval uniform/10/15 size uniform/15/20 byterate uniform/20/25 start uniform/25/30 end uniform/30/35");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getRecurringRequestSeries().size());
      RequestSeries series = configuration.getRecurringRequestSeries().iterator().next();

      assertEquals(14, series.getInterval().getValue(fixedRandom).longValue());
      assertEquals(19, series.getSize().getValue(fixedRandom).longValue());
      assertEquals(24, series.getByterate().getValue(fixedRandom).longValue());
      assertEquals(29, series.getStartTick().getValue(fixedRandom).longValue());
      assertEquals(34, series.getEndTick().getValue(fixedRandom).longValue());
   }

   @Test
   public void testSeriesNormal() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error n/0/0.12");
      line("relative-prediction-amplitude-error n/-0.8/0.2");
      line("absolute-prediction-time-error u/350/450");
      line("absolute-prediction-amplitude-error u/-20/-10");
      line("look-ahead 1");

      line("request-series interval norm/10/2 size norm/15/10 byterate norm/20/18 start norm/25/10 end norm/30/5");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getRecurringRequestSeries().size());
      RequestSeries series = configuration.getRecurringRequestSeries().iterator().next();

      assertEquals(10, series.getInterval().getValue(fixedRandom).longValue());
      assertEquals(18, series.getSize().getValue(fixedRandom).longValue());
      assertEquals(25, series.getByterate().getValue(fixedRandom).longValue());
      assertEquals(28, series.getStartTick().getValue(fixedRandom).longValue());
      assertEquals(31, series.getEndTick().getValue(fixedRandom).longValue());
   }

   @Test
   public void testParametersDistributions() throws Exception {
      line("ticks 10");
      line("byterate u/10/16");
      line("slot-length u/10/12");
      line("network-uptime 0.95");
      line("relative-jitter u/0.1/0.3");
      line("absolute-jitter u/11/13");
      line("relative-prediction-time-error n/0/0.12");
      line("relative-prediction-amplitude-error n/-0.8/0.2");
      line("absolute-prediction-time-error u/350/450");
      line("absolute-prediction-amplitude-error u/-20/-10");
      line("look-ahead 1");

      line("request-series interval norm/10/2 size norm/15/10 byterate norm/20/18 start norm/25/10 end norm/30/5");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(10, configuration.getTotalTicks());
      assertEquals(13, configuration.getByterate().getMean().intValue());
      assertEquals(11, configuration.getSlotLength().getMean().longValue());
      assertEquals(0.95, configuration.getNetworkUptime(), 0.00001);
      assertEquals(0.2, configuration.getRelativeJitter().getMean().doubleValue(), 0.00001);
      assertEquals(12, configuration.getAbsoluteJitter().getMean().intValue());
      assertEquals(0, configuration.getRelativePredictionTimeError().getMean(), 0.00001);
      assertEquals(-0.8, configuration.getRelativePredictionAmplitudeError().getMean(), 0.00001);
      assertEquals(400, configuration.getAbsolutePredictionTimeError().getMean().longValue());
      assertEquals(-15, configuration.getAbsolutePredictionAmplitudeError().getMean().intValue());
      assertEquals(1, configuration.getLookAheadTime());
   }

   @Test
   public void testRequest() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("look-ahead 1");

      line("request tick 1500001 data 1000 byterate 400");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getIntermittentRequests().size());
      Request request = configuration.getIntermittentRequests().iterator().next();

      assertEquals(1500001, request.getDeadline());
      assertEquals(1000, request.getData());
      assertEquals(400, request.getAvailableByterate());
   }

   @Test
   public void testAlgorithmConfiguration() throws Exception {
      line("ticks 10");
      line("byterate 11");
      line("slot-length 12");
      line("network-uptime 0.95");
      line("relative-jitter 0.1");
      line("absolute-jitter 13");
      line("relative-prediction-time-error 0.5");
      line("relative-prediction-amplitude-error 0.8");
      line("absolute-prediction-time-error 400");
      line("absolute-prediction-amplitude-error -20");
      line("algorithm " + IgnoreRatePredictionAlgorithm.class.getName());
      line("algorithm-parameter key1 value1");
      line("algorithm-parameter key2 value2");
      line("algorithm-parameter key3 value3");
      line("look-ahead 1");
      buildSut();

      Configuration configuration = sut.read();

      Map<String, String> algorithmConfiguration = configuration.getAlgorithmConfiguration();
      assertEquals("value1", algorithmConfiguration.get("key1"));
      assertEquals("value2", algorithmConfiguration.get("key2"));
      assertEquals("value3", algorithmConfiguration.get("key3"));
   }

   @Test(expected = ConfigurationException.class)
   public void testUnknownCommand() throws Exception {
      line("exotic 10");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingCommand() throws Exception {
      line("ticks 10");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingParameter() throws Exception {
      line("request data 12");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingLongArgument() throws Exception {
      line("ticks");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingIntArgument() throws Exception {
      line("max-byterate");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingDoubleArgument() throws Exception {
      line("relative-jitter");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testBadDoubleArgument() throws Exception {
      line("relative-jitter BAD");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingClass() throws Exception {
      line("algorithm");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testBadClass() throws Exception {
      line("algorithm BAD");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingLongDistributionArgument() throws Exception {
      line("request-series interval uniform/10/15 size uniform/15/20 byterate uniform/20/25 start uniform/25/30 end uniform/30");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingIntDistributionArgument() throws Exception {
      line("request-series interval uniform/10/15 size uniform/15 byterate uniform/20/25 start uniform/25/30 end uniform/30/35");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testBadLongDistributionArgument() throws Exception {
      line("request-series interval uniform/10/15 size uniform/15/20 byterate uniform/20/25 start uniform/25/30 end uniform/30/BAD");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testBadIntDistributionArgument() throws Exception {
      line("request-series interval uniform/10/15 size uniform/15/BAD byterate uniform/20/25 start uniform/25/30 end uniform/30/35");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testBadDistribution() throws Exception {
      line("request-series interval BAD/10/15 size uniform/15/20 byterate uniform/20/25 start uniform/25/30 end uniform/30/35");
      buildSut();
      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testMissingDistributionArgument() throws Exception {
      line("request-series interval exact size uniform/15/20 byterate uniform/20/25 start uniform/25/30 end uniform/30/35");
      buildSut();
      sut.read();
   }
}
