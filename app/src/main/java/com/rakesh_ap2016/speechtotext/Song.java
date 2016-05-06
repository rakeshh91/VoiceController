package com.rakesh_ap2016.speechtotext;

/**
 * Created by rakeshh91 on 4/20/2016.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String filePath;

    public Song(long songID, String songTitle, String songArtist,String path) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        filePath = path;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getFilePath(){return filePath;}
}
