package com.emiliorgvintaje.myapps.ui.videoplayer.playing;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.videoplayer.VideoPlayerFragment;

/**
 * Fragment de la reproduccion del video actual
 */
public class PlayingFragment extends Fragment {
    private VideoView videoView;
    private String path;
    private VideoPlayerFragment videoPlayerFragment;
    private View root;

    /**
     * Constructor
     * @param path ruta del video
     * @param videoPlayerFragment fragment de la playlist
     */
    public PlayingFragment(String path, VideoPlayerFragment videoPlayerFragment) {
        this.path = path;
        this.videoPlayerFragment = videoPlayerFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.playing_fragmentvideo, container, false);

        return root;
    }

    /**
     * Instanciamos el videoview, lo inicializamos y le seteamos los listener por si cambia
     * de tamaño o completa el propio video
     * @param savedInstanceState instancia salvada
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // TODO: Use the ViewModel
        videoView = (VideoView) root.findViewById(R.id.vvVideo);
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                ((MainActivity)getActivity()).onBackPressed();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        MediaController mediaController = new MediaController(getContext());
                        videoView.setMediaController(mediaController);
                        mediaController.setAnchorView(videoView);
                        if(mp.getVideoWidth() > mp.getVideoHeight()){
                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                    }
                });
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });

    }


    /**
     * Volvemos a la vista en Portrait y en ventana
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        videoPlayerFragment.playing = false;
        ((MainActivity)getActivity()).getSupportActionBar().show();
        getActivity(). getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }
}
