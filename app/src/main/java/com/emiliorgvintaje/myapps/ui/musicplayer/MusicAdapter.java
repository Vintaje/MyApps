package com.emiliorgvintaje.myapps.ui.musicplayer;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.Audio;
import com.emiliorgvintaje.myapps.ui.musicplayer.Services.MediaPlayerService;
import com.emiliorgvintaje.myapps.util.MyFiles;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private ArrayList<Audio> musiclist;
    private MusicFragment musicFragment;
    private MediaPlayerService player;
    private FragmentManager fragmentManager;


    public MusicAdapter(ArrayList<Audio> musiclist, MusicFragment musicFragment, MediaPlayerService player, FragmentManager fragmentManager) {
        this.musiclist = musiclist;
        this.musicFragment = musicFragment;
        this.player = player;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.music_item, parent, false);
        MusicAdapter.ViewHolder viewHolder = new MusicAdapter.ViewHolder(listItem);


        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Audio musica = musiclist.get(position);

        if (musica.getTitle().length() > 35) {
            holder.titulo.setText(musica.getTitle().substring(0, 35).toUpperCase());
        } else {

            holder.titulo.setText(musica.getTitle().toUpperCase());
        }
        holder.imageView.setImageBitmap(musicFragment.getAlbumart(Long.parseLong(musica.getCaratula())));
        holder.artistalbum.setText(String.format("%s | %s", musica.getArtist(), musica.getAlbum()).toUpperCase());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                musicFragment.playAudio(position);

            }
        });


        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(musicFragment.getContext(), holder.more);
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
                                sharingIntent.setType("audio/*");
                                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musica.getData()));
                                musicFragment.startActivity(Intent.createChooser(sharingIntent, "Compartido desde MyAPPs de Emilio Rubiales"));
                                break;
                            case R.id.borrar:

                                    alertDialog(musica.getData());
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

    }

    private void alertDialog(final String pathBorrado) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(musicFragment.getContext());
        dialog.setMessage("Accion irreversible: ¿Quiere borrar la cancion?");
        dialog.setTitle("Alerta");

        System.out.println(pathBorrado);
        dialog.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Toast.makeText(musicFragment.getContext(), "Elemento borrado", Toast.LENGTH_LONG).show();

                        MyFiles.delete(musicFragment.getContext(), new File(pathBorrado));
                        musicFragment.loadList();

                    }
                });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(musicFragment.getContext(), "Borrado Cancelado", Toast.LENGTH_SHORT);
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    /**
     * Borrar item del Recycler
     *
     * @param position Integer
     */
    public void removeItem(int position) {
        musiclist.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, musiclist.size());


    }

    /**
     * Recuperamos un Item borrado previamente
     *
     * @param item     Noticia
     * @param position Posicion
     */
    public void restoreItem(Audio item, int position) {
        //listdata.set(position, item);
        musiclist.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, musiclist.size());
    }


    /**
     * IMPORTANTE A MAS NO PODER, SIN EL getItemCount(){return tulist.size();} NO FUNCIONARA EL RECYCLER!
     *
     * @return numero de Items
     */
    @Override
    public int getItemCount() {
        return musiclist.size();
    }

    /**
     * ViewHolder del Recycler
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton more;
        public TextView titulo;
        public TextView artistalbum;
        private ImageView imageView;

        public LinearLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.more = (ImageButton) itemView.findViewById(R.id.ibMasOpciones);

            this.titulo = (TextView) itemView.findViewById(R.id.tvMusicTitulo);
            this.artistalbum = (TextView) itemView.findViewById(R.id.tvArtistaAlbum);

            this.relativeLayout = (LinearLayout) itemView.findViewById(R.id.rlMusicMainLayout);
            this.imageView = (ImageView) itemView.findViewById(R.id.ivMusica);
        }
    }


}
