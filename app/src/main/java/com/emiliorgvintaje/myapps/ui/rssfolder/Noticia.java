package com.emiliorgvintaje.myapps.ui.rssfolder;

public class Noticia {
    private String titulo, desc, link, fecha, imagen;


    /**
     * Clase de nuestro item del Recycler View
     *
     * @param titulo String
     * @param desc String
     * @param link String
     * @param fecha String
     * @param imagen String
     */
    public Noticia(String titulo, String desc, String link, String fecha, String imagen) {
        this.titulo = titulo;
        this.desc = desc;
        this.link = link;
        this.fecha = fecha;
        this.imagen = imagen;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
