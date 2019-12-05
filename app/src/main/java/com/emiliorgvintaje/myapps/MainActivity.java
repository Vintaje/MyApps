package com.emiliorgvintaje.myapps;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.emiliorgvintaje.myapps.ui.juegos.Juego;
import com.emiliorgvintaje.myapps.ui.juegos.detalles.JuegosDetalleFragment;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.Audio;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.MediaPlayerService;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.StorageUtil;
import com.emiliorgvintaje.myapps.ui.rssfolder.noticiaDetalle.NoticiaDetalleFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Pasos importantes para el proyecto!:
 * <p>
 * 1- Agregar permisos necesarios, internet, escritura, lectura
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.CAMERA" />
 * <p>
 * 2- Para recyclerView, que no se olvide cambiar la funcion getItemCount a {return milista.size();}
 * <p>
 * 3- Todas las operaciones que requieran internet(excepto Picasso que lo usamos en el adaptador)
 * se deben hacer en una AsyncTask
 */


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    public FloatingActionButton fab;
    public Toolbar toolbar;

    public Menu menushare;
    public DrawerLayout drawer;
    public static boolean editando, nuevo;
    public Menu menu_save;
    public static Context context;
    public static ArrayList<Audio> savelist;
    public static boolean mode = false;

    public static boolean newlist = false;


    //SERVICIO MUSICA
    public MediaPlayerService player;
    public boolean serviceBound = false;

    /**
     * Instancia de la conexion del servicio
     */
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance

            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            getApplication().unbindService(this);
        }
    };




    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = new MediaPlayerService();
        try {
            dbPorDefecto();
        } catch (SQLiteConstraintException ex) {

        }
        try {
            this.setContentView(R.layout.activity_main);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

        }catch(Exception ex){
            this.onCreate(null);
        }




        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_juegos, R.id.nav_music, R.id.nav_send, R.id.nav_sensor, R.id.nav_share, R.id.nav_videos)
                .setDrawerLayout(drawer)
                .build();
        //GRUPO MYCLASS
        context = getApplicationContext();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tweetUrl = NoticiaDetalleFragment.noticiatweet.getLink();
                Uri uri = Uri.parse(tweetUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        editando = false;
        setDrawerLocked(false);
        pedirMultiplesPermisos();


    }

    public void onResume() {

        super.onResume();
        NotificationManager notiManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = notiManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == MediaPlayerService.NOTIFICATION_ID) {



                MainActivity.newlist = true;


                MainActivity.mode = notification.getNotification().extras.getBoolean("shuffle", false);


            }}

        }
    }


    @Override
    protected void onDestroy() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        try {
            this.unbindService(serviceConnection);
            new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
        }catch(Exception ignored){

        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    private void pedirMultiplesPermisos() {
        // Indicamos el permisos y el manejador de eventos de los mismos
        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // ccomprbamos si tenemos los permisos de todos ellos
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "¡Todos los permisos concedidos!", Toast.LENGTH_SHORT).show();
                        }

                        // comprobamos si hay un permiso que no tenemos concedido ya sea temporal o permanentemente
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // abrimos un diálogo a los permisos
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }


                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Existe errores! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    public void onBackPressed() {
        if (!editando) {
            super.onBackPressed();

        } else {

            alertDialog();
        }
    }

    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Se han realizado cambios, ¿quiere descartarlos?");
        dialog.setTitle("Alerta");
        dialog.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Toast.makeText(getApplicationContext(), "Cambios descartados", Toast.LENGTH_LONG).show();

                        MainActivity.super.onBackPressed();

                    }
                });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    public void dbPorDefecto() {
        DBC bdEjemplo = new DBC(getApplicationContext(), "BDJuegos", null, 1);
        SQLiteDatabase db = bdEjemplo.getWritableDatabase();
        try {


            ContentValues default_platforms = new ContentValues();

            default_platforms.put("nombre", "PS4");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "XBOXONE");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "XBOX360");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "3DS2DS");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "NSWITCH");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "PCFISICO");
            db.insert("Plataforma", null, default_platforms);
            default_platforms.put("nombre", "PCDIGITAL");
            db.insert("Plataforma", null, default_platforms);

            db.close();

        } catch (
                SQLiteConstraintException ex) {
            System.out.println("Los datos ya existen");
            db.close();
        }

    }

    /**
     * Bloqueamos el navigation drawer, usado en el fragment de mas Detalles de cada noticia:
     * NoticiaDetalleFragment.class
     * y en rssFragment.class para habilitar y deshabilitar
     *
     * @param enabled verdadero = bloqueado; falso = desbloqueado
     */
    public void setDrawerLocked(boolean enabled) {
        if (enabled) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        menushare = menu;
        hideShare();

        getMenuInflater().inflate(R.menu.menu_save, menu);
        menu_save = menu;
        hideSave();

        return true;
    }




    /**
     * Mostramos el menu de compartir usado en el fragment de mas Detalles
     * de cada noticia: NoticiaDetalleFragment.class
     */
    public void showShare() {
        MenuItem shareit = menushare.findItem(R.id.miMainShareIt);
        MenuItem backarrow = menushare.findItem(R.id.backArrow);
        shareit.setVisible(true);
        backarrow.setVisible(true);
    }

    /**
     * Escondemos el menu de compartir usado en el fragment de mas Detalles
     * de cada noticia: NoticiaDetalleFragment.class
     */
    public void hideShare() {
        MenuItem item = menushare.findItem(R.id.miMainShareIt);
        MenuItem backarrow = menushare.findItem(R.id.backArrow);
        item.setVisible(false);
        backarrow.setVisible(false);
    }

    /**
     * Mostramos el menu de guardar o agregar foto en el Fragment de Mas
     * Detalles de Mis Juegos en caso de que este sea editable
     */
    public void showSave() {
        MenuItem saveit = menu_save.findItem(R.id.btSaveIt);
        MenuItem backarrow = menu_save.findItem(R.id.backArrowSave);
        saveit.setVisible(true);
        backarrow.setVisible(true);
    }

    /**
     * Escondemos el menu de guardar o agregar foto en el Fragment de Mas
     * Detalles de Mis Juegos en caso de que este sea editable
     */
    public void hideSave() {
        MenuItem saveit = menu_save.findItem(R.id.btSaveIt);
        MenuItem backarrow = menu_save.findItem(R.id.backArrowSave);
        saveit.setVisible(false);
        backarrow.setVisible(false);
    }

    public void hideBack() {
        MenuItem backarrow = menushare.findItem(R.id.backArrow);
        backarrow.setVisible(false);
    }

    public void showBack() {
        MenuItem backarrow = menushare.findItem(R.id.backArrow);
        backarrow.setVisible(true);
    }


    /**
     * Seleccion de items y su accion en el menu, compartiremos la noticia o iremos al Fragment anterior
     * dependiendo de la accion
     *
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miMainShareIt) {

            //Realizamos un Intent pasado extras relacionados al asunto y al cuerpo del mensaje
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "[@Eurogamer_es]" + NoticiaDetalleFragment.noticiatweet.getTitulo()
                    + "\n Enlace: " + NoticiaDetalleFragment.noticiatweet.getLink() +
                    "\n\n\nEnviado desde MyAPPs de Emilio Rubiales Twitter:https://twitter.com/vintajeskull98";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Noticias de videojuegos y relacionados" + "\n\n");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(sharingIntent, "Compartido desde MyAPPs de Emilio Rubiales"));
            return true;
        } else if (id == R.id.backArrow) {
            //Accion de volver a l pulsar un boton
            onBackPressed();
            return true;
        } else if (id == R.id.btSaveIt) {
            Juego j = JuegosDetalleFragment.juegoguardado;
            if (comprobarDatosJuego(j)) {
                if (j.getImagen().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Debes introducir una imagen", Toast.LENGTH_SHORT).show();

                } else {
                    DBC bdJuegos = new DBC(getApplicationContext(), "BDJuegos", null, 1);

                    SQLiteDatabase db = bdJuegos.getWritableDatabase();

                    if (JuegosDetalleFragment.nuevo) {

                        bdJuegos.insert(j);


                    } else {
                        bdJuegos.update(j);
                    }
                    db.close();
                    hideSoftKeyboard(this);


                    Toast.makeText(getApplicationContext(), "Cambios Guardados", Toast.LENGTH_SHORT).show();
                    super.onBackPressed();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Solo puede dejarse en blanco la sinopsis", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.backArrowSave) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {

        }
    }

    private boolean comprobarDatosJuego(Juego j) {

        if (j.getNombre().trim().isEmpty() || j.getPlataforma().trim().isEmpty()
                || j.getFecha_lanzamiento().isEmpty() || Double.isNaN(j.getPrecio())) {
            return false;
        }

        return true;
    }

    /**
     * Mostrar Float Button
     */
    public void showFloatingActionButton() {
        fab.show();
    }

    /**
     * Esconder Float Button
     */
    public void hideFloatingActionButton() {
        fab.hide();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}
