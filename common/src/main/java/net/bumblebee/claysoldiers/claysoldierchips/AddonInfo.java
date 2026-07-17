package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AddonInfo {
    AddonInfo EMPTY = new AddonInfo() {
        @Override
        public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
            return false;
        }

        @Override
        public int getAllowedAddonsCount() {
            return 0;
        }
    };
    AddonInfo NO_BREAK_AND_RANGE = allowed(Set.of(ClaySoldierChipAddons.NO_BREAK_ADDON, ClaySoldierChipAddons.RANGE_ADDON), 2);

    boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon);

    int getAllowedAddonsCount();

    static AddonInfo allowed(ClaySoldierChipAddon allowed) {
        return new AddonInfo() {
            @Override
            public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
                if (presentAddons.contains(addon)) {
                    return false;
                }
                return allowed == addon;
            }

            @Override
            public int getAllowedAddonsCount() {
                return 1;
            }
        };
    }

    static AddonInfo allowed(Set<ClaySoldierChipAddon> addons, int max) {
        return new AddonInfo() {
            @Override
            public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
                if (presentAddons.contains(addon)) {
                    return false;
                }

                return addons.contains(addon);
            }

            @Override
            public int getAllowedAddonsCount() {
                return max;
            }
        };
    }
}
