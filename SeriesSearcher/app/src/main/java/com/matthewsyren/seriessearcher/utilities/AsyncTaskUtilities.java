package com.matthewsyren.seriessearcher.utilities;

import android.os.AsyncTask;

/**
 * Performs tasks related to AsyncTasks
 */

public class AsyncTaskUtilities {
    /**
     * Cancels the AsyncTask
     * @param asyncTask The AsyncTask to be cancelled
     */
    public static void cancelAsyncTask(AsyncTask asyncTask){
        //Cancels the AsyncTask if it is still running
        if(isAsyncTaskRunning(asyncTask) && !asyncTask.isCancelled()){
            asyncTask.cancel(true);
        }
    }

    /**
     * Determines if the AsyncTask is running
     * @param asyncTask The AsyncTask to be checked
     * @return True if the AsyncTask is running, otherwise returns false
     */
    public static boolean isAsyncTaskRunning(AsyncTask asyncTask){
        return asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING;
    }
}