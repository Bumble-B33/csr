package net.bumblebee.claysoldiers.entity.common.variant;

public interface VariantHolder<T> {
    void setVariant(T variant);
    T getVariant();
}
