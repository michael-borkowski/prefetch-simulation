package at.borkowski.scovillej.prefetch.members.server;

import java.io.IOException;

import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.services.comm.SimulationSocket;
import at.borkowski.scovillej.simulation.SimulationContext;

/**
 * Represents a client sub-processor of {@link FetchServer}. It is responsible
 * for handling communication with one clinet over a {@link SimulationSocket}.
 */
public class ClientProcessor {
   private final FetchServer owner;
   private final SimulationSocket<VirtualPayload> socket;

   /**
    * Creates a new client processor
    * 
    * @param owner
    *           the owning {@link FetchServer}
    * @param socket
    *           the socket to communicate over
    */
   public ClientProcessor(FetchServer owner, SimulationSocket<VirtualPayload> socket) {
      this.owner = owner;
      this.socket = socket;
   }

   /**
    * Handles a phase, ie. performs business logic
    * 
    * @param context
    *           the simulation context
    * @throws IOException
    *            if an I/O exception occurs
    */
   public void handle(SimulationContext context) throws IOException {
      if (socket.available() == 0)
         return;

      VirtualPayload request = socket.read();

      if (request == null)
         close();
      else
         handle(request, context);
   }

   private void handle(VirtualPayload request, SimulationContext context) throws IOException {
      socket.write(new VirtualPayload(request.getSize()));
   }

   private void close() {
      socket.close();
      owner.deregisterClientProcessor(this);
   }

}
