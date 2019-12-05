package com.emiliorgvintaje.myapps.ui.videoplayer.model;

import android.graphics.Bitmap;

/**
 * Clase Video
 */
public class Video {
    String path, titulo, duracion;
    Bitmap thumb;
    String width, height, orientation;
    boolean selected;

    public Video(String path, Bitmap thumb, String titulo, String duracion, String width, String height, String orientation, boolean selected) {
        this.path = path;
        this.thumb = thumb;
        this.titulo = titulo;
        this.duracion = duracion;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.selected = selected;
    }

    public Video() {
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
