package com.emiliorgvintaje.myapps.ui.juegos;

public class Juego {
    private int id;
    private String nombre,fecha_lanzamiento,plataforma, imagen, descripcion;
    private float precio;


    public Juego(String nombre, String fecha_lanzamiento, String plataforma, String descripcion, float precio, String imagen){
        this.id = 0;
        this.nombre = nombre;
        this.fecha_lanzamiento = fecha_lanzamiento;
        this.plataforma = plataforma;
        this.precio = precio;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    public Juego(){
        this.id = 0;
        this.nombre = "";
        this.fecha_lanzamiento = "";
        this.plataforma = "";
        this.precio = 0.00F;
        this.imagen = "";
        this.descripcion = "";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha_lanzamiento() {
        return fecha_lanzamiento;
    }

    public void setFecha_lanzamiento(String fecha_lanzamiento) {
        this.fecha_lanzamiento = fecha_lanzamiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }
}
