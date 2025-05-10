package net.bumblebee.claysoldiers.datamap;

public interface SoldierSlotCallback {
    SoldierSlotCallback NULL = new SoldierSlotCallback() {
        @Override
        public void slot(SoldierEquipmentSlot slot) {
        }

        @Override
        public void capability() {
        }

        @Override
        public void carried() {
        }
    };

    void slot(SoldierEquipmentSlot slot);

    void capability();

    void carried();
}
