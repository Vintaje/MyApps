package com.emiliorgvintaje.myapps.ui.maps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.maps.Ruta.RutasFragment;
import com.emiliorgvintaje.myapps.util.GPX.GPXUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapasFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 1; // Para los permisos
    private boolean permisos = false;

    // Para obtener el punto actual (no es necesario para el mapa)
    // Pero si para obtener las latitud y la longitud
    private FusedLocationProviderClient mPosicion;


    private LatLng posLast;
    private LatLng posActual;

    // Marcador actual
    private Marker marcadorActual = null;

    // Marcador marcadorTouch
    private Marker marcadorTouch = null;

    // Posición actual con eventos y no hilos
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Runnable runnable;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //Boolean para el autoactualizar
    private boolean actualizar;

    //Boolean para guardar ruta
    private boolean grabar;



    private View root;


    //Ruta y linea
    private Polyline line;
    private ArrayList<LatLng> ruta;
    private double dist;
    private Marker inicio, fin;

    //Elementos de la UI
    private TextView tiempo;
    private TextView distancia;
    private Button autolocalizador;
    private FloatingActionButton fabRuta;
    private LinearLayout layoutRuta;
    private Button rutas;
    private Button cancelar;


    private Handler handler;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.mapas_fragment, container, false);
        grabar = false;
        tiempo = root.findViewById(R.id.tvHoraActual);
        fabRuta = root.findViewById(R.id.fabRuta);
        cancelar = root.findViewById(R.id.btCancelarRuta);
        rutas = root.findViewById(R.id.btRutas);

        distancia = root.findViewById(R.id.tvDistanciaActual);
        mPosicion = LocationServices.getFusedLocationProviderClient(root.getContext());
        handler = new Handler();
        autolocalizador = root.findViewById(R.id.btDesactivarLocalizador);
        tiempo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_black_24dp, 0, 0, 0);

        layoutRuta = root.findViewById(R.id.layoutRuta);

        autolocalizador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actualizar){
                    actualizar = false;
                    autolocalizador.setText(R.string.actv_autoloc);
                    Toast.makeText(getContext(),"Auto Localizador desactivado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Auto Localizador activado", Toast.LENGTH_SHORT).show();
                    actualizar = true;
                    autolocalizador.setText(R.string.desct_autoloc);
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();

        getFragmentManager().beginTransaction().replace(R.id.mapContainer, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);
        actualizar = true;
        layoutRuta.setVisibility(View.INVISIBLE);


        fabRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(grabar){
                    grabar = false;
                    fabRuta.setImageResource(android.R.drawable.ic_media_play);
                    autolocalizador.setVisibility(View.VISIBLE);
                    layoutRuta.setVisibility(View.INVISIBLE);
                    alertDialog();
                    mMap.clear();
                    obtenerPosicion();
                    //Marcador de inicio
                    inicio = mMap.addMarker(new MarkerOptions()
                            // Posición
                            .position(posActual)
                            // Título
                            .title("Inicio")
                            // Subtitulo
                            .snippet("Inicio Ruta")
                            // Color o tipo d icono
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );
                    mMap.setMyLocationEnabled(true);

                }else{
                    grabar = true;
                    fabRuta.setImageResource(R.drawable.ic_clear_black_24dp);
                    autolocalizador.setVisibility(View.INVISIBLE);
                    dist = 0;
                    layoutRuta.setVisibility(View.VISIBLE);
                    ruta = new ArrayList<>();
                    distancia.setText(String.format(getString(R.string.kmmarker), dist));

                }
            }

        });


        rutas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RutasFragment rutasFragment = new RutasFragment(getFragment());
                getFragmentManager().beginTransaction().add(R.id.nav_host_fragment,rutasFragment)
                        .setCustomAnimations(R.anim.fragment_effect, R.anim.fragment_effect_exit).addToBackStack(null).commit();
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                layoutRuta.setVisibility(View.INVISIBLE);
                mMap.clear();
                fabRuta.setVisibility(View.VISIBLE);
                rutas.setVisibility(View.VISIBLE);
                autolocalizador.setVisibility(View.VISIBLE);
                cancelar.setVisibility(View.INVISIBLE);
                actualizar = true;
            }
        });


        return root;
    }

    public MapasFragment getFragment(){
        return this;
    }

    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Introduce el nombre del fichero a guardar");
        dialog.setTitle("Guardar...");
        // Seteamos el input
        final EditText input = new EditText(getContext());
        // Especificamos el tipo de input
        input.setPadding(40,0,40,0);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(input);
        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String path = input.getText().toString().trim();
                        if(path.isEmpty()) Toast.makeText(getContext(),"Nombre Incorrecto", Toast.LENGTH_SHORT).show();
                        else GPXUtil.build(ruta,root.getContext(),path);
                    }
                });
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(),"Guardado Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }


    private void printTime() {
        //Imprimir en TextView la hora y fecha actual
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Timer timer = new Timer();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getActivity().runOnUiThread(runnable = new Runnable() {
                                @Override
                                public void run() {
                                    String datetime = sdf.format(new Date());
                                    tiempo.setText(datetime);
                                }
                            });

                        } catch (Exception e) {
                            Log.e("TIMER", "Error: " + e.getMessage());
                        }
                    }
                });
            }


        };

        timer.schedule(doAsyncTask, 0, 1000);
    }


    @Override
    public void onDestroy() {

        handler.removeCallbacks(null);
        handler = new Handler();
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {


        //new LatLng(, ));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                posActual = new LatLng(location.getLatitude(), location.getLongitude());
                if(actualizar){
                    situarCamaraMapa();

                }
            }
        });
        mMap.setMyLocationEnabled(true);
        // Configurar IU Mapa
        configurarIUMapa();

        // Obtenemos la posición GPS
        // Esto lo hago para informar de la última posición
        // Obteniendo coordenadas GPS directamente
        obtenerPosicion();
        // Situar la camara inicialmente a una posición determinada

        // Acrtualizar cada X Tiempo, implica programar eventos o hacerlo con un hilo
        // Esto consume, por lo que ideal es activarlo y desactivarlo
        // cuando sea necesario
        autoActualizador();

        // Para usar eventos
        mGoogleApiClient = new GoogleApiClient.Builder(root.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Crear el LocationRequest
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 segundos en milisegundos
                .setFastestInterval(1000); // 1 segundo en milisegundos

        printTime();
    }


    private void obtenerPosicion() {
        try {
            if (permisos) {
                // Lo lanzamos como tarea concurrente
                Task<Location> local = mPosicion.getLastLocation();

                local.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            posActual = new LatLng(task.getResult().getLatitude(),
                                    task.getResult().getLongitude());
                            situarCamaraMapa();
                        } else {
                            Log.d("GPS", "No se encuetra la última posición.");
                            Log.e("GPS", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //Calculamos la distancia entre dos puntos
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    @SuppressLint("RestrictedApi")
    public void cargarRuta(ArrayList<LatLng> puntos){
        actualizar = false;
        mMap.addMarker(new MarkerOptions()
                // Posición
                .position(puntos.get(puntos.size()-1))
                // Título
                .title("Inicio")
                // Subtitulo
                .snippet("Inicio Ruta")
                // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(puntos.get(0)));

        mMap.addMarker(new MarkerOptions()
                // Posición
                .position(puntos.get(0))
                // Título
                .title("Fin")
                // Subtitulo
                .snippet("Fin Ruta")
                // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(puntos.get(puntos.size()-1)));
        PolylineOptions options = new PolylineOptions().width(5).color(Color.CYAN).geodesic(true);
        for (int z = 0; z < puntos.size(); z++) {
            LatLng point = puntos.get(z);
            options.add(point);
        }
        line = mMap.addPolyline(options);
        layoutRuta.setVisibility(View.VISIBLE);

        rutas.setVisibility(View.INVISIBLE);
        fabRuta.setVisibility(View.INVISIBLE);
        autolocalizador.setVisibility(View.INVISIBLE);
        cancelar.setVisibility(View.VISIBLE);
        dist = 0;
        for (int i = 1; i < puntos.size();i++){
            dist += distance(puntos.get(i-1).latitude, puntos.get(i-1).longitude,puntos.get(i).latitude,puntos.get(i).longitude);

        }
        distancia.setText(String.format(getString(R.string.kmmarker), dist));
    }

    // Hilo con un reloj interno
    private void autoActualizador() {
        Timer timer = new Timer();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(actualizar) {
                            try {
                                // Obtenemos la posición
                                obtenerPosicion();
                                situarCamaraMapa();
                                if(grabar) {
                                    ruta.add(posActual);
                                    if(ruta.size() > 1) {
                                        dist += distance(ruta.get(ruta.size()-2).latitude,ruta.get(ruta.size()-2).longitude,ruta.get(ruta.size()-1).latitude,ruta.get(ruta.size()-1).longitude);
                                        distancia.setText(String.format(getString(R.string.kmmarker), dist));
                                    }

                                    //Pintamos la ruta
                                    PolylineOptions options = new PolylineOptions().width(10).color(Color.CYAN).geodesic(true);
                                    for (int z = 0; z < ruta.size(); z++) {
                                        LatLng point = ruta.get(z);
                                        options.add(point);
                                    }
                                    line = mMap.addPolyline(options);
                                }
                            } catch (Exception e) {
                                Log.e("TIMER", "Error: " + e.getMessage());
                            }
                        }
                    }
                });
            }


        };
        // Actualizamos cada 1 segundo
        timer.schedule(doAsyncTask, 0, 1000);
    }

    private void situarCamaraMapa() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posActual));
    }

    private void configurarIUMapa() {

        mMap.setOnMarkerClickListener(this);

        // Activar Boton de Posición actual
        if (permisos) {
            mMap.setMyLocationEnabled(true);
        }

        // Mapa híbrido, lo normal es usar el
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Que se vea la interfaz y la brújula por ejemplo
        // También podemos quitar gestos
        UiSettings uiSettings = mMap.getUiSettings();
        // Activamos los gestos
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        // Activamos la brújula
        uiSettings.setCompassEnabled(true);
        // Activamos los controles de zoom
        uiSettings.setZoomControlsEnabled(true);
        // Activamos la brújula
        uiSettings.setCompassEnabled(true);
        // Actiovamos la barra de herramientas
        uiSettings.setMapToolbarEnabled(true);

        // Hacemos el zoom por defecto mínimo
        mMap.setMinZoomPreference(15.0f);
        // Señalamos el tráfico
        mMap.setTrafficEnabled(true);

        boolean success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        getContext(), R.raw.mapthem));

        if(success){
            Toast.makeText(getContext(), "Mapa cargado correctamente",Toast.LENGTH_SHORT).show();

        }
    }





}
