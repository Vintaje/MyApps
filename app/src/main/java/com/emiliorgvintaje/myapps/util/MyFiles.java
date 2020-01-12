package com.emiliorgvintaje.myapps.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MyFiles {
    public static void borrarFichero(String path) {

        File fdelete = new File(path);
        if (fdelete.exists()) {
            fdelete.delete();
        }
    }

    public static boolean delete(final Context context, final File file) {
        try {
            final String where = MediaStore.MediaColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{
                    file.getAbsolutePath()
            };
            final ContentResolver contentResolver = context.getContentResolver();
            final Uri filesUri = MediaStore.Files.getContentUri("external");

            contentResolver.delete(filesUri, where, selectionArgs);

            if (file.exists()) {

                contentResolver.delete(filesUri, where, selectionArgs);
            }
        }catch(Exception ex){

            Toast.makeText(context, "Por favor, revise los permisos en ajustes",Toast.LENGTH_SHORT);
        }
        return !file.exists();
    }
}
