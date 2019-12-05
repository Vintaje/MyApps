package com.emiliorgvintaje.myapps.ui.rssfolder;

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

import com.emiliorgvintaje.myapps.ui.rssfolder.noticiaDetalle.NoticiaDetalleFragment;
import com.squareup.picasso.Picasso;
import com.emiliorgvintaje.myapps.R;

import java.util.ArrayList;

/**
 * Adaptador para el Recycler
 *
 * En el constructor le pasamos un FragmentManager para poder ir a otro Fragment
 * Tambien le pasamos nuestro Array de Noticias
 */
public class NoticiaAdaptador extends RecyclerView.Adapter<NoticiaAdaptador.ViewHolder>{
    private ArrayList<Noticia> noticialist;
    public FragmentManager mFragmentManager;

    public NoticiaAdaptador(ArrayList<Noticia> noticialist, FragmentManager fragmentManager) {
        this.noticialist = noticialist;
        this.mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public NoticiaAdaptador.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.noticia,parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Noticia noticia = noticialist.get(position);

        holder.titulo.setText(noticia.getTitulo());
        holder.fecha.setText(noticia.getFecha());
        Picasso.get().load(noticia.getImagen())
                .error(R.mipmap.ic_no_image_round).error(R.mipmap.ic_no_image_round).fit().centerCrop().into(holder.imagen);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Cuando hacemos click en cada item, nos envia al Fragment de mas Detalles de Noticia
                Fragment noticiaDetalle = new NoticiaDetalleFragment(noticia);

                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fragment_effect,R.anim.fragment_effect_exit,R.anim.fragment_effect,R.anim.fragment_effect_exit);
                fragmentTransaction.replace(R.id.nav_host_fragment,noticiaDetalle);
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
        noticialist.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, noticialist.size());


    }

    /**
     * Recuperamos un Item borrado previamente
     * @param item Noticia
     * @param position Posicion
     */
    public void restoreItem(Noticia item, int position) {
        //listdata.set(position, item);
        noticialist.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, noticialist.size());
    }


    @Override
    public int getItemCount() {
        return noticialist.size();
    }

    /**
     * ViewHolder del Recycler
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imagen;
        public TextView titulo;
        public TextView fecha;


        public RelativeLayout relativeLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Inicializamos los objetos del layout de cada Item
            this.imagen = (ImageView) itemView.findViewById(R.id.ivNoticia);
            this.titulo = (TextView) itemView.findViewById(R.id.tvNoticiaTitulo);
            this.fecha = (TextView) itemView.findViewById(R.id.tvNoticiaFecha);

            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.noticiaLayout);

        }
    }
}
