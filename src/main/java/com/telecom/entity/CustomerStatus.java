package com.telecom.entity;

public enum CustomerStatus {
    ACTIVE, SUSPENDED, TERMINATED;

    /** ACTIVE <-> SUSPENDED, anything -> TERMINATED, TERMINATED is final. */
    public boolean canChangeTo(CustomerStatus target) {
        if (this == TERMINATED) {
            return false;
        }
        return target == TERMINATED || target != this;
    }
}
