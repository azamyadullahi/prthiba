package com.errortech.prthiba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class splashScreen extends AppCompatActivity {

    private int SPLASH_TIME = 2000;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(splashScreen.this,MainActivity.class));
                    finish();

                }
            }
        };
        timer.start();

    }


}
