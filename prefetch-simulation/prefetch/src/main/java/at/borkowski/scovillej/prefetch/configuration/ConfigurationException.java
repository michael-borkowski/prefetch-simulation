package at.borkowski.scovillej.prefetch.configuration;

public class ConfigurationException extends Exception {

   private static final long serialVersionUID = 1203768526792946L;
   
   public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }

   public ConfigurationException(String message) {
      super(message);
   }

}