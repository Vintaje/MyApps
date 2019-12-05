package com.emiliorgvintaje.myapps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Creamos un Handler que maneja el tiempo de muestra del SplashScreen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashScreen.this.finish();
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(intent);
            }
        }, 2000);
    }
}
