package at.borkowski.scovillej.prefetch.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.borkowski.scovillej.prefetch.Request;
import at.borkowski.scovillej.prefetch.members.aux.RatePredictionService;

/**
 * This algorithm schedules all requests according to their parameters, assuming
 * an infinite link bandwidth.
 */
public class RespectRatePredictionAlgorithm implements PrefetchAlgorithm {
   public final static long CONNECTION_OVERHEAD = 5;
   public final static double ALPHA = 1;

   @Override
   public Map<Request, Long> schedule(Collection<Request> requests, RatePredictionService ratePredictionService) {
      HashMap<Request, Long> ret = new HashMap<>();

      List<Request> sortedByDeadline = new LinkedList<Request>(requests);
      Collections.sort(sortedByDeadline, new Comparator<Request>() {
         @Override
         public int compare(Request o1, Request o2) {
            return o1.getDeadline() > o2.getDeadline() ? -1 : 1;
         }
      });

      long previousStart = Long.MAX_VALUE;

      for (Request req : sortedByDeadline) {
         long start = getStart(previousStart, req, ratePredictionService);
         ret.put(req, start);
         
         previousStart = start;
      }

      return ret;
   }

   private long getStart(long busyUntil, Request req, RatePredictionService ratePredictionService) {
      long data = req.getData();
      long tick = Math.min(busyUntil, req.getDeadline()) - CONNECTION_OVERHEAD - 1;

      while (data > 0 && tick >= 0) {
         Integer prediction = ratePredictionService.predict(tick);
         if(prediction == null) prediction = Integer.MAX_VALUE;
         data -= Math.min(req.getAvailableByterate(), prediction);
         tick--;
      }

      return tick;
   }
}
