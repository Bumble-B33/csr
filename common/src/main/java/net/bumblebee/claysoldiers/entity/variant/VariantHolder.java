package net.bumblebee.claysoldiers.entity.variant;

public interface VariantHolder<T> {
    void setVariant(T variant);
    T getVariant();
}
