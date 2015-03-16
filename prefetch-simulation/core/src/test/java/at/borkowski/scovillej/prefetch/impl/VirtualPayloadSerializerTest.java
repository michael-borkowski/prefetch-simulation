package at.borkowski.scovillej.prefetch.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VirtualPayloadSerializerTest {

   VirtualPayloadSerializer sut = new VirtualPayloadSerializer();

   @Test
   public void testSerializationWithPayload1() {
      byte[] bytes = new byte[100];
      bytes[3] = 100;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(100, true)));
   }

   @Test
   public void testSerializationWithPayload2() {
      byte[] bytes = new byte[100];
      bytes[3] = 100;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(100)));
   }

   @Test
   public void testSerializationWithZeroPayload() {
      byte[] bytes = new byte[4];
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(0, true)));
   }

   @Test
   public void testSerializationWithOnePayload() {
      byte[] bytes = new byte[4];
      bytes[3] = 1;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(1, true)));
   }

   @Test
   public void testSerializationWithTwoPayload() {
      byte[] bytes = new byte[4];
      bytes[3] = 2;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(2, true)));
   }

   @Test
   public void testSerializationWithThreePayload() {
      byte[] bytes = new byte[4];
      bytes[3] = 3;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(3, true)));
   }

   @Test
   public void testSerializationWithFourPayload() {
      byte[] bytes = new byte[4];
      bytes[3] = 4;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(4, true)));
   }

   @Test
   public void testSerializationWithFivePayload() {
      byte[] bytes = new byte[5];
      bytes[3] = 5;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(5, true)));
   }

   @Test
   public void testSerializationWithoutPayload1() {
      byte[] bytes = new byte[4];
      bytes[3] = 100;
      assertArrayEquals(bytes, sut.serialize(new VirtualPayload(100, false)));
   }

   @Test
   public void testDeserialization() {
      byte[] bytes = new byte[4];
      bytes[3] = 100;
      assertEquals(100, sut.deserialize(bytes).getSize());
      assertTrue(sut.deserialize(bytes).getTransferPayload());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testDeserializationInvalid() {
      byte[] bytes = new byte[3];
      bytes[2] = 100;
      sut.deserialize(bytes);
   }

}
