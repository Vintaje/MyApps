package com.emiliorgvintaje.myapps.ui.rssfolder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.rssfolder.noticiaDetalle.NoticiaDetalleFragment;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/***
 *
 * Del Grupo MyClass
 *
 */


public class rssFragment extends Fragment {



    private RecyclerView recyclerView;
    private NoticiaAdaptador adaptador;
    private ArrayList<Noticia> noticias;
    private String path = "https://eurogamer.es/?format=rss";
    private View root;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Paint p = new Paint();
    private Button btRefrescar;
    private Fragment noticiaDetalle;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.frament_recycler, container, false);

        iniciarSwipeRecargar();
        recyclerView = (RecyclerView) root.findViewById(R.id.rvNoticias);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        swipeRefreshLayout.setRefreshing(true);
        btRefrescar = (Button) root.findViewById(R.id.btRefresh);
        btRefrescar.setVisibility(View.INVISIBLE);
        iniciarSwipeHorizontal();

        int resId = R.anim.layout_animation;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(root.getContext(), resId);
        recyclerView.setLayoutAnimation(animation);
        ((MainActivity) getActivity()).setDrawerLocked(false);
        btRefrescar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                new dataLoader().execute();

            }

        });

        if(!isNetworkAvailable() && noticias == null){
            btRefrescar.setVisibility(View.VISIBLE);

        }

        return root;
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);
        adaptador.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void iniciarSwipeRecargar() {


        swipeRefreshLayout = (SwipeRefreshLayout) root.getRootView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setColorSchemeResources(R.color.eurogamer);
                swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.whitebox);


                new dataLoader().execute();

            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new dataLoader().execute();


        noticiaDetalle = new NoticiaDetalleFragment();
        ((MainActivity)getActivity()).hideShare();
        ((MainActivity)getActivity()).hideFloatingActionButton();
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((MainActivity)getActivity()).setDrawerLocked(false);
    }


     private void iniciarSwipeHorizontal() {
        //https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e

         ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

             @Override
             public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                 return false;
             }


             // Evento al mover
             @Override
             public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                 int position = viewHolder.getAdapterPosition();

                 // Voy a hacer lo mismo en las dos
                 // Si nos movemos a la izquierda
                 if (direction == ItemTouchHelper.LEFT) {
                     borrarElemento(position);

                 } else {
                     Noticia detalles = noticias.get(position);

                     noticiaDetalle = new NoticiaDetalleFragment(detalles);

                     FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                     fragmentTransaction.setCustomAnimations(R.anim.fragment_effect,R.anim.fragment_effect_exit,R.anim.fragment_effect,R.anim.fragment_effect_exit);
                     fragmentTransaction.replace(R.id.nav_host_fragment,noticiaDetalle);
                     fragmentTransaction.addToBackStack(null);

                     fragmentTransaction.commit();
                 }
             }




             // Dibujamos los botones y evenetos
             @Override
             public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                 Bitmap icon;
                 if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                     View itemView = viewHolder.itemView;
                     float height = (float) itemView.getBottom() - (float) itemView.getTop();
                     float width = height / 3;
                     // Si es dirección a la derecha: izquierda->derecta
                     if (dX > 0) {
                         p.setColor(getResources().getColor(R.color.eurogamer));
                         RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                         c.drawRect(background, p);
                         icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_letsgo);
                         RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                         c.drawBitmap(icon, null, icon_dest, p);

                         // Caso contrario
                     } else {
                         p.setColor(getResources().getColor(R.color.leftswiper));
                         RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                         c.drawRect(background, p);
                         icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                         RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                         c.drawBitmap(icon, null, icon_dest, p);
                     }
                 }
                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
             }
         };
         ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
         itemTouchHelper.attachToRecyclerView(recyclerView);

    }



    private void borrarElemento(int position) {
        final Noticia deletedModel = noticias.get(position);
        final int deletedPosition = position;
        adaptador.removeItem(position);
        // Mostramos la barra
        Snackbar snackbar = Snackbar.make(getView(), " eliminado de la lista!", Snackbar.LENGTH_LONG);
        snackbar.setAction("DESHACER", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // undo is selected, restore the deleted item
                   adaptador.restoreItem(deletedModel, deletedPosition);
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) root.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void btRefrescarVisible(){
        btRefrescar.setVisibility(View.VISIBLE);
    }

    public class dataLoader extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... strings) {

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = null;
                try {
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
                Document document = null;
                try {
                    document = builder.parse(path);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

                ArrayList<Noticia> newsletter = new ArrayList<>();

                NodeList lItems = document.getElementsByTagName("item");

                for (int i = 0; i < 20; i++) {
                    Node node = lItems.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) node;

                        String img = element.getElementsByTagName("description").item(0).getTextContent();
                        img = img.substring(img.indexOf("<img src=\"") + 10, img.indexOf("\" alt=\"\"/>"));

                        newsletter.add(new Noticia(element.getElementsByTagName("title").item(0).getTextContent(),
                                element.getElementsByTagName("description").item(0).getTextContent(),
                                element.getElementsByTagName("link").item(0).getTextContent(),
                                element.getElementsByTagName("pubDate").item(0).getTextContent().substring(0, 25),
                                img
                        ));


                    }


                }
                noticias = newsletter;


            }catch(Exception ex){


            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
            if(!isNetworkAvailable()){

                this.cancel(true);

                ((Activity) root.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(root.getContext(), "No hay Conexion", Toast.LENGTH_SHORT).show();
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

                try{
                    noticias.size();

                btRefrescar.setVisibility(View.INVISIBLE);
                adaptador = new NoticiaAdaptador(noticias, getFragmentManager());
                recyclerView.setAdapter(adaptador);

                runLayoutAnimation(recyclerView);
                recyclerView.setHasFixedSize(true);
                swipeRefreshLayout.setRefreshing(false);
                }catch(Exception ex){
                    swipeRefreshLayout.setRefreshing(false);
                    btRefrescarVisible();
                }



        }




    }


}