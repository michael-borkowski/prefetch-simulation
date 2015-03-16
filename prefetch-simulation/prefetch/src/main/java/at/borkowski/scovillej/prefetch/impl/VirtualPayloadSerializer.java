package at.borkowski.scovillej.prefetch.impl;

import java.nio.ByteBuffer;

import at.borkowski.scovillej.services.comm.Serializer;

public class VirtualPayloadSerializer implements Serializer<VirtualPayload> {
   @Override
   public VirtualPayload deserialize(byte[] bytes) {
      if (bytes.length < 4)
         throw new IllegalArgumentException("frame must be at least four bytes long");
      ByteBuffer wrapper = ByteBuffer.wrap(bytes);
      return new VirtualPayload(wrapper.getInt(0));
   }

   public java.lang.Class<VirtualPayload> getSerializedClass() {
      return VirtualPayload.class;
   }

   @Override
   public byte[] serialize(VirtualPayload object) {
      byte[] ret = new byte[4 + Math.max(0, object.getTransferPayload() ? object.getSize() - 4 : 0)];
      ByteBuffer wrapper = ByteBuffer.allocate(4);
      wrapper.putInt(0, object.getSize());
      wrapper.get(ret, 0, 4);
      return ret;
   }
}
