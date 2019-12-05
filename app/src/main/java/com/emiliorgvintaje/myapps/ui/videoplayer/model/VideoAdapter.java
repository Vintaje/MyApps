package com.emiliorgvintaje.myapps.ui.videoplayer.model;


import android.content.Context;

import android.content.Intent;

import android.net.Uri;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.videoplayer.VideoPlayerFragment;
import com.emiliorgvintaje.myapps.ui.videoplayer.playing.PlayingFragment;
import com.emiliorgvintaje.myapps.util.MyFiles;

import java.io.File;
import java.util.ArrayList;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>{

    private ArrayList<Video> videosList;
    private Context context;
    private FragmentManager fragmentManager;
    final private VideoPlayerFragment videoPlayerFragment;

    public VideoAdapter(ArrayList<Video> videosList, Context context, FragmentManager fragmentManager, VideoPlayerFragment videoPlayerFragment) {
        this.videosList = videosList;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.videoPlayerFragment = videoPlayerFragment;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.video_itemrv, parent, false);
        VideoAdapter.ViewHolder viewHolder = new VideoAdapter.ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Video video = videosList.get(position);
        final ArrayList<String> playlist = new ArrayList<>();
        for (int i = 0; i < videosList.size();i++){
            playlist.add(videosList.get(i).getPath());

        }

        holder.titulo.setText(video.getTitulo());
        holder.duracion.setText(video.getDuracion());

        if(video.getHeight() == null || video.getWidth() == null){
            holder.resolucion.setText("No disponible");
            holder.resolucion.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

        }else {
            holder.resolucion.setText(String.format("%sx%s", video.getWidth(), video.getHeight()));
            holder.resolucion.setTextColor(context.getResources().getColor(android.R.color.background_dark));
        }



        holder.imagen.setImageBitmap(video.getThumb());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!videoPlayerFragment.playing) {
                    ((MainActivity)videoPlayerFragment.getActivity()).getSupportActionBar().hide();

                    videoPlayerFragment.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    videoPlayerFragment.getActivity(). getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    PlayingFragment playingFragment = new PlayingFragment(video.getPath(), videoPlayerFragment);
                    videoPlayerFragment.playing = true;
                    fragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment, playingFragment).addToBackStack(null).commit();
                }
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.more);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.song_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.compartir:
                                //Realizamos un Intent pasado extras relacionados al asunto y al cuerpo del mensaje
                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("video/*");
                                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.getPath()));
                                context.startActivity(Intent.createChooser(sharingIntent, "Compartido desde MyAPPs de Emilio Rubiales"));

                                break;
                            case R.id.borrar:
                                MyFiles.delete(context,new File(video.path));

                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }







    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView titulo, duracion, resolucion;
        private LinearLayout linearLayout;
        private ImageView imagen;

        private ImageButton more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.titulo = (TextView) itemView.findViewById(R.id.tvVideoTitulo);
            this.duracion = (TextView) itemView.findViewById(R.id.tvVideoDuration);
            this.resolucion = (TextView) itemView.findViewById(R.id.tvVideoRes);
            this.imagen = (ImageView) itemView.findViewById(R.id.ivThumbVideo);
            this.more = (ImageButton) itemView.findViewById(R.id.ibMoreVideo);

            this.linearLayout = (LinearLayout) itemView.findViewById(R.id.linearVideoItem);
        }
    }
}
