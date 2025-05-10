package net.bumblebee.claysoldiers.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

public class BlueprintRequest {
    private final Item item;
    private final BlockPos pos;
    private final long start;
    private Status status = Status.STARTED;

    public BlueprintRequest(Item item, BlockPos pos, long start) {
        this.item = item;
        this.pos = pos;
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    public Item getItem() {
        return item;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean hasStarted() {
        return status == Status.STARTED;
    }
    public boolean isPlacing() {
        return status == Status.PLACING;
    }

    public void setPlacing() {
        status = Status.PLACING;
    }
    public void setFinished() {
        status = Status.FINISHED;
    }
    public boolean isFinished() {
        return status == Status.FINISHED;
    }
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
    public void cancel() {
        status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        return "BlueprintRequest{%s at: %s|%s}".formatted(item, pos, status);
    }

    private enum Status {
        /**
         * Indicates this Request has started but the item has not yet been retrieved.
         */
        STARTED,
        /**
         * The Item has been retrieved and is ready to be placed.
         */
        PLACING,
        /**
         * The item has been placed down.
         */
        FINISHED,
        /**
         * The Request got cancelled, the Item has not been placed down. The request should not be attempted to fulfill
         */
        CANCELLED;
    }
}
