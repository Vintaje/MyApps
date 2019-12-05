package com.emiliorgvintaje.myapps.ui.videoplayer.playing;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.emiliorgvintaje.myapps.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;


/**
 * Vista para reproducir videos de Youtube
 */
public class YoutubeFragment extends Fragment {
    private View root;
    private YouTubePlayerView youTubePlayerView;
    private String videourl;

    /**
     * Constructor en el que tratamos las posibles URLs que se pueden obtener al compartir un enlace
     * @param videourl url del video
     */
    public YoutubeFragment(String videourl) {
        if(videourl.contains("youtu.be/")){
            try {
                this.videourl = videourl.substring(videourl.indexOf("be/")+3, videourl.indexOf("?"));
            }catch(Exception ex){
                this.videourl = videourl.substring(videourl.indexOf("be/")+3);
            }
        }else if(videourl.contains("?v=")){
            try {
                this.videourl = videourl.substring(videourl.indexOf("?v=")+3, videourl.indexOf("?"));
            }catch(Exception ex){
                this.videourl = videourl.substring(videourl.indexOf("?v=")+3);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.youtube_fragment, container, false);
        return root;
    }

    /**
     * Instanciamos el YoutubePlayerView y le agregamos el Listener para que, cuando este preparado
     * inicie la reproduccion
     * @param savedInstanceState instancia salvada
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        youTubePlayerView = root.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videourl, 0);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
        onDestroy();
    }

    /**
     * Desvinculamos el playerView
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }

}
