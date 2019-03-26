package com.example.kaitl.brickbreaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static java.lang.Math.max;
import static java.lang.Math.min;

class GameView extends SurfaceView
{
    private SurfaceHolder holder;

    private int ballX = 10;
    private int ballY = 10;
    private int xspeed = 10;
    private int yspeed = 10;
    private int radius = 20;

    private Brick[] bricks;
    private int brickHeight = 40;
    private int brickWidth = 80;

    private int paddleX = 0;
    private int paddleY = 0;
    private int paddleHeight = 40;
    private int paddleWidth = 80;
    private int paddleSpeed = 10;

    private int level = 1;
    private int maxLevel = 2;

    private float previousMouseX;
    private float previousMouseY;

    private GameThread gameThread;

    public GameView(Context context)
    {
        super(context);

        gameThread = new GameThread(this);
        holder = getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                gameThread.SetRunning(true);

                gameThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                boolean retry = true;
                gameThread.SetRunning(false);
                while (retry)
                {
                    try
                    {
                        gameThread.join();

                        retry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Setup();
    }

    @Override
    protected  void onDraw(Canvas canvas)
    {
        checkCollision(canvas);

        //keep paddle Y constant
        if(paddleY == 0)
            paddleY = canvas.getHeight() - 30;

        //draw the game
        if (canvas != null)
        {
            //canvas colours
            canvas.drawColor(Color.BLUE);
            Paint paint = new Paint();

            //draw the ball
            paint.setColor(Color.GRAY);
            canvas.drawCircle(ballX, ballY, radius, paint);
            Log.i("debug", "onDraw: ball drawn");

            //draw paddle
            paint.setColor(Color.BLACK);
            canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);

            //draw bricks
            for(int i = 0; i < bricks.length; i++)
            {
                if(bricks[i].getHealth() > 0)
                {
                    if (bricks[i].getHealth() == 1)
                        paint.setColor(Color.YELLOW);
                    else if(bricks[i].getHealth() == 2)
                        paint.setColor(Color.GREEN);
                    canvas.drawRect(bricks[i].getX(), bricks[i].getY(),
                            bricks[i].getX() + brickWidth, bricks[i].getY() + brickHeight, paint);
                    Log.i("debug", "onDraw: brick drawn");
                }
            }
        }
    }

    private void Setup()
    {

        Log.i("debug", "Setup: called");
        if(level == 1)
        {
            Log.i("debug", "Setup: level 1");
            bricks = new Brick[10];
            for(int i = 0; i < bricks.length; i++)
            {
                bricks[i] = new Brick(i*100, 200, 1);
            }
        }
        else if (level == 2)
        {
            bricks = new Brick[10];
            for(int i = 0; i < bricks.length; i++)
            {
                bricks[i] = new Brick(i*10, i, 1);
            }
        }
    }

    private void checkCollision(Canvas canvas)
    {
        ballX += xspeed;
        ballY += yspeed;

        //check for off screen
        if(ballX < 0 || ballX + radius >= canvas.getWidth())
            xspeed*=-1;
        if(ballY < 0 )
            yspeed*=-1;
        else if(ballY + radius >= canvas.getHeight())
            GameOver(false);

        //check for paddle


        //check for each brick
        for(int i = 0; i < bricks.length; i++)
        {
            int DeltaX = ballX - max(bricks[i].getX(), min(ballX, bricks[i].getX() + brickWidth));
            int DeltaY = ballY - max(bricks[i].getY(), min(ballY, bricks[i].getY() + brickHeight));

            if((DeltaX * DeltaX + DeltaY * DeltaY) < (radius * radius))
            {
                if (bricks[i].getHealth() > 0)
                {
                    if (Math.abs(ballY - bricks[i].getY()) < radius)
                    {
                        // Hit the top
                        bricks[i].setHealth(bricks[i].getHealth() - 1);
                        yspeed *= -1;
                    }
                    else if (Math.abs(ballY - bricks[i].getY() + brickHeight) < radius)
                    {
                        // Hit the bottom
                        bricks[i].setHealth(bricks[i].getHealth() - 1);
                        yspeed *= -1;
                    }
                    else if (Math.abs(ballX - bricks[i].getX()) < radius)
                    {
                        // Hit the left
                        bricks[i].setHealth(bricks[i].getHealth() - 1);
                        xspeed *= -1;
                    }
                    else if (Math.abs(ballX - bricks[i].getX() + brickWidth) < radius)
                    {
                        // Hit the right
                        bricks[i].setHealth(bricks[i].getHealth() - 1);
                        xspeed *= -1;
                    }
                }
            }
        }
    }

    private boolean levelOver()
    {
        //if no bricks are left the game is over
        boolean bricksleft = false;
        for(int i = 0; i < bricks.length; i++)
        {
            if(bricks[i].getHealth() > 0)
                bricksleft = true;
        }

        if(bricksleft) return false;
        return true;
    }

    private void LevelOver()
    {
        level++;
        if (level <= maxLevel)
            Setup();
        else
            GameOver(true);
    }

    private void GameOver(boolean win)
    {

    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        float x = e.getX();
        float y = e.getY();

        if(e.getAction() == MotionEvent.ACTION_MOVE)
        {
            //horizontal displacement
            float dx = x - previousMouseX;
            //float dy = y - previousMouseY;

            if(dx < 0)
                paddleX -= paddleSpeed;
            else if(dx > 0)
                paddleX += paddleSpeed;
        }

        previousMouseX = x;
        previousMouseY = y;

        return true;
    }
}
