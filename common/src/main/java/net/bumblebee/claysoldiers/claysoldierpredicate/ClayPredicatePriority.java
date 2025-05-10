package net.bumblebee.claysoldiers.claysoldierpredicate;

/**
 * The priority of which clay predicate functions are tested.
 */
public enum ClayPredicatePriority implements Comparable<ClayPredicatePriority> {
    /**
     * A Priority of Multi means that the predicate
     * combined the result of multiple other predicate in any way
     */
    MULTI,
    /**
     * A High Priority means that the predicate result does not rely
     * on factors affected by other soldier properties
     */
    HIGH,
    /**
     * A Low Priority means that the predicate result relies on
     * factors affected by other soldier properties
     */
    LOW;
}
