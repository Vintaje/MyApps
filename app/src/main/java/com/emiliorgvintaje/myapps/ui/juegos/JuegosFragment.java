package com.emiliorgvintaje.myapps.ui.juegos;

import com.emiliorgvintaje.myapps.DBC;

import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.emiliorgvintaje.myapps.MainActivity;
import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.juegos.detalles.JuegosDetalleFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;

public class JuegosFragment extends Fragment {


    private Paint p = new Paint();
    private JuegoAdaptador adaptador;

    private ArrayList<Juego> juegoslist = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private View root;
    private RecyclerView recyclerView;
    private FloatingActionButton fabJuegos;

    private static int idborrado;

    private Spinner spFiltroJuegos;

    private static int seleccion = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        root = inflater.inflate(R.layout.juegos_fragment, container, false);
        instanciarItems();
        ((MainActivity) getActivity()).hideSave();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.mis_juegos_title);
        ((MainActivity) getActivity()).hideBack();
        rellenarSpinner();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ((MainActivity) getActivity()).hideShare();
        ((MainActivity) getActivity()).hideFloatingActionButton();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((MainActivity) getActivity()).setDrawerLocked(false);

        ((MainActivity) getActivity()).editando = false;

        spinnerListener();

        new juegoLoader().execute();
    }


    /**
     * Instanciamos los items que necesitamos previamente antes de realizar cualquier cosa en el Fragment
     */
    public void instanciarItems() {
        iniciarSwipeRecargar();
        recyclerView = (RecyclerView) root.findViewById(R.id.rvJuegos);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        swipeRefreshLayout.setRefreshing(true);
        iniciarSwipeHorizontal();

        spFiltroJuegos = (Spinner) root.findViewById(R.id.spFiltrarJuegos);

        int resId = R.anim.layout_animation;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(root.getContext(), resId);
        recyclerView.setLayoutAnimation(animation);
        ((MainActivity) getActivity()).setDrawerLocked(false);
        fabJuegos = (FloatingActionButton) root.findViewById(R.id.fbJuegosAdd);

        fabJuegos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showSave();
                Fragment JuegoDetalle = new JuegosDetalleFragment(true);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fragment_effect, R.anim.fragment_effect_exit, R.anim.fragment_effect, R.anim.fragment_effect_exit);
                fragmentTransaction.replace(R.id.nav_host_fragment, JuegoDetalle);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
            }
        });
    }

    /**
     * Listener del spinner referenciado cuando la vista ya se ha creado
     */
    public void spinnerListener() {

        spFiltroJuegos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Here you change your value or do whatever you want

                switch (spFiltroJuegos.getSelectedItem().toString()) {
                    case "Filtrar: Nada":
                        seleccion = 0;

                        new juegoLoader().execute();
                        break;
                    case "Por Nombre Ascendente":
                        seleccion = 1;
                        new juegoLoader().execute();
                        break;
                    case "Por Lanzamiento Ascendente":
                        seleccion = 2;
                        new juegoLoader().execute();
                        break;
                    case "Por Plataforma Ascendente":
                        seleccion = 3;
                        new juegoLoader().execute();
                        break;
                    case "Por Precio Ascendente":
                        seleccion = 4;
                        new juegoLoader().execute();
                        break;
                    case "Por Nombre Descendente":
                        seleccion = 5;
                        new juegoLoader().execute();
                        break;
                    case "Por Lanzamiento Descendente":
                        seleccion = 6;
                        new juegoLoader().execute();
                        break;
                    case "Por Plataforma Descendente":
                        seleccion = 7;
                        new juegoLoader().execute();
                        break;
                    case "Por Precio Descendente":
                        seleccion = 8;
                        new juegoLoader().execute();
                        break;
                }
                System.out.println(seleccion);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Rellenamos el spinner de Filtros con las opciones a realizar
     */
    public void rellenarSpinner() {

        ArrayList<String> filtro = new ArrayList<>();
        filtro.add("Filtrar: Nada");
        filtro.add("Por Nombre Ascendente");
        filtro.add("Por Nombre Descendente");
        filtro.add("Por Lanzamiento Ascendente");
        filtro.add("Por Lanzamiento Descendente");
        filtro.add("Por Plataforma Ascendente");
        filtro.add("Por Plataforma Descendente");
        filtro.add("Por Precio Ascendente");
        filtro.add("Por Precio Descendente");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(root.getContext(), R.layout.spinneritem, filtro);
        dataAdapter.setDropDownViewResource(R.layout.spinnerdropitem);
        spFiltroJuegos.setAdapter(dataAdapter);


    }

    /**
     * Instanciamos nuestra animacion y la seteamos al recycler
     *
     * @param recyclerView Recycler view a usar
     */
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);
        adaptador.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Iniciamos el movimiento Swipe To Refresh
     */
    private void iniciarSwipeRecargar() {


        swipeRefreshLayout = (SwipeRefreshLayout) root.getRootView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setColorSchemeResources(R.color.eurogamer);
                swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.whitebox);


                new juegoLoader().execute();

            }
        });

    }

    /**
     * Iniciamos el Swipe Horizontal para las acciones de Borrar o Editar
     */
    private void iniciarSwipeHorizontal() {


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
                    Fragment JuegoDetalle = new JuegosDetalleFragment(juegoslist.get(position), true, false);
                    ((MainActivity) getActivity()).showSave();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fragment_effect, R.anim.fragment_effect_exit, R.anim.fragment_effect, R.anim.fragment_effect_exit);
                    fragmentTransaction.replace(R.id.nav_host_fragment, JuegoDetalle);
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
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_bt);
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


    /**
     * Dialogo de atencion cuando el usuario quiere borrar un elemento, para que este mismo no se elimine
     * accidentalmente
     */
    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Accion irreversible: ¿Quiere borrar el elemento?");
        dialog.setTitle("Alerta");
        dialog.setPositiveButton("Confirmar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Toast.makeText(getContext(), "Elemento borrado", Toast.LENGTH_LONG).show();

                        DBC dbc = new DBC(root.getContext(), "BDJuegos", null, 1);
                        dbc.delete(idborrado);

                        new juegoLoader().execute();

                    }
                });

        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new juegoLoader().execute();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    /**
     * Iniciamos el proceso de borrado llamando al alertDialog
     *
     * @param position posicion del item accionado
     */
    private void borrarElemento(int position) {

        idborrado = juegoslist.get(position).getId();

        // Mostramos la barra
        alertDialog();


    }

    /**
     * AsyncTask que carga todos los juegos de la base de datos en el recycler view
     */
    public class juegoLoader extends AsyncTask<Void, Void, Void> {

        private boolean error;
        private DBC dbc;

        /**
         * Seteamos el swipe refresh layout a habilitado
         * Limpiamos la lista de juegos
         */
        @Override
        protected void onPreExecute() {
            // Saco la barra de progreso
            swipeRefreshLayout.setEnabled(true);
            juegoslist.clear();
            dbc = new DBC(root.getContext(), "BDJuegos", null, 1);
            this.error = false;
        }


        /**
         * Llamamos al metodo que realiza la Select en la Base de Datos en funcion de la opcion selecionada
         * en el Spinner de Filtros
         *
         * @param param Ningun parametro
         * @return Ningun resultado
         */
        @Override
        protected Void doInBackground(Void... param) {

            // Lo cargamos
            try {
                switch (seleccion) {
                    case 0:
                        System.out.println("Nada");
                        juegoslist = dbc.seleccionarData();
                        break;

                    //En este caso, ya en funcion del filtro, se hace la select con un order by
                    // y el tipo de orden
                    case 1:

                        juegoslist = dbc.seleccionarData("nombre", false);
                        break;
                    case 2:

                        juegoslist = dbc.seleccionarData("fecha_lanzamiento", false);
                        break;
                    case 3:

                        juegoslist = dbc.seleccionarData("plataforma", false);
                        break;
                    case 4:

                        juegoslist = dbc.seleccionarData("precio", false);
                        break;
                    case 5:

                        juegoslist = dbc.seleccionarData("nombre", true);
                        break;
                    case 6:

                        juegoslist = dbc.seleccionarData("fecha_lanzamiento", true);
                        break;
                    case 7:

                        juegoslist = dbc.seleccionarData("plataforma", true);
                        break;
                    case 8:

                        juegoslist = dbc.seleccionarData("precio", true);
                        break;
                }


            } catch (Exception ex) {
                this.error = true;
                System.out.println(ex.getMessage());
            }
            return null;

        }

        /**
         * Despues de realizar la accion de recuperar datos, si esta ha sido concluida perfectamente
         * cargaremos el resultado en el recycler view, sino se le mostrara un aviso de error al usuario
         *
         * @param aVoid Ningun parametro
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (error) {
                Toast.makeText(getContext(),"Error al cargar los datos",Toast.LENGTH_SHORT).show();
            } else {
                try {
                    juegoslist.size();

                    adaptador = new JuegoAdaptador(juegoslist, getFragmentManager());
                    recyclerView.setAdapter(adaptador);

                    runLayoutAnimation(recyclerView);
                    recyclerView.setHasFixedSize(true);
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception ex) {
                    swipeRefreshLayout.setRefreshing(false);

                }
            }

        }


    }



}
