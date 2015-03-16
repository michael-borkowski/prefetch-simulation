package at.borkowski.scovillej.prefetch.configuration;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import at.borkowski.scovillej.prefetch.configuration.model.Configuration;

public class ConfigurationReaderTest {

   StringBuilder sb = new StringBuilder();
   ConfigurationReader sut;

   private void line(String line) {
      sb.append(line);
      sb.append('\n');
   }

   private void buildSut() throws Exception {
      sut = new ConfigurationReader(new ByteArrayInputStream(sb.toString().getBytes("UTF8")));
      sb.setLength(0);
   }

   @Test
   public void testComments() throws Exception {
      line("# comment");
      line("");
      line("   # comment after whitespace");
      line("\t\t\t#comment after tabs");
      line("\t \t #comment after mixed whitespace");
      line("  # comment containing # hash symbol # and another");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(1, configuration.getTicks());
      assertEquals(0, configuration.getRatePredicted().size());
      assertEquals(0, configuration.getRateReal().size());
      assertEquals(0, configuration.getRequests().size());
   }

   @Test
   public void testRequests() throws Exception {
      line("# comment");
      line("100 request 40         50");
      line("120          request        50 50# end-line comment with # another hash sign");
      line("210 request    100 \t\t        03 # end-line comment");
      line("250                request\t1\t100# end-line comment immediately after data");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(251, configuration.getTicks());
      assertEquals(0, configuration.getRatePredicted().size());
      assertEquals(0, configuration.getRateReal().size());
      assertEquals(4, configuration.getRequests().size());

      assertEquals(100, configuration.getRequests().get(0).getDeadline());
      assertEquals(40, configuration.getRequests().get(0).getData());
      assertEquals(50, configuration.getRequests().get(0).getAvailableByterate());

      assertEquals(120, configuration.getRequests().get(1).getDeadline());
      assertEquals(50, configuration.getRequests().get(1).getData());
      assertEquals(50, configuration.getRequests().get(1).getAvailableByterate());

      assertEquals(210, configuration.getRequests().get(2).getDeadline());
      assertEquals(100, configuration.getRequests().get(2).getData());
      assertEquals(3, configuration.getRequests().get(2).getAvailableByterate());

      assertEquals(250, configuration.getRequests().get(3).getDeadline());
      assertEquals(1, configuration.getRequests().get(3).getData());
      assertEquals(100, configuration.getRequests().get(3).getAvailableByterate());
   }

   @Test
   public void testRates() throws Exception {
      line("# comment");
      line("100 rate-real 4");
      line("120 rate-prediction 8");
      line("210 rate-prediction\t5");
      line("250 rate-real       3");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(251, configuration.getTicks());
      assertEquals(2, configuration.getRatePredicted().size());
      assertEquals(2, configuration.getRateReal().size());
      assertEquals(0, configuration.getRequests().size());

      assertEquals(4, configuration.getRateReal().get(100L).intValue());
      assertEquals(8, configuration.getRatePredicted().get(120L).intValue());
      assertEquals(5, configuration.getRatePredicted().get(210L).intValue());
      assertEquals(3, configuration.getRateReal().get(250L).intValue());
   }

   @Test
   public void testEnd() throws Exception {
      line("# comment");
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(301, configuration.getTicks());
      assertEquals(0, configuration.getRatePredicted().size());
      assertEquals(0, configuration.getRateReal().size());
      assertEquals(2, configuration.getRequests().size());
   }

   @Test
   public void testAlgorithm() throws Exception {
      line("# comment");
      line("0 algorithm " + ConfigurationReaderTest_Algorithm.class.getName());
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      Configuration configuration = sut.read();

      assertEquals(301, configuration.getTicks());
      assertEquals(0, configuration.getRatePredicted().size());
      assertEquals(0, configuration.getRateReal().size());
      assertEquals(2, configuration.getRequests().size());

      assertEquals(ConfigurationReaderTest_Algorithm.class, configuration.getAlgorithm().getClass());
   }

   @Test(expected = ConfigurationException.class)
   public void testNonMonotonic() throws Exception {
      line("# comment");
      line("100 request 40 50");
      line("110 request 40 50");
      line("100 request 40 50");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNonNumberTick() throws Exception {
      line("# comment");
      line("100x request 40 50");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNonNumberParameter1() throws Exception {
      line("# comment");
      line("100 request 40x 50");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNonNumberParameter2() throws Exception {
      line("# comment");
      line("100 request 40 50x");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNonNumberParameter3() throws Exception {
      line("# comment");
      line("100 rate-real 40x");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNonNumberParameter4() throws Exception {
      line("# comment");
      line("100 rate-prediction 40x");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testUnknownCommand() throws Exception {
      line("# comment");
      line("100 request 40 50");
      line("100 banana");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testWrongCommandSyntax1() throws Exception {
      line("# comment");
      line("100 request 40 50 banana");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testWrongCommandSyntax2() throws Exception {
      line("# comment");
      line("100 rate-real 10 banana");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testWrongCommandSyntax3() throws Exception {
      line("# comment");
      line("100 end banana");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAfterEnd1() throws Exception {
      line("# comment");
      line("100 request 40 50");
      line("110 end");
      line("120 request 10 10");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAfterEnd2() throws Exception {
      line("# comment");
      line("100 request 40 50");
      line("110 end");
      line("109 request 10 10");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testNegativeTick() throws Exception {
      line("# comment");
      line("100 request 40 50");
      line("-110 end");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAlgorithmUnknown() throws Exception {
      line("# comment");
      line("0 algorithm NOT_" + ConfigurationReaderTest_Algorithm.class.getName());
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAlgorithmNotzero() throws Exception {
      line("# comment");
      line("1 algorithm NOT_" + ConfigurationReaderTest_Algorithm.class.getName());
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAlgorithmParameters() throws Exception {
      line("# comment");
      line("0 algorithm a b");
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      sut.read();
   }

   @Test(expected = ConfigurationException.class)
   public void testAlgorithmException() throws Exception {
      line("# comment");
      line("0 algorithm " + ConfigurationReaderTest_BadAlgorithm1.class.getName());
      line("100 request 40 5");
      line("110 request 40 50");
      line("300 end");
      buildSut();

      sut.read();
   }

}
