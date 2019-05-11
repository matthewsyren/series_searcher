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
     */
    public String getEpisodeName() {
        return episodeName;
    }

    /**
     * Getter method
     */
    public String getEpisodeAirDate() {
        return episodeAirDate;
    }

    /**
     * Getter method
     */
    public String getEpisodeRuntime() {
        return episodeRuntime;
    }

    /**
     * Getter method
     */
    public String getEpisodeSummary() {
        return episodeSummary;
    }

    /**
     * Getter method
     */
    public String getSeasonNumber() {
        return seasonNumber;
    }

    /**
     * Getter method
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