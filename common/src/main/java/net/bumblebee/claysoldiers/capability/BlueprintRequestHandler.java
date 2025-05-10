package net.bumblebee.claysoldiers.capability;

import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface BlueprintRequestHandler {

    /**
     * Returns a new Blueprint Request or {@code null} if not suitable request exist.
     *
     * @param canReach a predicate that test whether a given {@code BlockPosition} can be reached.
     * @return a new {@code BlueprintRequest}
     */
    @Nullable BlueprintRequest getRequest(Predicate<BlockPos> canReach);

    /**
     * Executes a {@code BlueprintRequest}.
     * @param request the request to execute.
     * @return whether it was a success or not
     */
    boolean doRequest(@Nullable BlueprintRequest request);
}
