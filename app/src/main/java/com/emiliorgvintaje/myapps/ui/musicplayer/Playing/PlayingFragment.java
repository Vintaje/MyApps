package com.emiliorgvintaje.myapps.ui.musicplayer.Playing;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.MusicFragment;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.Audio;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.StorageUtil;
import com.emiliorgvintaje.myapps.util.Times;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;

public class PlayingFragment extends Fragment {

    private ViewPager viewPager;
    private View root;
    private ArrayList<Audio> audioList;
    private int audioIndex;
    private Runnable runnable;
    private Handler handler;
    private MusicFragment musicFragment;
    private TextView titulo, artalbum, currentS, totalS;
    private SeekBar barraProg;
    private RelativeLayout relativeLayout;
    private ImageButton play, next, previous;
    private BarVisualizer visualizer;

    /**
     * Instancia del Fragment de Reproduccion
     * @param audioList Lista de reproduccion actual
     * @param audioIndex Index de la cancion actual
     * @param musicFragment Fragment anterior de musica
     */
    public PlayingFragment(ArrayList<Audio> audioList, int audioIndex, MusicFragment musicFragment) {

        this.audioList = audioList;
        this.audioIndex = audioIndex;
        this.musicFragment = musicFragment;
    }


    /**
     * Para el onResume, volvemos a mostrar la imagen de la cancion actual
     */
    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(audioIndex);
    }


    /**
     *
     * @param inflater LayoutInflater
     * @param container Grupo de vistas
     * @param savedInstanceState Instancia guardada
     * @return Vista actual
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.playing_fragment, container, false);

        viewPager = (ViewPager) root.findViewById(R.id.vpActualSong);
        titulo = (TextView) root.findViewById(R.id.tvTituloSong);
        artalbum = (TextView) root.findViewById(R.id.tvArtAlbumSong);
        if (audioIndex < 0) {
            audioIndex = 0;
        }
        titulo.setText(audioList.get(audioIndex).getTitle());
        barraProg = (SeekBar) root.findViewById(R.id.seekBarSong);
        relativeLayout = (RelativeLayout) root.findViewById(R.id.relActualSong);
        handler = new Handler();
        handleSeekbar();
        visualizer = root.findViewById(R.id.circle);
        play = (ImageButton) root.findViewById(R.id.ibPlayStopSong);
        next = (ImageButton) root.findViewById(R.id.ibNextSong);
        previous = (ImageButton) root.findViewById(R.id.ibPrevSong);

        currentS = (TextView) root.findViewById(R.id.tvCurrentSong);
        totalS = (TextView) root.findViewById(R.id.tvTotalSong);
        return root;
    }

    /**
     * Cargamos todas las acciones que repercuten en la UI
     * @param view Vista actual
     * @param savedInstanceState Instancia salvada
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Use the ViewModel
        loadSlider();


        seekBarUpdate();
        StorageUtil storageUtil = new StorageUtil(getContext());
        audioIndex = storageUtil.loadAudioIndex();
        viewPager.setCurrentItem(audioIndex, false);
        isPlayingSong();
        changeSong();


    }


    /**
     * Hilo que trabaja en el Hilo de la Interfaz para manejar la barra de progreso
     */
    public void seekBarUpdate() {
        new Thread() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(runnable = new Runnable() {
                        @Override
                        public void run() {

                            if (((MainActivity)getActivity()).player.getMediaPlayer() != null) {
                                try {
                                    barraProg.setMax(((MainActivity)getActivity()).player.getMediaPlayer().getDuration() / 1000);
                                    int mCurrentPosition = ((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition() / 1000;
                                    barraProg.setProgress(mCurrentPosition);
                                    currentS.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition()));
                                    totalS.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getDuration()));
                                } catch (Exception ex) {
                                }
                            }
                        }
                    });
                    //Timeamos la accion con un handler que publica cada 1 segundo el hilo
                    handler.postDelayed(this, 1000);
                } catch (Exception ex) {
                }
            }


        }.start();
    }

    /**
     * Clase FadePageTransforme
     * Creamos una pequeña animacion para el propio ViewPager
     */
    public class FadePageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            view.setTranslationX(view.getWidth() * -position);
            if (position <= -1.0F || position >= 1.0F) {
                view.setAlpha(0.0F);
            } else if (position == 0.0F) {
                view.setAlpha(1.0F);
            } else {
                // La posicion esta dentro de -1.0F & 0.0F OR 0.0F & 1.0F
                view.setAlpha(1.0F - Math.abs(position));
            }
        }

    }

    /**
     * Instanciamos un hilo que trabaja en el Hilo de la Interfaz
     * para manejar el cambio del ViewPager cuando cambia la propia cancion actual
     */
    public void loadSlider() {
        new Thread() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(runnable = new Runnable() {
                        @Override
                        public void run() {
                            ViewSongAdapter viewSongAdapter = new ViewSongAdapter(getContext(), audioList, musicFragment);
                            viewPager.setAdapter(viewSongAdapter);
                            viewPager.setCurrentItem(audioIndex, false);
                            viewPager.setScrollBarFadeDuration(500);
                            viewPager.setOffscreenPageLimit(3);
                            viewPager.setPageTransformer(false, new FadePageTransformer());
                            if (audioIndex < 0) {
                                audioIndex = 0;
                            }
                            artalbum.setText(String.format("%s | %s", audioList.get(audioIndex).getArtist(), audioList.get(audioIndex).getAlbum()));
                            //Listener del viewPager para el cambio de cancion al hacer un drag
                            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                }

                                @Override
                                public void onPageSelected(int position) {
                                    musicFragment.playAudio(position, false);
                                    titulo.setText(audioList.get(position).getTitle());
                                    artalbum.setText(String.format("%s | %s", audioList.get(position).getArtist(), audioList.get(position).getAlbum()));
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });
                        try {
                            int audioSessionId = ((MainActivity)getActivity()).player.getMediaPlayer().getAudioSessionId();
                            if (audioSessionId != -1)
                                visualizer.setAudioSessionId(audioSessionId);
                        }catch(Exception ex){}
                        }
                    });
                    barraProg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.whitebox), PorterDuff.Mode.SRC_ATOP);

                } catch (Exception ex) {
                }
            }


        }.start();


    }


    /**
     * Instanciamos un Listener para la SeekBar que, si el usuario cambia su posicion, la posicion
     * actual de reproduccion de la cancion cambia
     */
    private void handleSeekbar() {
        barraProg.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (((MainActivity)getActivity()).player.getMediaPlayer() != null && fromUser) {
                    ((MainActivity)getActivity()).player.getMediaPlayer().seekTo(progress * 1000);
                    currentS.setText(Times.milliSecondsToTimer(((MainActivity)getActivity()).player.getMediaPlayer().getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    /**
     * Listener de los botones de pausa, skipToNext y skipToPrevious
     * maneja, a traves del fragment anterior que mantiene el servicio
     * en activo, el cual llamamos para cambiar la cancion
     */
    public void changeSong() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    ((MainActivity)getActivity()).player.skipToNext();
                    StorageUtil storageUtil = new StorageUtil(getContext());
                    audioIndex = storageUtil.loadAudioIndex();
                    viewPager.setCurrentItem(audioIndex, false);

                } catch (Exception ex) {
                    Toast.makeText(getContext(), "Ultima Cancion", Toast.LENGTH_SHORT);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    ((MainActivity)getActivity()).player.skipToPrevious();
                    StorageUtil storageUtil = new StorageUtil(getContext());
                    audioIndex = storageUtil.loadAudioIndex();
                    viewPager.setCurrentItem(audioIndex, false);
                } catch (Exception ex) {

                    Toast.makeText(getContext(), "Primera Cancion", Toast.LENGTH_SHORT);
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (((MainActivity)getActivity()).player.getMediaPlayer().isPlaying()) {
                        ((MainActivity)getActivity()).player.pauseMedia();
                    } else {
                        ((MainActivity)getActivity()).player.resumeMedia();
                    }
                }catch(Exception ex){

                    ((MainActivity)getActivity()).player.getMediaPlayer().reset();
                }
            }
        });
    }

    /**
     * Hilo que trabaja en el Hilo de la Interfaz para Postear en tiempo real la cancion exacta que esta
     * sonando
     */
    public void isPlayingSong() {

        new Thread() {
            @Override
            public void run() {

                try {
                    getActivity().runOnUiThread(runnable = new Runnable() {
                        @Override
                        public void run() {

                            if (((MainActivity)getActivity()).player.getMediaPlayer() != null) {
                                if (((MainActivity)getActivity()).player.getMediaPlayer().isPlaying()) {

                                    play.setImageResource(R.drawable.bt_stop_large);
                                } else {
                                    play.setImageResource(R.drawable.bt_play_large);
                                }
                            }

                            StorageUtil storageUtil = new StorageUtil(getContext());
                            audioIndex = storageUtil.loadAudioIndex();
                            if (viewPager.getCurrentItem() != audioIndex) {
                                viewPager.setCurrentItem(audioIndex, false);
                            }
                            titulo.setSelected(true);

                        }
                    });
                    handler.postDelayed(this, 200);
                } catch (Exception ex) {
                }

            }


        }.start();
    }

    /**
     * Realizamos un onBackPressed para volver al fragment de las canciones
     */
    @Override
    public void onPause() {
        super.onPause();

    }


    /**
     * Removemos todos los hilos que trabajan en nuestro Handler y desvinculamos nuestro
     * Audio Visualizer
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = new Handler();
        try {
            if (visualizer != null) {
                visualizer.release();
            }
        }catch(Exception ex){}
        handler.removeCallbacksAndMessages(runnable);
        musicFragment.setPlaying(false);

    }
}
