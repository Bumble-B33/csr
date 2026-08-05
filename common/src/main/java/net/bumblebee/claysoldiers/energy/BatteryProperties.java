package net.bumblebee.claysoldiers.energy;

public record BatteryProperties(int capacity, int maxExtract, int maxInsert) {
    private static final int BASE_CAPACITY = 3000;

    public static Builder of(int multiplier) {
        return new Builder(multiplier * BASE_CAPACITY);
    }

    public static class Builder {
        private final int capacity;
        private int maxExtract;
        private int maxInsert;

        public Builder(int capacity) {
            this.capacity = capacity;
            this.maxExtract = 0;
            this.maxInsert = 0;
        }

        public Builder allowExtraction() {
            this.maxExtract = capacity;
            return this;
        }

        public Builder allowInsertion() {
            this.maxInsert = capacity;
            return this;
        }

        public BatteryProperties build() {
            return new BatteryProperties(capacity, maxExtract, maxInsert);
        }
    }
}
