package com.emiliorgvintaje.myapps.ui.maps;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.emiliorgvintaje.myapps.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
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

    private View root;


    //Ruta y linea
    private Polyline route;


    //Elementos de la UI
    private TextView tiempo;
    private TextView distancia;
    private Button autolocalizador;


    private Handler handler;

    public static MapasFragment newInstance() {
        return new MapasFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.mapas_fragment, container, false);

        tiempo = root.findViewById(R.id.tvHoraActual);


        mPosicion = LocationServices.getFusedLocationProviderClient(getContext());
        handler = new Handler();
        autolocalizador = root.findViewById(R.id.btDesactivarLocalizador);
        tiempo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_black_24dp, 0, 0, 0);;

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

        return root;
    }


    public void printTime() {
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
            }
        });
        mMap.setMyLocationEnabled(true);
        // Configurar IU Mapa
        configurarIUMapa();


        // Añadmimos marcadores
        añadirMarcadores();

        // activa el evento de marcadores Touc
        activarEventosMarcdores();

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
        // Yo lo haría con obtenerposición, pues hacemos lo mismo
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Crear el LocationRequest
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 segundos en milisegundos
                .setFastestInterval(1 * 1000); // 1 segundo en milisegundos

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
                            // Añadimos un marcador especial para poder operar con esto
                            marcadorPosicionActual();
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

    // Para dibujar el marcador actual
    private void marcadorPosicionActual() {

        // Borramos el arcador actual si está puesto
        if (marcadorActual != null) {
            marcadorActual.remove();
        }
        // añadimos el marcador actual
        marcadorActual = mMap.addMarker(new MarkerOptions()
                // Posición
                .position(posActual)
                // Título
                .title("Mi Localización")
                // Subtitulo
                .snippet("Localización actual")
                // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );

    }


    // solicitamos los permisos para leer de algo
    // Esto debemos hacerlo con todo

    // Hilo con un reloj interno
    // Te acuerdas de los problemas de PSP con un Thread.Sleep()
    // Aqui lo llevas
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

                            } catch (Exception e) {
                                Log.e("TIMER", "Error: " + e.getMessage());
                            }
                        }
                    }
                });
            }


        };
        // Actualizamos cada 10 segundos
        // podemos pararlo con timer.cancel();
        timer.schedule(doAsyncTask, 0, 5000);
    }


    private void activarEventosMarcdores() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // Creamos el marcador
                // Borramos el marcador Touch si está puesto
                if (marcadorTouch != null) {
                    marcadorTouch.remove();
                }
                marcadorTouch = mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(point)
                        // Título
                        .title("Marcador Touch")
                        // Subtitulo
                        .snippet("El Que tú has puesto")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

            }
        });


    }

    private void situarCamaraMapa() {
        // Puedo moverla a una posición que queramos
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(posDefecto));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posActual));
    }

    private void configurarIUMapa() {
        // Puedo activar eventos para un solo marcador o varios
        // Con la interfaz  OnMarkerClickListener
        mMap.setOnMarkerClickListener(this);

        // Activar Boton de Posición actual
        if (permisos) {
            // Si tenemos permisos pintamos el botón de la localización actual
            // Esta posición la obtiene google automaticamente y no tiene que ver con
            // obtener posición
            mMap.setMyLocationEnabled(true);
        }

        // Mapa híbrido, lo normal es usar el
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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
    }

    private void añadirMarcadores() {
        // Podemos lerlos de cualquier sitios
        // Añadimos un marcador en la estación
        // Podemos leerlos de un servición, BD SQLite, XML, Arraylist, etc
        LatLng estacion = new LatLng(38.69128, -4.111655);
        mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(estacion)
                        // Título
                        .title("Estación de AVE")
                        // Subtitulo
                        .snippet("Estación AVE de Puertollano")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        );

        // Añadimos el ayuntamiento
        // Añadimos un marcador en la estación
        LatLng ayto = new LatLng(38.6866069, -4.1110002);
        mMap.addMarker(new MarkerOptions()
                        // Posición
                        .position(ayto)
                        // Título
                        .title("Ayuntamiento")
                        // Subtitulo
                        .snippet("Ayuntamiento de Puertollano")
                        // Color o tipo d icono
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        );
    }

}
