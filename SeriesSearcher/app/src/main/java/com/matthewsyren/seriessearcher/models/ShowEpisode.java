package com.matthewsyren.seriessearcher.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class holds the details for a Show's episode
 */

public class ShowEpisode
        implements Parcelable {
    //Declarations
    private String episodeName;
    private String episodeAirDate;
    private String episodeRuntime;
    private String episodeSummary;
    private String seasonNumber;
    private String episodeNumber;

    /**
     * Constructor
     * @param episodeName The name of the episode
     * @param episodeAirDate The date that the episode aired
     * @param episodeRuntime The runtime of the episode (in minutes)
     * @param episodeSummary The summary of the episode's plot
     * @param seasonNumber The season number of the episode
     * @param episodeNumber The episode number
     */
    public ShowEpisode(String episodeName, String episodeAirDate, String episodeRuntime, String episodeSummary, String seasonNumber, String episodeNumber) {
        this.episodeName = episodeName;
        this.episodeAirDate = episodeAirDate;
        this.episodeRuntime = episodeRuntime;
        this.episodeSummary = episodeSummary;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
    }

    /**
     * Getter method
     * @return The name of the episode
     */
    public String getEpisodeName() {
        return episodeName;
    }

    /**
     * Getter method
     * @return The date that the episode aired
     */
    public String getEpisodeAirDate() {
        return episodeAirDate;
    }

    /**
     * Getter method
     * @return The runtime of the episode in minutes
     */
    public String getEpisodeRuntime() {
        return episodeRuntime;
    }

    /**
     * Getter method
     * @return The summary of the episode's plot
     */
    public String getEpisodeSummary() {
        return episodeSummary;
    }

    /**
     * Getter method
     * @return The season number of the episode
     */
    public String getSeasonNumber() {
        return seasonNumber;
    }

    /**
     * Getter method
     * @return The episode number of the episode
     */
    public String getEpisodeNumber() {
        return episodeNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(episodeName);
        dest.writeString(episodeAirDate);
        dest.writeString(episodeRuntime);
        dest.writeString(episodeSummary);
    }

    private ShowEpisode(Parcel in) {
        episodeName = in.readString();
        episodeAirDate = in.readString();
        episodeRuntime = in.readString();
        episodeSummary = in.readString();
    }

    public static final Creator<ShowEpisode> CREATOR = new Creator<ShowEpisode>() {
        @Override
        public ShowEpisode createFromParcel(Parcel in) {
            return new ShowEpisode(in);
        }

        @Override
        public ShowEpisode[] newArray(int size) {
            return new ShowEpisode[size];
        }
    };
}