package com.example.kaitl.splashscreen;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity
{
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash1);

        mp = MediaPlayer.create(this, R.raw.bubbles);
        mp.start();

        Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);

                    startActivity(myIntent);
                }
            }
        };

        timer.start();
    }

    protected void onPause()
    {
        super.onPause();

        if(mp.isPlaying())
            mp.stop();

        mp.release();

        this.finish();
    }
}
