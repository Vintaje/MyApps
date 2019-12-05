package com.emiliorgvintaje.myapps.ui.videoplayer;



import android.app.AlertDialog;
import android.content.ContentResolver;

import android.content.Context;
import android.content.DialogInterface;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.videoplayer.model.Video;
import com.emiliorgvintaje.myapps.ui.videoplayer.model.VideoAdapter;

import com.emiliorgvintaje.myapps.ui.videoplayer.playing.YoutubeFragment;
import com.emiliorgvintaje.myapps.util.Times;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


/**
 * Fragment de la lista de videos disponibles
 */
public class VideoPlayerFragment extends Fragment {
    private ArrayList<Video> videosList;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private View root;
    private FloatingActionButton ytVideo;

    public boolean playing;


    /**
     * Cambiamos la ventana a pantalla completa, landscape
     * @param inflater LayoutInflater
     * @param container Grupo de Vistas
     * @param savedInstanceState Instancia salvada
     * @return Vista actual
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Portrait
        ((MainActivity)getActivity()).setDrawerLocked(false);
        getActivity(). getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//Limpiamos FullScreen
        ((MainActivity)getActivity()).getSupportActionBar().show();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//Modo Ventana
        root = inflater.inflate(R.layout.video_player_fragment, container, false);
        playing = false;
        return root;


    }


    /**
     * Instanciamos el recycler View, el dialogo del boton para reproducir un video de youtube dada
     * su URL
     * @param savedInstanceState Instancia salvada
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = (RecyclerView) root.findViewById(R.id.rvVideosLista);
        ytVideo = (FloatingActionButton) root.findViewById(R.id.fabYoutubeVideo);

        ytVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });

        //Tarea que recoge todos los videos
        new videoLoader().execute();
    }


    /**
     * Metodo que genera un alert dialog para introducir una URL de youtube, incluye los listener
     * para que si, es correcta y confirma el usuario, le lleva a la vista de reproductor
     */
    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Introduce la URL del Video a Reproducir");
        dialog.setTitle("Alerta");
        // Seteamos el input
        final EditText input = new EditText(getContext());
        // Especificamos el tipo de input
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(input);

        dialog.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        String path = input.getText().toString().trim();

                        if(path.isEmpty()){

                            Toast.makeText(getContext(),"URL incorrecta", Toast.LENGTH_SHORT).show();
                        }else {
                            getActivity(). getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            ((MainActivity)getActivity()).setDrawerLocked(true);
                            ((MainActivity)getActivity()).getSupportActionBar().hide();
                            YoutubeFragment youtubeFragment = new YoutubeFragment(input.getText().toString());
                            getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.nav_host_fragment, youtubeFragment).commit();
                        }
                    }
                });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(),"Reproduccion Cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    /**
     * Hacemos un fetch de todos los videos del dispositivos
     */
    public void fetchVideos(){
        videosList = new ArrayList<>();
        Uri uri;
        Cursor cursor;
        int column_index_data, thumb;
        String absolutePathImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT};

        String orderBy = MediaStore.Images.Media.DATE_TAKEN;

        cursor = getContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        while(cursor.moveToNext()){
            absolutePathImage = cursor.getString(column_index_data);
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
            String height = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
            String width = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
            String duracion =  cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));




            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 1;
            ContentResolver crThumb = getContext().getContentResolver();
            Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);

            Video video = new Video();
            video.setPath(absolutePathImage);
            video.setTitulo(title);
            video.setHeight(height);
            video.setWidth(width);
            video.setThumb(curThumb);
            video.setDuracion(Times.milliSecondsToTimer(Long.parseLong(duracion)));

            videosList.add(video);

        }



    }


    /**
     * Iniciamos el recycler View
     */
    public void initRecycler(){
        adapter = new VideoAdapter(videosList, getContext(), getFragmentManager(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        runLayoutAnimation(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }


    /**
     * Ejecutamos la animacion de carga de items en el recycler
     * @param recyclerView recycler View
     */
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);

        recyclerView.scheduleLayoutAnimation();
    }


    /**
     * AsynTask que usando el metodo de fetch de todos los videos en background, los muestra en pantalla
     *
     */
    public class videoLoader extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... voids) {
            fetchVideos();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            initRecycler();
        }
    }




}
