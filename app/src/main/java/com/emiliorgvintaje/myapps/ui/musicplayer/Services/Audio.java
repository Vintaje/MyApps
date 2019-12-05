package com.emiliorgvintaje.myapps.ui.musicplayer.Services;

import java.io.Serializable;

/**
 * Clase Audio
 */
public class Audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String caratula;

    public Audio(String data, String title, String album, String artist, String caratula) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.caratula = caratula;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCaratula() {
        return caratula;
    }

    public void setCaratula(String caratula) {
        this.caratula = caratula;
    }
}
