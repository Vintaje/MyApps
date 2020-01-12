package com.emiliorgvintaje.myapps.ui.maps.Ruta.Lista;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.maps.MapasFragment;
import com.emiliorgvintaje.myapps.ui.maps.Ruta.RutasFragment;
import com.emiliorgvintaje.myapps.ui.musicplayer.MusicAdapter;
import com.emiliorgvintaje.myapps.util.GPX.GPXUtil;
import com.emiliorgvintaje.myapps.util.MyFiles;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;

public class RutaAdapter extends RecyclerView.Adapter<RutaAdapter.ViewHolder>{
    private ArrayList<File> list;
    private RutasFragment rutasFragment;
    private MapasFragment mapasFragment;


    public RutaAdapter(ArrayList<File> list, RutasFragment rutasFragment, MapasFragment mapasFragment){
        this.list = list;
        this.rutasFragment = rutasFragment;
        this.mapasFragment = mapasFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.ruta_item_rv, parent, false);


        return new RutaAdapter.ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final File loc = list.get(position);

        holder.titulo.setText(GPXUtil.readNombre(loc));
        holder.borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(loc);
            }
        });
        holder.titulo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File ruta = list.get(position);
                ArrayList<LatLng> puntos = GPXUtil.read(rutasFragment.getContext(), ruta);

                mapasFragment.cargarRuta(puntos);
                ((MainActivity)rutasFragment.getActivity()).onBackPressed();

            }
        });
    }

    private void alertDialog(final File loc) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(rutasFragment.getContext());
        dialog.setMessage("Quieres borrar el fichero de la ruta?");
        dialog.setTitle("Borrado...");
        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MyFiles.borrarFichero(loc.getAbsolutePath());
                Toast.makeText(rutasFragment.getContext(),"Ruta borrada", Toast.LENGTH_SHORT).show();
                rutasFragment.load();
            }
        });
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(rutasFragment.getContext(),"Borrado Cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView titulo;
        private ImageButton borrar;
        private CardView layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.tvTituloRuta);
            borrar = itemView.findViewById(R.id.borrarRuta);
        }
    }
}
