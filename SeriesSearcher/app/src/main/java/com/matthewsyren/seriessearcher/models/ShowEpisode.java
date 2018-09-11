package com.matthewsyren.seriessearcher.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ShowEpisode
        implements Parcelable {
    private String episodeName;
    private String episodeAirDate;
    private String episodeRuntime;
    private String episodeSummary;

    /**
     * Constructor
     */
    public ShowEpisode(String episodeName, String episodeAirDate, String episodeRuntime, String episodeSummary) {
        this.episodeName = episodeName;
        this.episodeAirDate = episodeAirDate;
        this.episodeRuntime = episodeRuntime;
        this.episodeSummary = episodeSummary;
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

    protected ShowEpisode(Parcel in) {
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