package net.bumblebee.claysoldiers.integration.curios;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.concurrent.CompletableFuture;

public class ModCuriosProvider extends CuriosDataProvider {
    public ModCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(ClaySoldiersCommon.MOD_ID, output, fileHelper, registries);
    }

    public static void init() {
        ClaySoldiersCommon.IS_WEARING_GOGGLES = ClaySoldiersCommon.IS_WEARING_GOGGLES.or(p -> {
           var cap = p.getCapability(CuriosCapability.INVENTORY);
           if (cap == null) {
               return false;
           }

            for (ICurioStacksHandler stacksHandler : cap.getCurios().values()) {
                int slots = stacksHandler.getSlots();
                for (int slot = 0; slot < slots; slot++) {
                    if (stacksHandler.getStacks().getStackInSlot(slot).is(ModItems.CLAY_GOGGLES.get())) {
                        return true;
                    }
                }
            }

            return false;
        });
    }

    @Override
    public void generate(HolderLookup.Provider provider, ExistingFileHelper existingFileHelper) {
        createEntities("players")
                .addPlayer()
                .addSlots("head");
    }
}
