package at.borkowski.prefetchsimulation.regression;

import at.borkowski.prefetchsimulation.configuration.Configuration;

public interface RegressionContext {

   Configuration getBaseConfiguration();

   void executeRun(String independentVariableLabel, Configuration configuration);

}
