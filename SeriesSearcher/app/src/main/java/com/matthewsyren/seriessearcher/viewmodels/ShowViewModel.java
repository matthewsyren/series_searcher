package com.matthewsyren.seriessearcher.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.services.FirebaseService;
import com.matthewsyren.seriessearcher.utilities.AsyncTaskUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;

import java.util.ArrayList;

public class ShowViewModel
        extends AndroidViewModel
        implements ApiConnection.IApiConnectionResponse {
    //Variables
    private MutableLiveData<ArrayList<Show>> mShows = new MutableLiveData<>();
    private MutableLiveData<String> mResponse = new MutableLiveData<>();
    private ApiConnection mApiConnection = new ApiConnection();
    private MutableLiveData<Boolean> mOngoingOperation = new MutableLiveData<>();
    private boolean mOngoingShowOperation = false;

    /**
     * Constructor
     */
    public ShowViewModel(@NonNull Application application) {
        super(application);
        mOngoingOperation.setValue(false);
    }

    /**
     * Returns an Observable variable that is used to store JSON responses that are requested
     * @return A variable that will be updated whenever a JSON response is retrieved from an API
     */
    public MutableLiveData<String> getObservableResponse(){
        return mResponse;
    }

    /**
     * Returns an Observable variable that is used to indicate whether an operation is ongoing
     * @return A variable that will be true when there is an ongoing operation, and false when there is no ongoing operation
     */
    public MutableLiveData<Boolean> getObservableOngoingOperation(){
        return mOngoingOperation;
    }

    /**
     * Returns an observable variable that is used to store a list of Shows
     * @return A variable that is updated whenever Shows are marked as being added to My Series
     */
    public MutableLiveData<ArrayList<Show>> getObservableShows(){
        return mShows;
    }

    /**
     * Requests a JSON response from the specified URL
     * @param url The URL of the page to request a JSON response from
     */
    public void requestJsonResponse(String url){
        //Marks an operation as ongoing
        if(mOngoingOperation.getValue() != null && !mOngoingOperation.getValue()){
            mOngoingOperation.setValue(true);
        }

        //Requests the response from the URL
        mApiConnection = new ApiConnection();
        mApiConnection.setApiConnectionResponse(this);
        mApiConnection.execute(url);
    }

    /**
     * Requests JSON information for the Shows that the user has added to My Series (observe the response variable to get the JSON response with all the Shows' information)
     */
    public void requestShowsInMySeriesJson(){
        //Marks an operation as ongoing
        if(mOngoingOperation.getValue() != null && !mOngoingOperation.getValue()){
            mOngoingOperation.setValue(true);
        }

        //Fetches the IDs of the Shows that the user has added to My Series (the result is sent to the DataReceiver class with the ACTION_GET_SHOW_IDS_RESULT_CODE result code)
        Show.getShowIdsInMySeries(getApplication(), new DataReceiver(new Handler()));
    }

    /**
     * Fetches the shows the user has added to My Series using the Show IDs passed in with the ArrayList
     * @param showIds An ArrayList of links to the user's Shows
     */
    private void getUserShowData(ArrayList<String> showIds){
        if(showIds.size() > 0){
            //Transfers the data from showIds to an array containing the necessary links to the API (an array of links can be passed in to the ApiConnection class to fetch data from the API)
            String[] arrShows = new String[showIds.size()];
            for(int i = 0; i < showIds.size(); i++){
                arrShows[i] = LinkUtilities.getShowInformationLink(showIds.get(i));
            }

            //Fetches the information about the series
            fetchSeriesData(arrShows);
        }
        else{
            //Marks an operation as completed
            mOngoingOperation.setValue(false);

            //Resets the mShows variable
            mShows.setValue(new ArrayList<Show>());
        }
    }

    /**
     * Fetches data from the links passed into the method (the response will be sent to the onJsonResponseRetrieved method in this class)
     * @param arrShows An array with links to pages with information about the desired series
     */
    private void fetchSeriesData(String[] arrShows){
        //Marks an operation as ongoing
        if(mOngoingOperation.getValue() != null && !mOngoingOperation.getValue()){
            mOngoingOperation.setValue(true);
        }

        //Fetches the data from the TVMaze API
        mApiConnection = new ApiConnection();
        mApiConnection.setApiConnectionResponse(this);
        mApiConnection.execute(arrShows);
    }

    /**
     * Marks the Shows that the user has added to My Series (observe the shows variable to get the updated ArrayList of Shows)
     * @param shows The Shows to be marked
     */
    public void markShowsInMySeries(ArrayList<Show> shows){
        //Marks an operation as ongoing
        if(mOngoingOperation.getValue() != null && !mOngoingOperation.getValue()){
            mOngoingOperation.setValue(true);
        }

        //Updates flag variable
        mOngoingShowOperation = true;

        //Determines which Shows have been added to My Series by the user
        Show.markShowsInMySeries(getApplication(), shows, new DataReceiver(new Handler()));
    }

    /**
     * Cancels all ongoing AsyncTasks for this ViewModel
     */
    public void cancelAsyncTasks(){
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);
    }

    @Override
    public void onJsonResponseRetrieved(String response) {
        //Sets the response to empty
        if(response == null){
            response = "";
        }

        //Updates the mResponse variable
        mResponse.setValue(response);

        //Marks the operation as complete
        if(!mOngoingShowOperation){
            mOngoingOperation.setValue(false);
        }
    }

    @Override
    protected void onCleared() {
        //Cancels any outstanding AsyncTasks
        cancelAsyncTasks();
        super.onCleared();
    }

    /**
     * Class is used to retrieve results from Services
     */
    private class DataReceiver
            extends ResultReceiver {

        /**
         * Constructor
         */
        DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == FirebaseService.ACTION_GET_SHOW_IDS_RESULT_CODE){
                //Stores the user's Show IDs in an ArrayList
                ArrayList<String> showIds = resultData.getStringArrayList(FirebaseService.EXTRA_SHOW_IDS);

                if(showIds != null){
                    //Fetches information about the Shows that the user has added to My Series
                    getUserShowData(showIds);
                }
            }
            else if(resultCode == FirebaseService.ACTION_MARK_SHOWS_IN_MY_SERIES_RESULT_CODE){
                //Initialises variable
                ArrayList<Show> shows = new ArrayList<>();

                //Updates the mShows ArrayList with the new data
                if(resultData != null && resultData.containsKey(FirebaseService.EXTRA_SHOWS)){
                    shows = resultData.getParcelableArrayList(FirebaseService.EXTRA_SHOWS);
                }

                //Updates the mShows variable
                mShows.setValue(shows);

                //Marks an operation as complete
                mOngoingOperation.setValue(false);
                mOngoingShowOperation = false;
            }
        }
    }
}