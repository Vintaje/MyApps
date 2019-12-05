package com.emiliorgvintaje.myapps.ui.sensores.Classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorEvent;
import android.view.View;

/**
 * Clase que crea una animacion para printar una bola en funcion del Accelerometro
 */
public class AnimacionBall extends View {

    private static final int CIRCLE_RADIUS = 10; //Pixeles

    private Paint mPaint;
    private int x;
    private int y;
    private int viewWidth;
    private int viewHeight;

    /**
     * Constructor al que pasamos el contexto
     * @param context Contexto de la vista actual
     */
    public AnimacionBall(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(android.R.color.holo_blue_light));
    }

    /**
     * A la espera de que cambie el tamaño para rediseñar las dimensiones
     * @param w Ancho
     * @param h Alto
     * @param oldw Ancho anterior
     * @param oldh Alto anterior
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    /**
     * A la espera del que el sensor envie datos para dibujar la pelota
     * @param event evento
     */
    public void onSensorEvent (SensorEvent event) {
        x = x - (int) event.values[0];
        y = y + (int) event.values[1];
        //Nos aseguramos de que dibujamos dentro del espacio establecido
        //Entonces los maximos valores donde podremos dibujar son:
        if (x <= 0 + CIRCLE_RADIUS) {
            x = 0 + CIRCLE_RADIUS;
        }
        if (x >= viewWidth - CIRCLE_RADIUS) {
            x = viewWidth - CIRCLE_RADIUS;
        }
        if (y <= 0 + CIRCLE_RADIUS) {
            y = 0 + CIRCLE_RADIUS;
        }
        if (y >= viewHeight - CIRCLE_RADIUS) {
            y = viewHeight - CIRCLE_RADIUS;
        }
    }


    /**
     * OnDraw para ir dibujando en el canvas, por eso mismo llamamos al invalidate
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(x, y, CIRCLE_RADIUS, mPaint);
        //Necesitamos llamar al invalidador para dibujar constantemente
        invalidate();
    }
}