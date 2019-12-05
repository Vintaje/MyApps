package com.emiliorgvintaje.myapps.ui.sensores;


import android.content.Context;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emiliorgvintaje.myapps.R;
import com.emiliorgvintaje.myapps.ui.sensores.Classes.AnimacionBall;
import com.emiliorgvintaje.myapps.ui.sensores.Classes.IndicadorAltura;
import com.emiliorgvintaje.myapps.ui.sensores.Classes.Orientation;
import com.emiliorgvintaje.myapps.ui.sensores.Classes.SensorAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.List;


/**
 * Clase del Fragment que muestra todos los sensores del movil
 */
public class SensoresFragment extends Fragment implements SensorEventListener, Orientation.Listener {

    private RecyclerView recyclerView;
    private SensorManager sensorManager;
    private List<Sensor> listsensor;
    private LineChart mChart;
    private Thread thread;
    private boolean encendida;


    private TextView acelerometro, orientacion, temperatura, proximidad, campomag, gravedad;
    private ArrayList<String> liststring;
    private SensorAdapter adaptador;
    private AnimacionBall mAnimacionBall = null;
    private LinearLayout accelerometer;
    private Orientation mOrientation;
    private boolean plotData = true;
    private ImageView compass;
    private View root;
    private FloatingActionButton fabLight;

