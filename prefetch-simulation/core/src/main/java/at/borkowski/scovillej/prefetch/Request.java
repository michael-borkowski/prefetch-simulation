package at.borkowski.scovillej.prefetch;

/**
 * This class represent a request which is known to be made by client code.
 * 
 * A request consists of a deadline, a predicted amount of data and a predicted
 * byterate with which the data source will provide the data (this is not the
 * link bandwidth, which might be significantly smaller).
 */
public class Request {
   private final long deadline;
   private final int data;
   private final int availableByterate;

   /**
    * Creates a request object
    * 
    * @param deadline
    *           the deadline
    * @param data
    *           the amount of data
    * @param availableByterate
    *           the provided byte rate
    */
   public Request(long deadline, int data, int availableByterate) {
      this.deadline = deadline;
      this.availableByterate = availableByterate;
      this.data = data;
   }

   /**
    * Returns the available byte rate
    * 
    * @return the available byte rate
    */
   public int getAvailableByterate() {
      return availableByterate;
   }

   /**
    * Returns the predicted data size
    * 
    * @return the predicted data size
    */
   public int getData() {
      return data;
   }

   /**
    * Returns the deadline until which the request must be satisfied
    * 
    * @return the deadline
    */
   public long getDeadline() {
      return deadline;
   }
}
