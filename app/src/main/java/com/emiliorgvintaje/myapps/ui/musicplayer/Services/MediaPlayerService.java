package com.emiliorgvintaje.myapps.ui.musicplayer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.MusicFragment;

import java.io.FileDescriptor;
import java.util.ArrayList;


/**
 * Clase MediaPlayerService, servicio que controla la ejecucion del media player
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {

    /**
     * Variables Final que manejan los codigos de accion para el servicio
     */
    public static final String ACTION_PLAY = "com.emiliorg.myrss.audioplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.emiliorg.myrss.audioplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.emiliorg.myrss.audioplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.emiliorg.myrss.audioplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.emiliorg.myrss.audioplayer.ACTION_STOP";

    private MediaPlayer mediaPlayer;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;

    //AudioPlayer notification ID
    public static final int NOTIFICATION_ID = 101;

    //Posicion para pausar/resumir
    private int resumePosition;

    //AudioFocus
    private AudioManager audioManager;

    //Binder dado a los clientes
    private final IBinder iBinder = new LocalBinder();

    //Lista de los archivos disponibles
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //Objeto del audio activo


    //Manejo de llamadas entrantes
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    /**
     * Getter del MediaPlayer
     * @return MediaPlayer instanciado
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    /**
     * Metodos del ciclo de vida del servicio
     */
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        //Cambio del dispositivo de audio
        registerBecomingNoisyReceiver();
        //Listener a la espera de un nuevo audio
        register_playNewAudio();
    }

    //Peticion para el inicio del servicio
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            //Cargamos la lista desde SharedPrefs
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //Comprobar que el indice esta dentro del rango establecido
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Peticion del audioFocus
        if (requestAudioFocus() == false) {
            //No puede ganar el foco
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
                buildNotification(PlaybackStatus.PLAYING);
            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }

        }

        //Manejo del intent de MediaControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Desbindeamos el servicio, desvinculamos el mediaplayer y quitamos la notificacion
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        stopMedia();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Desactivamos el listener de llamadas entrantes
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //Eliminar los Broadcast Receiver
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();

    }
    /**
     * Bindeo del servicio
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Devuelve la instancia actual del servicio
            return MediaPlayerService.this;
        }
    }


    /**
     * Metodos del media player
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.


        //Skip a la siguiente cancion al completarse
        skipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Continuar la reproduccion
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Pierde el foco por un tiempo, desvinculamos el media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Pierde el foco por un corto tiempo, para la cancion y luego la continua
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Pierde el foco por un corto tiempo pero recupera la reproduccion
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    /**
     * AudioFocus
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Foco ganado
            return true;
        }
        //No podra ganar el foco
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    /**
     * MediaPlayer acciones
     */
    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();//nueva instancia del media player

        //Seteamos los listener del media player
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reseteamos el media player
        mediaPlayer.reset();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Seteamos el datasource al audio actual
            mediaPlayer.setDataSource(activeAudio.getData());
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    /**
     * Reproducimos una cancion
     */
    private void playMedia() {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

            }
            buildNotification(PlaybackStatus.PLAYING);
        }catch(Exception ex){

            initMediaPlayer();
        this.playMedia();
    }
    }

    /**
     * Paramos la reproduccion
     */
    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        removeNotification();
    }

    /**
     * Pausamos la reproduccion
     */
    public void pauseMedia() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                resumePosition = mediaPlayer.getCurrentPosition();
            }
            buildNotification(PlaybackStatus.PAUSED);
        }catch(Exception ex){

            initMediaPlayer();
            this.playMedia();
        }

    }

    /**
     * Continuamos la reproduccion
     */
    public void resumeMedia() {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(resumePosition);
                mediaPlayer.start();
                buildNotification(PlaybackStatus.PLAYING);
            }
        }catch(Exception ex){

            initMediaPlayer();
            this.playMedia();
        }

    }

    /**
     * Pasamos a la siguiente cancion
     */
    public void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //Si es el ultimo de la playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //cogemos la siguiente
            activeAudio = audioList.get(++audioIndex);
        }

        //Actualizamos el index en el XML
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reseteamos el mp
        mediaPlayer.reset();
        initMediaPlayer();
        buildNotification(PlaybackStatus.PLAYING);
    }

    /**
     * Pasar a la cancion anterior
     */
    public void skipToPrevious() {

        if (audioIndex == 0) {
            //Si es el primero
            //Seteamos el index al ultimo
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //Cogemos el ultimo de la lista
            activeAudio = audioList.get(--audioIndex);
        }

        //Actualizamos el index en el XML
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reseteamos el MP
        mediaPlayer.reset();
        initMediaPlayer();
        buildNotification(PlaybackStatus.PLAYING);
    }


    /**
     * ACTION_AUDIO_BECOMING_NOISY -- cambiamos el output del audio
     */
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pausamos on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };


    private void registerBecomingNoisyReceiver() {
        //registramos despues de ganar el foco
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    /**
     * Manejo del estado del telefono
     */
    private void callStateListener() {
        // Get manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Instanciamos el listener
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //Si una llamada existe
                    //pausamos el mp
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        //Se va la llamada, continuamos
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Registramos el telefono con el manager
        // Listener a la espera de cambios
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * MediaSession y acciones de Notificacion
     */
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager existe

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // instanciamos uno nuevo
        mediaSession = new MediaSession(getApplicationContext(), "AudioPlayer");
        //cogemos los controles
        transportControls = mediaSession.getController().getTransportControls();

        mediaSession.setActive(true);
        //Indicamos que tenga los controles de transport
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Seteamos el metadata
        updateMetaData();

        // Asignamos el callback al mediasession
        mediaSession.setCallback(new MediaSession.Callback() {
            // Implementamos el  callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Paramos el servicio
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }


    /**
     * Actualizamos el metadata
     */
    private void updateMetaData() {

        try {
            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, getAlbumart(Long.parseLong(activeAudio.getCaratula())))
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, activeAudio.getArtist())
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                    .putString(MediaMetadata.METADATA_KEY_TITLE, activeAudio.getTitle())
                    .build());
        }catch(Exception ex){}
    }


    /**
     * Actualizamos el metadata
     * @param playbackStatus
     */
    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//Necesita ser instanciado
        PendingIntent play_pauseAction = null;
        initChannels(getApplicationContext());

        //Construimos una nueva notificacion
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //Accion de pausa
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //Accion de reproducir
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = getAlbumart(Long.parseLong(activeAudio.getCaratula()));
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("musica", "gotoMusica");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent,PendingIntent.FLAG_UPDATE_CURRENT);


        Bundle bundle = new Bundle();
        bundle.putString("titulo", activeAudio.getTitle());
        bundle.putString("artista", activeAudio.getArtist());
        bundle.putString("album", activeAudio.getAlbum());
        bundle.putString("data", activeAudio.getData());
        bundle.putBoolean("shuffle",MainActivity.mode);


        // Creamos la notificacion
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext(),"default").setOngoing(true)
                // Escondemos el tiempo de vida
                .setShowWhen(false)
                // Seteamos el estilo
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // Asignamos nuestro token de la sesion
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken()))
                        // Mostramos los controles
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.eurogamer))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(String.format("%s | %s", activeAudio.getArtist(), activeAudio.getAlbum()))
                .setContentTitle(activeAudio.getTitle())
                .setContentInfo(activeAudio.getData())
                .setContentIntent(pendingIntent)
                // Agregamos las acciones de reproduccion
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                .addExtras(bundle);


        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Metodo para extraer dado un ID, el bitmap
     * @param album_id id de la caratula
     * @return Bitmap de la caratula
     */
    public Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
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
     * Acciones dependiendo del codigo
     * @param actionNumber codigo de accion
     * @return Intent de accion
     */
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    /**
     * Removemos la notificacion
     */
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        stopSelf();
    }

    /**
     * Manejamos las acciones siguientes
     * @param playbackAction intent accion
     */
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


    /**
     * Reproducimos nuevo audio
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Cogemos el index de nuestro XML
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();

            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //Comprobamos que este dentro del rango
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //Accion PLAY_NEW_AUDIO recibida
            //Reseteamos el mediaplayer
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    /**
     * Registramos un nuevo audio
     */
    private void register_playNewAudio() {
        //Registramos recibidor de PlayNewAudio
        IntentFilter filter = new IntentFilter(MusicFragment.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);


    }


    /**
     * Iniciamos los canales de notificacion
     * @param context
     */
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }
}
