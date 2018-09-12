package com.matthewsyren.seriessearcher.utilities;

public class LinkUtilities {
    /**
     * Returns the link to the page with information about the specified Show
     * @param showID The ID of the Show whose information is being requested
     */
    public static String getShowInformationLink(String showID){
        return "http://api.tvmaze.com/shows/" + showID;
    }

    /**
     * Returns the link to a the specified page of Shows
     * @param pageNumber The desired page number (could be randomised to fetch a List of random Shows)
     */
    public static String getMultipleShowPageLink(int pageNumber){
        return "http://api.tvmaze.com/shows?page=" + pageNumber;
    }

    /**
     * Returns a link to a page with the search results
     * @param searchText The text used to search
     */
    public static String getSearchLink(String searchText){
        return "http://api.tvmaze.com/search/shows?q=" + searchText;
    }

    /**
     * Returns a link to a page with information about a specific episode for a Show
     * @param showID The ID of the Show whose information is being requested
     * @param season The season number of the desired episode
     * @param episode The episode number of the desired episode
     */
    public static String getShowEpisodeInformationLink(String showID, int season, int episode){
        return "http://api.tvmaze.com/shows/" + showID + "/episodebynumber?season=" + season + "&number="  + episode;
    }
}