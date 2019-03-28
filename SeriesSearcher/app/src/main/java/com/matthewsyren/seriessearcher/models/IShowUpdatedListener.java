package com.matthewsyren.seriessearcher.models;

/**
 * This interface is used to indicate to the class that implements this interface when Show objects are updated
 */

public interface IShowUpdatedListener {
    /**
     * Used to indicate that a Shows List has been updated
     */
    void showsUpdated();
}