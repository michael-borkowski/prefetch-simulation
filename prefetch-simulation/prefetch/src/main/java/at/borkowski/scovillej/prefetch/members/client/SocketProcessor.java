package at.borkowski.scovillej.prefetch.members.client;

import java.io.IOException;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.services.comm.SimulationSocket;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

/**
 * Represents the socket sub-processor of {@link FetchClient}. It is responsible
 * for communicating with the server member using a socket.
 */
public class SocketProcessor {
   private final String socketName;

   private CommunicationService comm;
   private SimulationSocket<VirtualPayload> socket;

   private boolean initialized = false;

   public SocketProcessor(String socketName) {
      this.socketName = socketName;
   }

   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      comm = context.getService(CommunicationService.class);
   }

   public void executePhase(SimulationContext context) throws IOException {
      if (!initialized)
         initialize(context);
   }

   private void initialize(SimulationContext context) throws IOException {
      socket = comm.beginConnect(socketName, VirtualPayload.class);
      initialized = true;
   }

   public VirtualPayload readIfPossible() throws IOException {
      if (socket.available() != 0)
         return (VirtualPayload) socket.read();
      return null;
   }

   public void request(Request request) throws IOException {
      socket.write(new VirtualPayload(request.getData(), false));
   }

   public boolean isReady() {
      return socket != null && socket.established();
   }
}
