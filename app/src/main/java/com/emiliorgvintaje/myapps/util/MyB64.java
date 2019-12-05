package com.emiliorgvintaje.myapps.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.emiliorgvintaje.myapps.R;

import java.io.ByteArrayOutputStream;

public class MyB64 {

    /**
     * Metodo conversor de Base64 a Bitmap
     * @param b64 Archivo en Base64
     * @return Bitmap resultante
     */
    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

    }

    /**
     * Metodo conversor de Bitmap a Base64
     * @param bitmap Bitmap a convertir
     * @return Archivo en Base64
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }




    public static Bitmap comprimirImagen(Bitmap myBitmap, Context context) {
        Bitmap bitmap;
        try{
        float porcentaje = 144 / (float) myBitmap.getWidth();
         bitmap= Bitmap.createScaledBitmap(myBitmap, 144, (int) (myBitmap.getHeight()*porcentaje), false);
        }catch(Exception ex){
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.music_defaukt);

        }

        return bitmap;
    }

}
