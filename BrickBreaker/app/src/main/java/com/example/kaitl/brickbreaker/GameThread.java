package com.example.kaitl.brickbreaker;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

class GameThread extends Thread
{

    private GameView view;
    private boolean running = false;

    public GameThread (GameView view)
    {
        this.view = view;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        super.run();

        while (running)
        {
            Canvas c = null;

            try
            {
                c = view.getHolder().lockCanvas();

                synchronized (view.getHolder())
                {
                    view.onDraw(c);
                }
            }
            finally
            {
                if (c!= null)
                {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }
        }
    }

    public void SetRunning(boolean run)
    {
        running = run;
    }

}
