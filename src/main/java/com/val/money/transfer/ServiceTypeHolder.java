package com.val.money.transfer;

/**
 * Will be extended by services, which {@link java.util.ServiceLoader} needs to load.
 */
public interface ServiceTypeHolder {
    String getType();
}
