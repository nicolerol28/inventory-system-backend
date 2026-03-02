package com.miapp.inventory_system.inventory.domain.model;

public enum MovementType {
    PURCHASE_ENTRY,
    SALE_EXIT,
    RETURN_ENTRY,
    DAMAGE_EXIT,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT;

    public boolean isInbound() {
        return this == PURCHASE_ENTRY
                || this == RETURN_ENTRY
                || this == ADJUSTMENT_IN;
    }
}