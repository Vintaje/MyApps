package com.emiliorgvintaje.myapps.ui.maps.Ruta.Lista;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.musicplayer.MusicAdapter;
import com.emiliorgvintaje.myapps.util.GPX.GPXUtil;

import java.io.File;
import java.util.ArrayList;

public class RutaAdapter extends RecyclerView.Adapter<RutaAdapter.ViewHolder>{
    private ArrayList<File> list;


    public RutaAdapter(ArrayList<File> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.ruta_item_rv, parent, false);


        return new RutaAdapter.ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File loc = list.get(position);

        holder.titulo.setText(GPXUtil.readNombre(loc));

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView titulo;
        private LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.tvTituloRuta);

            layout = itemView.findViewById(R.id.layoutRuta);

        }
    }
}
