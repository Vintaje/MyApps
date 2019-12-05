package com.emiliorgvintaje.myapps.ui.juegos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.emiliorgvintaje.myapps.util.MyB64;
import com.emiliorgvintaje.myapps.ui.juegos.detalles.JuegosDetalleFragment;
import com.emiliorgvintaje.myapps.R;

import java.util.ArrayList;

/**
 * Adaptador para el Recycler
 *
 * En el constructor le pasamos un FragmentManager para poder ir a otro Fragment
 * Tambien le pasamos nuestro Array de Noticias
 */
public class JuegoAdaptador extends RecyclerView.Adapter<JuegoAdaptador.ViewHolder>{
    private ArrayList<Juego> juegoslist;
    public FragmentManager mFragmentManager;

    public JuegoAdaptador(ArrayList<Juego> juegoslist, FragmentManager fragmentManager) {
        this.juegoslist = juegoslist;
        this.mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public JuegoAdaptador.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.card_juegos,parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Juego juego = juegoslist.get(position);

        holder.titulo.setText(juego.getNombre());
        holder.precio.setText(String.format("Precio: %s€", juego.getPrecio()));
        holder.plataforma.setText(juego.getPlataforma());
        holder.imagen.setImageBitmap(MyB64.base64ToBitmap(juego.getImagen()));
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Cuando hacemos click en cada item, nos envia al Fragment de mas Detalles de Noticia
                Fragment JuegoDetalle = new JuegosDetalleFragment(juego,false,false);

                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fragment_effect,R.anim.fragment_effect_exit,
                        R.anim.fragment_effect,R.anim.fragment_effect_exit);
                fragmentTransaction.replace(R.id.nav_host_fragment,JuegoDetalle);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
            }
        });
    }


    /**
     * Borrar item del Recycler
     * @param position Integer
     */
    public void removeItem(int position) {
        juegoslist.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, juegoslist.size());


    }

    /**
     * Recuperamos un Item borrado previamente
     * @param item Noticia
     * @param position Posicion
     */
    public void restoreItem(Juego item, int position) {
        //listdata.set(position, item);
        juegoslist.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, juegoslist.size());
    }


    /**
     * IMPORTANTE A MAS NO PODER, SIN EL getItemCount(){return tulist.size();} NO FUNCIONARA EL RECYCLER!
     *
     * @return numero de Items
     */
    @Override
    public int getItemCount() {
        return juegoslist.size();
    }

    /**
     * ViewHolder del Recycler
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imagen;
        public TextView titulo;
        public TextView precio;
        public TextView plataforma;


        public RelativeLayout relativeLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Inicializamos los objetos del layout de cada Item
            this.imagen = (ImageView) itemView.findViewById(R.id.ivImagenJuego);
            this.titulo = (TextView) itemView.findViewById(R.id.tvTituloJuego);
            this.precio = (TextView) itemView.findViewById(R.id.tvPrecioJuego);
            this.plataforma = (TextView) itemView.findViewById(R.id.tvPlataformaJuegos);

            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rlJuegos);

        }
    }
}
