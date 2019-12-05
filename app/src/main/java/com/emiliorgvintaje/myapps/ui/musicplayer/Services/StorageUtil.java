package com.emiliorgvintaje.myapps.ui.musicplayer.Services;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Clase que maneja el XML+JSON de almacenamiento de la lista de reproduccion y el index actual
 */
public class StorageUtil {

    //Nombre del storage
    private final String STORAGE = " com.emiliorg.myrss.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    /**
     * Almacenamos el arraylist de la lista de reproduccion
     * @param arrayList lista de reproduccion
     */
    public void storeAudio(ArrayList<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }


    /**
     * Recogemos la lista de reproduccion de nuestro archivo
     * @return playlist
     */
    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Guardamos el index actual de la cancion
     * @param index index actual
     */
    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    /**
     * Recogemos el index actual
     * @return index
     */
    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//devolvemos -1 si no encuentra nada
    }


    /**
     * Limpiamos el fichero para no haber sobreescritura y corrupcion de datos
     */
    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
    }
}
