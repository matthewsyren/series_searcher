package com.matthewsyren.seriessearcher.fragments;

import com.matthewsyren.seriessearcher.models.Show;

public interface IRemoveShowFromMySeriesFragmentOnClickListener {
    /**
     * Sends data back to the appropriate Activity once a decision has been made on RemoveShowFromMySeriesFragment
     */
    void onRemoveShowFromMySeriesFragmentClick(boolean removed, Show show);
}