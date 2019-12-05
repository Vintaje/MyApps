package com.emiliorgvintaje.myapps.ui.musicplayer;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.Playing.PlayingFragment;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.Audio;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.MediaPlayerService;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.StorageUtil;
import com.emiliorgvintaje.myapps.util.Times;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;

public class MusicFragment extends Fragment {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.emiliorgvintaje.myrss.PlayNewAudio";

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private MusicAdapter adaptador;
    private RecyclerView recyclerView;
    private View root;


    ArrayList<Audio> audioList;
    ArrayList<Audio> prelista;
    ArrayList<Audio> preshuffle;
    private Handler handler;
    private SeekBar seekBar;
    private ImageButton play, next, prev;
    private ImageView shuffle;
    private Runnable runnable, actual;
    private TextView titulo, artistalbum, playMode, current, total;

    private LinearLayout seekbarLayout;
    private String tituloRecuperado;

    private Intent playerIntent;
    private LinearLayout playModeLayout, linearActualSong;
    private boolean playing;
    private int audioPos;

    public MusicFragment() {
    }




    /**
     * Instancia de nuestro Servicio MediaPlayer
     *
     * @return player service
     */
    public MediaPlayerService getPlayer() {
        return ((MainActivity)getActivity()).player;
    }


    /**
     * Creacion de la vista del Fragment
     *
     * @param inflater           LayoutInflater
     * @param container          Grupo de Vistas
     * @param savedInstanceState Instancia salvada
     * @return Vista actual
     */
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.music_fragment, container, false);
        playing = false;

        audioPos = -2;
        instanciaItems();

        handleSeekbar();

        instanciaListenerItems();
        if (MainActivity.mode) {

            shuffle.setImageResource(R.drawable.bt_shuffle);
            playMode.setText(R.string.rep_shuffler);
        } else {
            shuffle.setImageResource(R.drawable.bt_repeat);
            playMode.setText(R.string.rep_normal);
        }
        return root;

    }


    /**
     * Una vez creada la vista, rellenamos la lista con las canciones
     *
     * @param savedInstanceState Instancia salvada
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        if (checkAndRequestPermissions()) {

            StorageUtil storage = new StorageUtil(getContext());

        new musicLoaderXML().execute();


        }


    }

    /**
     * onResume que esconde el floatButton del MainActivity
     */
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).hideFloatingActionButton();
        try {
            comprobarReproduccion();
        }catch(Exception ignored){}
    }

    public void comprobarReproduccion(){
        if (((MainActivity) getActivity()).player.getMediaPlayer().isPlaying()) {
            playerIntent = new Intent(getContext(), MediaPlayerService.class);
            seekbarLayout.setVisibility(View.VISIBLE);

            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, ((MainActivity) getActivity()).serviceConnection, Context.BIND_AUTO_CREATE);
            NotificationManager notiManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                StatusBarNotification[] notifications = notiManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() == MediaPlayerService.NOTIFICATION_ID) {

                        tituloRecuperado = notification.getNotification().extras.getString("titulo");


                        artistalbum.setText(tituloRecuperado);


                    }

                }
            }

            audioActual();
        }

        if (MainActivity.mode) {

            shuffle.setImageResource(R.drawable.bt_shuffle);
            playMode.setText(R.string.rep_shuffler);
        } else {
            shuffle.setImageResource(R.drawable.bt_repeat);
            playMode.setText(R.string.rep_normal);
        }
    }

    /**
     * OnDestroy que desvincula el handler de todos los Hilos y el propio servicio de la App
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            handler = new Handler();
            handler.removeCallbacksAndMessages(runnable);
            handler.removeCallbacksAndMessages(actual);
        } catch (Exception ignored) {
        }
        MainActivity.savelist = audioList;

    }


    /**
     * Instancia de los listener de la UI
     */
    public void instanciaListenerItems() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pasamos de cancion
                ((MainActivity)getActivity()).player.skipToNext();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Volvemos a la cancion anterior
                ((MainActivity)getActivity()).player.skipToPrevious();

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //Comprobamos si esta reproduciendo
                    if (((MainActivity)getActivity()).player.getMediaPlayer().isPlaying()) {
                        play.setImageResource(R.drawable.bt_play);//Cambiamos la imagen
                        //Pausamos la cancion
                        ((MainActivity)getActivity()).player.pauseMedia();
                    } else {
                        //Continuamos reproduciendo
                        ((MainActivity)getActivity()).player.resumeMedia();
                        play.setImageResource(R.drawable.bt_stop);//Cambiamos la imagen
                    }
                } catch (Exception ex) {
                    //Volvemos a iniciar el servicio
                    playerIntent = new Intent(getContext(), MediaPlayerService.class);
                    getActivity().startService(playerIntent);
                    getActivity().bindService(playerIntent, ((MainActivity)getActivity()).serviceConnection, Context.BIND_AUTO_CREATE);
                }

            }
        });

        playModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if (!MainActivity.mode) {
                    //Reproducimos aleatoriamente las canciones
                    playMode.setText(R.string.rep_shuffler);
                    shuffle.setImageResource(R.drawable.bt_shuffle);
                    new shuffleLoader().execute();
                    MainActivity.mode = true;


                } else {
                    //Reproducimos las canciones ordenadas por nombre
                    playMode.setText(R.string.rep_normal);
                    shuffle.setImageResource(R.drawable.bt_repeat);
                    new musicLoader().execute();
                    MainActivity.mode = false;


                }
            }
        });
        //Si se hace click en una cancion, se muestra el fragment con la cancion
        linearActualSong.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!playing) {
                    setPlaying(true);
                    StorageUtil storage = new StorageUtil(getContext());
                    PlayingFragment playingFragment;
                    int audioIndex = storage.loadAudioIndex();
                    playingFragment = new PlayingFragment(storage.loadAudio(), audioIndex, returnThis());
                    getFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_effect, R.anim.fragment_effect_exit, R.anim.fragment_effect, R.anim.fragment_effect_exit)
                            .add(R.id.nav_host_fragment, playingFragment).addToBackStack("PLAYING_FRAG").commit();
                }
            }
        });

    }

    /**
     * Instancia de todos los elementos de la UI
     */
    public void instanciaItems() {
        recyclerView = (RecyclerView) root.findViewById(R.id.rvMusicGlobal);
        seekbarLayout = (LinearLayout) root.findViewById(R.id.LinearMusicProgress);
        seekbarLayout.setVisibility(View.INVISIBLE);

        seekBar = (SeekBar) root.findViewById(R.id.seekBar);
        playMode = (TextView) root.findViewById(R.id.tvTypePlayer);
        play = (ImageButton) root.findViewById(R.id.ibPlayMusic);
        titulo = (TextView) root.findViewById(R.id.tvCancionActual);
        artistalbum = (TextView) root.findViewById(R.id.tvArtistAlbumActual);
        shuffle = (ImageView) root.findViewById(R.id.ibShuffleMusic);

        current = (TextView) root.findViewById(R.id.tvTimeRep);
        total = (TextView) root.findViewById(R.id.tvTimeTotal);

        playModeLayout = (LinearLayout) root.findViewById(R.id.repModeLayout);
        handler = new Handler();
        linearActualSong = (LinearLayout) root.findViewById(R.id.linearActualSong);

        next = (ImageButton) root.findViewById(R.id.ibNextMusic);
        prev = (ImageButton) root.findViewById(R.id.ibPrevMusic);


    }


    /**
     * Comprobacion del estado del reproductor
     *
     * @return Estado de reproduccion
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Setter del estado de reproduccion
     *
     * @param playing estado de reproduccion
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * Getter del Fragment
     *
     * @return Fragment de Musica
     */
    public MusicFragment returnThis() {

        return this;
    }


    /**
     * Metodo que dado un Index reproduce el audio que le corresponde en base a la lista de reproduccion
     * guardada en nuestro ArrayList. Esta lista se guarda de nuevo en nuestro XML en formato JSON para
     * poder manejarla desde el propio servicio
     *
     * @param audioIndex Index, posicion del audio a reproducir
     */
    public void playAudio(int audioIndex) {
        //Check is service is active
        if (!((MainActivity)getActivity()).serviceBound) {
            //Almacena la playlist serializada en el XML
            StorageUtil storage = new StorageUtil(getContext());
            storage.storeAudioIndex(audioIndex);
            audioPos = audioIndex;
            playerIntent = new Intent(getContext(), MediaPlayerService.class);
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, ((MainActivity)getActivity()).serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Almacena el nuevo audio index
            StorageUtil storage = new StorageUtil(getContext());
            storage.storeAudioIndex(audioIndex);
            audioPos = audioIndex;
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            getActivity().sendBroadcast(broadcastIntent);
        }
        seekbarLayout.setVisibility(View.VISIBLE);
        play.setImageResource(R.drawable.bt_stop);

        titulo.setText(String.format("%s | %s", getResources().getString(R.string.reproduciendo), audioList.get(audioIndex).getTitle()));
        artistalbum.setText(String.format("%s | %s", audioList.get(audioPos).getArtist(), audioList.get(audioIndex).getAlbum()));
        titulo.setSelected(true);
        artistalbum.setSelected(true);

        handler = new Handler();
        audioActual();

        if (!playing) {
            setPlaying(true);
            PlayingFragment playingFragment;
            StorageUtil storage = new StorageUtil(getContext());
            playingFragment = new PlayingFragment(storage.loadAudio(), audioIndex, this);
            getFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_effect, R.anim.fragment_effect_exit, R.anim.fragment_effect, R.anim.fragment_effect_exit)
                    .add(R.id.nav_host_fragment, playingFragment).addToBackStack(null).commit();
            playing = true;
        }
    }


    /**
     * Sobrecarga del metodo el cual inicia Hilos para manejar los intent del Servicio
     *
     * @param audioIndex
     * @param back
     */
    public void playAudio(int audioIndex, boolean back) {
        //Check is service is active
        if (!((MainActivity)getActivity()).serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            new Thread() {
                public void run() {

                    playerIntent = new Intent(getContext(), MediaPlayerService.class);


                    getActivity().startService(playerIntent);
                    getActivity().bindService(playerIntent, ((MainActivity)getActivity()).serviceConnection, Context.BIND_AUTO_CREATE);
                }
            }.start();


        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            new Thread() {
                public void run() {
                    //Service is active
                    //Send a broadcast to the service -> PLAY_NEW_AUDIO
                    Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                    getActivity().sendBroadcast(broadcastIntent);

                }
            }.start();
        }

        seekbarLayout.setVisibility(View.VISIBLE);
        play.setImageResource(R.drawable.bt_stop);

        artistalbum.setText(audioList.get(audioIndex).getTitle());


        handler = new Handler();
        audioActual();


    }


    /**
     * Hilos que trabajan en la UI para mantener en la propia interfaz los datos correctos, tanto
     * en los textos como en la SeekBar
     */
    public void audioActual() {

        new Thread() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(runnable = new Runnable() {
                        @Override
                        public void run() {
                            seekbarLayout.setVisibility(View.VISIBLE);
                            if (((MainActivity)getActivity()).player.getMediaPlayer() != null) {
                                try {
                                    seekBar.setMax(((MainActivity)getActivity()).player.getMediaPlayer().getDuration() / 1000);
                                    int mCurrentPosition = ((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition() / 1000;
                                    seekBar.setProgress(mCurrentPosition);
                                    current.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition()));
                                    total.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getDuration()));
                                } catch (Exception ex) {
                                }
                            }
                        }
                    });
                    handler.postDelayed(this, 1000);
                } catch (Exception ex) {
                }
            }


        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(actual = new Runnable() {
                        @Override
                        public void run() {

                            if (((MainActivity)getActivity()).player.getMediaPlayer() != null) {
                                if (((MainActivity)getActivity()).player.getMediaPlayer().isPlaying()) {

                                    play.setImageResource(R.drawable.bt_stop);
                                    StorageUtil storageUtil = new StorageUtil(getContext());
                                    try {
                                        if (audioPos != storageUtil.loadAudioIndex()) {
                                            audioPos = storageUtil.loadAudioIndex();
                                            titulo.setText(String.format("%s | %s", getResources().getString(R.string.reproduciendo), audioList.get(audioPos).getTitle()));
                                            artistalbum.setText(String.format("%s | %s", audioList.get(audioPos).getArtist(), audioList.get(audioPos).getAlbum()));
                                            titulo.setSelected(true);
                                            artistalbum.setSelected(true);
                                        }
                                    } catch (Exception ex) {
                                    }
                                } else {
                                    play.setImageResource(R.drawable.bt_play);
                                }
                            }
                        }
                    });
                    handler.postDelayed(this, 300);
                } catch (Exception ex) {
                }
            }


        }.start();

    }


    /**
     * Listener de la SeekBar para cuando el usuario cambia la posicion actual
     */
    private void handleSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (((MainActivity)getActivity()).player.getMediaPlayer() != null && fromUser) {
                    ((MainActivity)getActivity()).player.getMediaPlayer().seekTo(progress * 1000);
                    current.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.whitebox), PorterDuff.Mode.SRC_ATOP);

    }


    /**
     * Metodo para checkear los permisos y, en caso de que no los tenga, volverlos a pedir
     *
     * @return Estado de los permisos
     */
    private boolean checkAndRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadPhoneState = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_PHONE_STATE);
            int permissionStorage = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionRecord = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO);
            int permissionWrite = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                StorageUtil storage = new StorageUtil(getContext());
                audioList = new ArrayList<>();
                audioList = storage.loadAudio();

                if (audioList == null) {
                    new musicLoader().execute();
                } else {
                    initRecyclerView();
                }
            }

            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (permissionRecord != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.RECORD_AUDIO);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


    /**
     * Metodo para hacer una request de los permisos
     *
     * @param requestCode  Codigo
     * @param permissions  Permisos
     * @param grantResults int de Resultados
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        String TAG = "LOG_PERMISSION";
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions

                    if (perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Phone state and storage permissions granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        StorageUtil storage = new StorageUtil(getContext());
                        audioList = new ArrayList<>();
                        audioList = storage.loadAudio();

                        if (audioList == null) {
                            new musicLoader().execute();
                        } else {
                            initRecyclerView();
                        }
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.READ_PHONE_STATE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.RECORD_AUDIO) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(getContext(), "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }


    }

    /**
     * Metodo para motrar un Dialogo con un mensaje y un boton de Ok
     *
     * @param message    Mensaje
     * @param okListener Listener del boton de OK
     */
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    /**
     * Metodo para cargar todos los archivos de Audio en la lista de reproduccion y almacenarlos en nuestro
     * archivo XML con el array en JSon
     */
    public void loadAudio() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//Donde vamos a buscar
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";//Tipo de archivo
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";//Orden
        //Creamos el cursor
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {//Si tiene contenido
            audioList = new ArrayList<>();
            //Recorremos el cursor
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String photo = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //Guardamos en la lista de audios
                audioList.add(new Audio(data, title, album, artist, photo));
            }
        }
        if (cursor != null) {
            cursor.close();//Cerramos el cursor}

            prelista = audioList;//Guardamos una lista previa
            StorageUtil storage = new StorageUtil(getContext());//Instanciamos nuestro StorageUtil
            storage.clearCachedAudioPlaylist();//Limpiamos el XML
            storage.storeAudioIndex(-1);//Almacenamos el audio
            storage.storeAudio(prelista);//Almacenamos la lista
        }
    }

    /**
     * Extraccion del bitmap en base al ID de la caratula de cada cancion
     *
     * @param album_id Id de la caratula
     * @return Bitmap de la caratula
     */
    public Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = getContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        if (bm == null) {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.musicdef);
        }
        return bm;
    }


    /**
     * Instancia del recycler View
     */
    public void initRecyclerView() {

        if (audioList != null && audioList.size() > 0) {

            adaptador = new MusicAdapter(audioList, this, ((MainActivity)getActivity()).player, getFragmentManager());

            recyclerView.setAdapter(adaptador);
            runLayoutAnimation(recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setItemViewCacheSize(17);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        }

    }

    /**
     * Metodo que llama a la AsyncTask que recoge todos los audios
     */
    public void loadList() {
        new musicLoader().execute();

    }


    /**
     * Ejecucion de la animacion para la carga de datos en el recycler
     *
     * @param recyclerView Recycler View
     */
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);

        recyclerView.scheduleLayoutAnimation();
    }


    /**
     * AsynTask que llama al metodo en background de rellenar la lista de canciones
     */
    public class musicLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            playModeLayout.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loadAudio();//Carga mediante un metodo los archivos en un arraylist<Audio>
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initRecyclerView();//Instancia el recycler
            playModeLayout.setClickable(true);
            if (!titulo.getText().equals(getResources().getString(R.string.reproduciendo))) {
                playAudio(0);//Inicia el primer audio
            }
        }
    }

    /**
     * AsyncTask que carga la lista de reproduccion ordenada aleatoriamente y la almacena en el XML
     */
    public class shuffleLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            playModeLayout.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            preshuffle = prelista;//Recoge la lista
            Collections.shuffle(preshuffle);//Reordena aleatoriamente la lista
            audioList = new ArrayList<>();
            audioList = preshuffle;
            StorageUtil storage = new StorageUtil(getContext());//Instanciamos nuestra clase storage
            storage.clearCachedAudioPlaylist();//limpiamos el xml
            storage.storeAudioIndex(-1);//almacenamos el index
            storage.storeAudio(audioList);//almacenamos la lista
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initRecyclerView();//Reinstanciamos nuestro recycler
            playModeLayout.setClickable(true);
            playAudio(0);//Reproducimos el primer audio
        }

    }


    public class musicLoaderXML extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            //Llamando a nuestra clase StorageUtil, recoge los datos del XML con los metodos
            StorageUtil storage = new StorageUtil(getContext());
            audioList = new ArrayList<>();
            audioList = storage.loadAudio();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Si la lista ha salido null significa que el xml esta vacio, procedemos
            //a llamar la siguiente tarea que carga del sistema de archivos
            if (audioList == null) {
                new musicLoader().execute();
            } else {
                prelista = audioList;
                initRecyclerView();
            }
        }
    }

}


