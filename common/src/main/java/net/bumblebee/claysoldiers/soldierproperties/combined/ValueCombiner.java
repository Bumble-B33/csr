package net.bumblebee.claysoldiers.soldierproperties.combined;

@FunctionalInterface
public interface ValueCombiner<T> {
    /**
     * Combines the first value with the second value.
     * May return a new instance.
     * @param first the first value
     * @param second the second value
     * @return the combined value
     *
     * @throws IllegalArgumentException when combining two non-combinable values
     */
    T combine(T first, T second);
}
