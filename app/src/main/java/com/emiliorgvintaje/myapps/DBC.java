package com.emiliorgvintaje.myapps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.emiliorgvintaje.myapps.ui.juegos.Juego;

import java.util.ArrayList;

public class DBC extends SQLiteOpenHelper {
    private static final String sqlCrear = "CREATE TABLE Juego(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre VARCHAR(255) NOT NULL," +
            "fecha_lanzamiento DATE NOT NULL," +
            "precio DECIMAL(6,2) NOT NULL,"+
            "plataforma VARCHAR(7) NOT NULL," +
            "descripcion VARCHAR(1000),"+
            "imagen BLOB," +
            "FOREIGN KEY (plataforma) " +
            "REFERENCES Plataforma(nombre));";

    private static final String sqlCategorias ="CREATE TABLE Plataforma(nombre VARCHAR(7) PRIMARY KEY);";


    public DBC(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCrear);
        db.execSQL(sqlCategorias);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Accion de seleccionar datos de la BBDD sin ningun filtro
     *
     */
    public ArrayList<Juego> seleccionarData() {
        //Abrimos la base de datos 'BDEjemplo' en modo lectura

        SQLiteDatabase bd = this.getReadableDatabase();

        ArrayList<Juego> juegos = new ArrayList<>();
        //Si hemos abierto correctamente la base de datos
        if (bd != null) {
            //Seleccionamos todos
            Cursor c = bd.rawQuery(" SELECT id,nombre, fecha_lanzamiento, plataforma, descripcion, precio, imagen FROM Juego", null);
            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no haya más registros
                do {
                    Juego j = new Juego(c.getString(1), c.getString(2), c.getString(3), c.getString(4)
                            , c.getFloat(5), c.getString(6));
                    j.setId(c.getInt(0));
                    juegos.add(j);

                    Log.d("IMAGENES", j.getImagen());
                } while (c.moveToNext());
            }
            //Cerramos la base de datos
            c.close();


        }
        bd.close();
        return juegos;
    }

    /**
     * Accion de seleccionar datos de la BBDD aplicando un filtro de busqueda y un tipo de orden
     *
     * @param filtro Campo a filtrar
     * @param tipo Tipo de orden: Ascendente o Descendente
     */
    public ArrayList<Juego> seleccionarData(String filtro, boolean tipo) {
        //Abrimos la base de datos 'BDEjemplo' en modo lectura

        SQLiteDatabase bd = this.getReadableDatabase();

        ArrayList<Juego> juegos = new ArrayList<>();
        //Si hemos abierto correctamente la base de datos
        if (bd != null) {

            String modo = null;
            if (tipo) {
                modo = "DESC";

            } else {

                modo = "ASC";
            }


            //Seleccionamos todos
            Cursor c = bd.rawQuery(" SELECT id,nombre, fecha_lanzamiento, plataforma, descripcion, precio, imagen FROM Juego ORDER BY " + filtro + " " + modo, null);
            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no haya más registros
                do {
                    Juego j = new Juego(c.getString(1), c.getString(2), c.getString(3), c.getString(4)
                            , c.getFloat(5), c.getString(6));
                    j.setId(c.getInt(0));
                    juegos.add(j);

                    Log.d("IMAGENES", j.getImagen());
                } while (c.moveToNext());
            }
            //Cerramos la base de datos
            c.close();


        }
        bd.close();

        return juegos;
    }


    public void delete(int id){
        SQLiteDatabase bd = this.getReadableDatabase();

        bd.delete("Juego", "id=" + id, null);

        bd.close();
    }

    public void insert(Juego juego){


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", juego.getNombre());
        values.put("fecha_lanzamiento", juego.getFecha_lanzamiento());
        juego.setPrecio((float)(Math.round(juego.getPrecio()*100.0)/100.0));
        values.put("precio", juego.getPrecio());
        values.put("plataforma", juego.getPlataforma());
        values.put("descripcion", juego.getDescripcion());
        values.put("imagen", juego.getImagen());

        db.insert("Juego", null, values);

    }

    public void update(Juego j){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", j.getNombre());
        values.put("fecha_lanzamiento", j.getFecha_lanzamiento());
        j.setPrecio((float)(Math.round(j.getPrecio()*100.0)/100.0));
        values.put("precio", j.getPrecio());
        values.put("plataforma", j.getPlataforma());
        values.put("descripcion", j.getDescripcion());
        values.put("imagen", j.getImagen());
        db.update("Juego", values, "id=" + j.getId(), null);

    }
}
