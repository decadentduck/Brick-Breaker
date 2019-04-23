package com.example.kaitl.brickbreaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

class GameView extends SurfaceView
{
    private SurfaceHolder holder;

    private boolean running = true;
    private String winner;

    private int ballX = 300;
    private int ballY = 300;
    private int xspeed = 10;
    private int yspeed = 10;
    private int radius = 20;

    private Brick[] bricks;
    private int brickHeight = 40;
    private int brickWidth = 80;

    private int paddleX = 0;
    private int paddleY = 0;
    private int paddleHeight = 80;
    private int paddleWidth = 160;
    private int paddleSpeed = 20;

    private int level = 1;
    private int maxLevel = 3;

    private float previousMouseX;
    private float previousMouseY;

    private PowerUp powerup;

    MediaPlayer music;
    MediaPlayer soundEffect;

    private GameThread gameThread;

    private float timer;
    private float timerMax = 10000;
    long last_time = System.nanoTime();

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

        music = MediaPlayer.create(context, R.raw.backgroundmusic);
        soundEffect = MediaPlayer.create(context, R.raw.blipsoundeffect);
        music.setLooping(true);
        music.start();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(running) {
            update();

            checkCollision(canvas);

            //draw the game
            if (canvas != null) {
                //canvas colours
                canvas.drawColor(Color.BLUE);
                Paint paint = new Paint();

                //draw the ball
                paint.setColor(Color.GRAY);
                canvas.drawCircle(ballX, ballY, radius, paint);
                Log.i("debug", "onDraw: ball drawn");

                //draw paddle
                paint.setColor(Color.GREEN);
                canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);

                //draw bricks
                for (int i = 0; i < bricks.length; i++) {
                    if (bricks[i].getHealth() > 0) {
                        if (bricks[i].getHealth() == 1)
                            paint.setColor(Color.YELLOW);
                        else if (bricks[i].getHealth() == 2)
                            paint.setColor(Color.RED);
                        canvas.drawRect(bricks[i].getX(), bricks[i].getY(),
                                bricks[i].getX() + brickWidth, bricks[i].getY() + brickHeight, paint);
                        Log.i("debug", "onDraw: brick drawn");
                    }
                }

                //draw powerup if there is one active, a green sphere the size of the ball
                if (powerup != null) {
                    paint.setColor(Color.GREEN);
                    canvas.drawCircle(powerup.getX(), powerup.getY(), 20, paint);
                }
            }
        }
        else
        {
            //game is over
            canvas.drawColor(Color.BLACK);
            Paint paint = new Paint();
            canvas.drawPaint(paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(200);
            canvas.drawText(winner, 50, canvas.getHeight() / 2, paint);

            long time = System.nanoTime();
            int delta_time = (int) ((time - last_time) / 1000000);
            last_time = time;
            timer -= delta_time;

            if(timer < 0)
            {
                winner = "";
                level = 1;
                running = true;
                Setup();
            }
        }
    }

    private void Setup()
    {
        timer = timerMax;
        ballY = 300;
        ballX = 300;

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
            Log.i("debug", "Setup: level 2");
            bricks = new Brick[10];
            for(int i = 0; i < bricks.length; i++)
            {
                if(i < 5){
                    bricks[i] = new Brick(100, i * 200, 1);
                }
                else {
                    bricks[i] = new Brick(1000, (i - 5) * 200, 1);
                }

            }
        }
        else if (level == 3){
            Log.i("debug", "Setup: level 3");
            bricks = new Brick[10];
            for(int i = 0; i < bricks.length; i++) {
                if (i < 5) {
                    bricks[i] = new Brick(i * 100, i * 100, 1);
                } else {
                    bricks[i] = new Brick(1000, (i - 5) * 200, 1);
                }
            }
        }
    }

    private void update()
    {
        //move ball
        ballX += xspeed;
        ballY += yspeed;

        //move paddle
        if(powerup != null)
        {
            int posY = powerup.getY() - powerup.getSpeed();
            powerup.setY(posY);
        }
    }
    private void checkCollision(Canvas canvas)
    {

        //check for off screen
        if(ballX < 0 || ballX + radius >= canvas.getWidth())
        {
            xspeed*=-1;
            soundEffect.start();
        }
        if(ballY < 0 )
        {
            yspeed*=-1;
            soundEffect.start();
        }
        else if(ballY + radius >= canvas.getHeight())
            GameOver(false);

        //paddle off screen
        if(paddleX < 0)
            paddleX = 0;
        else if(paddleX + paddleWidth > canvas.getWidth())
            paddleX = canvas.getWidth() - paddleWidth;

        //check for paddle
        int DeltaX = ballX - max(paddleX, min(ballX, paddleX + paddleWidth));
        int DeltaY = ballY - max(paddleY, min(ballY, paddleY + paddleWidth));

        if((DeltaX * DeltaX + DeltaY * DeltaY) < (radius * radius))
        {
            if (Math.abs(ballY - paddleY) < radius)
                yspeed *= -1;
             else if (Math.abs(ballY - paddleY + paddleHeight) < radius)
                 yspeed *= -1;
            else if (Math.abs(ballX - paddleX) < radius)
                xspeed *= -1;
            else if (Math.abs(ballX - paddleX + paddleWidth) < radius)
                xspeed *= -1;

            soundEffect.start();
        }

        //keep paddle Y constant
        if (paddleY == 0)
            paddleY = canvas.getHeight() - 30;

        //check for each brick
        for(int i = 0; i < bricks.length; i++)
        {
            DeltaX = ballX - max(bricks[i].getX(), min(ballX, bricks[i].getX() + brickWidth));
            DeltaY = ballY - max(bricks[i].getY(), min(ballY, bricks[i].getY() + brickHeight));

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

                    if (bricks[i].getHealth() > 0)
                    {
                        Random rand = new Random();
                        int spawnChance = rand.nextInt(15)+1;
                        if(spawnChance == 15){
                            //spawn a powerup
                            powerup = new PowerUp(bricks[i].getX(), bricks[i].getY());
                        }
                    }

                    soundEffect.start();
                }
            }
        }

        //check for powerup
        if(powerup != null)
        {
            if (Math.abs(powerup.getY() - paddleHeight) < radius || Math.abs(powerup.getY() - paddleWidth) < radius || Math.abs(powerup.getX() - paddleWidth) < radius || Math.abs(powerup.getX() - paddleHeight) < radius) {
                paddleWidth += 10;
                powerup = null;
            } else if (powerup.getY() + radius >= canvas.getHeight()) {
                powerup = null;
            }
        }

        if(levelOver()){NextLevel();}
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

    private void NextLevel()
    {
        paddleWidth = 160;
        level++;
        if (level <= maxLevel)
            Setup();
        else
            GameOver(true);
    }

    private void GameOver(boolean win)
    {
        if(win)
            winner = "You Win!";
        else
            winner = "You Lose!";

        running = false;

        music.stop();
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
