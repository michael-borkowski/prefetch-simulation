package at.borkowski.prefetchsimulation.genesis;

public class GenesisException extends Exception {

   private static final long serialVersionUID = 1203768526792946L;
   
   public GenesisException(String message, Throwable cause) {
      super(message, cause);
   }

   public GenesisException(String message) {
      super(message);
   }

}