    private IndicadorAltura mIndicadorAltura;
    private float currentDegree = 0f;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.sensores_fragment, container, false);

        instanciaItems();

        fabLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!encendida) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flashLightOn();
                    }
                    encendida = true;
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flashLightOff();
                    }
                    encendida = false;
                }
            }
        });


        instanciarChart();
        return root;
    }

    /**
     * Instancia de todos los items de la interfaz
     */
    public void instanciaItems() {
        recyclerView = (RecyclerView) root.findViewById(R.id.rvSensores);

        acelerometro = (TextView) root.findViewById(R.id.tvAcelerometro);
        orientacion = (TextView) root.findViewById(R.id.tvOrientacion);
        temperatura = (TextView) root.findViewById(R.id.tvTemperatura);
        proximidad = (TextView) root.findViewById(R.id.tvProximidad);
        campomag = (TextView) root.findViewById(R.id.tvCampoMag);
        gravedad = (TextView) root.findViewById(R.id.tvGravedad);
        mAnimacionBall = new AnimacionBall(getContext());
        accelerometer = (LinearLayout) root.findViewById(R.id.linearDraw);
        accelerometer.addView(mAnimacionBall);
        compass = (ImageView) root.findViewById(R.id.ivCompass);

        mOrientation = new Orientation(getActivity());
        mIndicadorAltura = (IndicadorAltura) root.findViewById(R.id.attitude_indicator);
        fabLight = (FloatingActionButton) root.findViewById(R.id.fabLight);
    }


    /**
     * Metodo para encender la linterna
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            encendida = true;
            fabLight.setImageResource(R.drawable.ic_highlight_black_24dp);
        } catch (CameraAccessException e) {
        }
    }

    /**
     * Metodo para apagar la linterna
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            encendida = false;
            fabLight.setImageResource(R.drawable.ic_highlight_off_black_24dp);
        } catch (CameraAccessException e) {
        }
    }

    /**
     * Instancia del Grafico del Flujo Magnetico
     */
    public void instanciarChart() {
        mChart = (LineChart) root.findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText(getString(R.string.flujo_magnetico));

        // Habilitamos los gestos
        mChart.setTouchEnabled(true);

        // Habilitamos el reescalado y arrastrar
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        //Zoom de X e Y
        mChart.setPinchZoom(true);

        //Background del grafico
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        //Datos vacios
        mChart.setData(data);

        //Leyenda del graphico
        Legend l = mChart.getLegend();

        //Modificamos la leyenda
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(-100f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);


        agregarDatos();
    }

    /**
     * onStart()
     * Inicia la escucha del listener del sensor de posicion de rotacion vectorial
     * Registra los sensores
     */
    @Override
    public void onStart() {
        super.onStart();
        mOrientation.startListening(this);
        registerList();
    }

    /**
     * onStop
     * Para la escucha del listener del sensor de posicion de rotacion vectorial
     * Quita los sensores
     */
    @Override
    public void onStop() {
        super.onStop();
        mOrientation.stopListening();
        sensorManager.unregisterListener(this);
    }

    /**
     * Cuando la orientacion cambia, cambiamos el propio cabeceo y balanceo
     *
     * @param pitch Cabeceo
     * @param roll  Balanceo
     */
    @Override
    public void onOrientationChanged(float pitch, float roll) {
        mIndicadorAltura.setAttitude(pitch, roll);
    }

    /**
     * onActivityCreated()
     * Listamos todos los sensores disponibles
     *
     * @param savedInstanceState Instancia salvada
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel


        liststring = new ArrayList<String>();

        sensorManager = (SensorManager) getActivity().getSystemService(getContext().SENSOR_SERVICE);

        listsensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 0; i < listsensor.size(); i++) {

            liststring.add(listsensor.get(i).getName());
            Sensor sensorscope = listsensor.get(i);

        }

        //Instancia del recycler
        initRecyclerView();


    }

    /**
     * Instanciamos un hilo que manejara el flujo de datos en el grafico del Flujo Magnetico
     */
    private void agregarDatos() {
        //Agregamos los datos
        if (thread != null) {
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }


    /**
     * Instancia del recyclerview
     */
    public void initRecyclerView() {


        adaptador = new SensorAdapter(liststring);

        recyclerView.setAdapter(adaptador);
        runLayoutAnimation(recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewCacheSize(17);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


    }

    /**
     * Se agrega un nuevo punto de valor al grafico del Flujo Magnetico
     *
     * @param event
     */
    private void addEntry(SensorEvent event) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);


            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }


            data.addEntry(new Entry(set.getEntryCount(), event.values[0] + 5), 0);
            data.notifyDataChanged();

            // Notificamos el cambio de datos
            mChart.notifyDataSetChanged();

            // Limite de visibilidad
            mChart.setVisibleXRangeMaximum(150);


            // Movemos siempre el grafico al ultimo dato recogido
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    /**
     * DataSet del grafico de Flujo Magnetico
     *
     * @return DataSet
     */
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    /**
     * Ejecucion de la animacion de recyclerview
     *
     * @param recyclerView
     */
    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        recyclerView.setLayoutAnimation(controller);

        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Listener de los sensores, en funcion del tipo que sea:
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            StringBuilder builder;
            switch (event.sensor.getType()) {

                case Sensor.TYPE_ORIENTATION:
                    builder = new StringBuilder();
                    builder.append("Orientacion\n");
                    for (int i = 0; i < 3; i++) {
                        float degree = Math.round(event.values[0]);
                        builder.append(String.format("%s    ", String.format("%.2f", event.values[i])));
                        //Creamos una animacion de rotacion
                        RotateAnimation ra = new RotateAnimation(
                                currentDegree,
                                -degree,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);

                        //Duracion de la animacion
                        ra.setDuration(210);

                        //Seteamos la animacion despues del estado de reservado
                        ra.setFillAfter(true);

                        //Iniciamos la animacion
                        compass.startAnimation(ra);
                        currentDegree = -degree;
                    }
                    builder.append("\n");
                    orientacion.setText(builder.toString());

                    break;

                case Sensor.TYPE_ACCELEROMETER:
                    //Acelerometro
                    mAnimacionBall.onSensorEvent(event);
                    builder = new StringBuilder();
                    builder.append("Acelerometro\n");
                    for (int i = 0; i < 3; i++) {

                        builder.append(String.format("%s    ", String.format("%.2f", event.values[i])));

                    }
                    builder.append("\n");
                    acelerometro.setText(builder.toString());
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    //Rotacion vectorial
                    builder = new StringBuilder();
                    builder.append("Posicionamiento\n");
                    for (int i = 0; i < 3; i++) {

                        builder.append(String.format("%s    ", String.format("%.2f", event.values[i])));

                    }
                    builder.append("\n");
                    gravedad.setText(builder.toString());
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    //Campo Magnetico
                    builder = new StringBuilder();
                    builder.append("Campo Magnetico\n");
                    for (int i = 0; i < 3; i++) {

                        builder.append(String.format("%s    ", String.format("%.2f", event.values[i])));

                    }
                    builder.append("\n");
                    campomag.setText(builder.toString());
                    if (plotData) {
                        addEntry(event);
                        plotData = false;
                    }
                    break;

                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    //Temperatura ambiental
                    builder = new StringBuilder();
                    builder.append("Temperatura\n");


                    builder.append(String.format("%s    ", String.format("%.2f", event.values[0])));


                    builder.append("\n");
                    temperatura.setText(builder.toString());
                    break;
                case Sensor.TYPE_PROXIMITY:
                    //Proximidad
                    builder = new StringBuilder();
                    builder.append("Proximidad\n");

                    if (event.values[0] > 0) {
                        builder.append(String.format("%s    ", String.format("%.2f Lejos", event.values[0])));
                    } else {
                        builder.append(String.format("%s    ", String.format("%.2f Cerca", event.values[0])));
                    }

                    builder.append("\n");
                    proximidad.setText(builder.toString());
                    break;

            }

        }
    }


    /**
     * Recogemos la lista de sensores
     */
    public void registerList() {


        //Registramos en nuestro manager los sensores
        //Orientacion
        listsensor = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (!listsensor.isEmpty()) {

            Sensor orientationSensor = listsensor.get(0);

            sensorManager.registerListener(this, orientationSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

        //Accelerometro
        listsensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (!listsensor.isEmpty()) {

            Sensor acelerometerSensor = listsensor.get(0);

            sensorManager.registerListener(this, acelerometerSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

        //Proximidad
        listsensor = sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (!listsensor.isEmpty()) {

            Sensor magneticSensor = listsensor.get(0);

            sensorManager.registerListener(this, magneticSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

        //Temperatura ambiental
        listsensor = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (!listsensor.isEmpty()) {

            Sensor temperatureSensor = listsensor.get(0);

            sensorManager.registerListener(this, temperatureSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            temperatura.setText(R.string.temp_no_disponible);
            temperatura.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        }

        //Rotacion Vectorial
        listsensor = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        if (!listsensor.isEmpty()) {

            Sensor temperatureSensor = listsensor.get(0);

            sensorManager.registerListener(this, temperatureSensor,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            gravedad.setText(R.string.no_disponible);
            gravedad.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        }

        listsensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

        if (!listsensor.isEmpty()) {

            Sensor temperatureSensor = listsensor.get(0);

            sensorManager.registerListener(this, temperatureSensor,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            campomag.setText(R.string.no_disponible);
            campomag.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        }

        listsensor = sensorManager.getSensorList(Sensor.TYPE_LIGHT);

        if (!listsensor.isEmpty()) {

            Sensor temperatureSensor = listsensor.get(0);

            sensorManager.registerListener(this, temperatureSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }


    //Si cambia la precision
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
