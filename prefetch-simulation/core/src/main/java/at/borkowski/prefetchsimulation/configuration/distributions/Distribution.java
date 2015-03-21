package at.borkowski.prefetchsimulation.configuration.distributions;

import at.borkowski.prefetchsimulation.util.RandomSource;

public interface Distribution<T extends Number> {

   T getValue(RandomSource randomSource);
}
