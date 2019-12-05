package com.emiliorgvintaje.myapps.ui.sensores.Classes;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.view.Surface;
import android.view.WindowManager;

/**
 * Clase que maneja el sensor de Rotacion Vectorial
 */
public class Orientation implements SensorEventListener {

    /**
     * Llamamos a la interface Listener
     */
    public interface Listener {
        //Cambiamos el cabeceo y balanceo en base a los valores introducidos
        void onOrientationChanged(float pitch, float roll);
    }

    //Delay de 16 ms
    private static final int SENSOR_DELAY_MICROS = 16 * 1000; // 16ms

    private final WindowManager mWindowManager;

    private final SensorManager mSensorManager;


    private final Sensor mRotationSensor;

    private int mLastAccuracy;
    private Listener mListener;

    /**
     * Constructor de la clase al que le pasamos una actividad
     * @param activity actividad
     */
    public Orientation(Activity activity) {
        mWindowManager = activity.getWindow().getWindowManager();
        mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        //Puede ser null si el sensor no esta disponible
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Iniciamos el listener y sus metodos de escucha
     * @param listener listener
     */
    public void startListening(Listener listener) {
        if (mListener == listener) {
            return;
        }
        mListener = listener;
        if (mRotationSensor == null) {
            System.out.println("Sensor de rotacion vectorial no disponible");
            return;
        }
        //Registramos el sensor
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
    }
    //Deja de escuchar
    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }

    //Si cambia la precision
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    //Si cambia el sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) {
            return;
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == mRotationSensor) {
            //Actualizamos la orientacion
            updateOrientation(event.values);
        }
    }


    /**
     * Actualizamos la rotacion y remapeamos los ejes dependiendo de la orientacion
     * inicial
     * @param rotationVector vector de rotacion
     */
    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remapeamos los ejes si el dispositivo tiene un panel de control
        // y ajustamos la matriz de rotacion al dispositivo
        switch (mWindowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transformamos la rotacion en generar un cabeceo, guiñada y rotacion(Basandonos de que estamos
        // en un avion)
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // Convertimos los radianes a grados
        float pitch = orientation[1] * -57;
        float roll = orientation[2] * -57;

        //Llamamos a nuestro listener
        mListener.onOrientationChanged(pitch, roll);
    }
}