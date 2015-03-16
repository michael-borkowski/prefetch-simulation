package at.borkowski.scovillej.prefetch.members.aux;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.scovillej.services.comm.CommunicationService;
import at.borkowski.scovillej.simulation.PhaseHandler;
import at.borkowski.scovillej.simulation.ServiceProvider;
import at.borkowski.scovillej.simulation.Simulation;
import at.borkowski.scovillej.simulation.SimulationContext;
import at.borkowski.scovillej.simulation.SimulationEvent;
import at.borkowski.scovillej.simulation.SimulationInitializationContext;

public class RateSetter implements ServiceProvider<RateControlService>, RateControlService {

   private CommunicationService communicationService;
   private final String socketName;

   private List<SimulationEvent> events = new LinkedList<>();
   private Integer requestSpecific;
   private Integer global;

   public RateSetter(String phase, String socketName, Map<Long, Integer> limits) {
      this.socketName = socketName;

      for (Long tick : limits.keySet()) {
         Integer limit = limits.get(tick);
         events.add(new SimulationEvent() {

            @Override
            public Collection<String> getPhaseSubcription() {
               return Arrays.asList(phase);
            }

            @Override
            public void executePhase(SimulationContext context) {
               global = limit;
               refreshRates();
            }

            @Override
            public long getScheduledTick() {
               return tick;
            }
         });
      }
   }

   @Override
   public void initialize(Simulation simulation, SimulationInitializationContext context) {
      this.communicationService = context.getService(CommunicationService.class);
   }

   @Override
   public Collection<SimulationEvent> generateEvents() {
      return events;
   }

   @Override
   public Collection<PhaseHandler> getPhaseHandlers() {
      return null;
   }

   @Override
   public void setRequestSpecificRate(Integer requestSpecific) {
      this.requestSpecific = requestSpecific;
      refreshRates();
   }

   private void refreshRates() {
      Integer uplink, downlink;
      downlink = uplink = global;
      
      if(requestSpecific != null && (global == null || global > requestSpecific)) downlink = requestSpecific;
      
      communicationService.setRates(socketName, uplink, downlink);
   }

   @Override
   public Class<RateControlService> getServiceClass() {
      return RateControlService.class;
   }

   @Override
   public RateControlService getService() {
      return this;
   }

}
