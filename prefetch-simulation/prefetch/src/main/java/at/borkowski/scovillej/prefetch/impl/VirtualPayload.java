package at.borkowski.scovillej.prefetch.impl;

public class VirtualPayload {
   private final int size;
   private final boolean transferPayload;

   public VirtualPayload(int size) {
      this(size, true);
   }

   public VirtualPayload(int size, boolean transferPayload) {
      this.size = size;
      this.transferPayload = transferPayload;
   }

   public int getSize() {
      return size;
   }

   public boolean getTransferPayload() {
      return transferPayload;
   }
}
