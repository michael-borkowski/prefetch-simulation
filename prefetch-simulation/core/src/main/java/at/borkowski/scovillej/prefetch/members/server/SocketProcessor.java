package at.borkowski.scovillej.prefetch.members.server;

import java.io.IOException;

import at.borkowski.scovillej.prefetch.impl.VirtualPayload;
import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.services.comm.SimulationServerSocket;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

/**
 * Represents the socket sub-processor of {@link FetchServer}. It is responsible
 * for communicating with the client member using a socket.
 */
public class SocketProcessor {
   private final FetchServer owner;
   private final String socketName;

   private CommunicationService comm;
   private SimulationServerSocket<VirtualPayload> serverSocket;

   private boolean initialized = false;

   /**
    * Creates a socket sub-processor
    * 
    * @param owner
    *           the owning {@link FetchServer}
    * @param socketName
    *           the socket name to listen on
    */
   public SocketProcessor(FetchServer owner, String socketName) {
      this.owner = owner;
      this.socketName = socketName;
   }

   /**
    * Initialization method called upon initialization of the owning
    * {@link FetchServer}
    * 
    * @param simulation
    *           the containing simulation
    * @param context
    *           the initailization context
    */
   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      comm = context.getService(CommunicationService.class);
   }

   /**
    * Handles a phase, ie. performs business logic
    * 
    * @param context
    *           the simulation context
    * @throws IOException
    *            if an I/O exception occurs
    */
   public void executePhase(SimulationContext context) throws IOException {
      if (!initialized)
         initialize(context);

      if (serverSocket.available() > 0)
         owner.registerClientProcessor(new ClientProcessor(owner, serverSocket.accept()));
   }

   private void initialize(SimulationContext context) throws IOException {
      serverSocket = comm.createServerSocket(socketName, VirtualPayload.class);
      initialized = true;
   }
}
