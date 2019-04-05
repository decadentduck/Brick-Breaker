package com.example.kaitl.brickbreaker;

public class PowerUp {

    private int x, y, speed;

    public PowerUp(int x, int y){
        this.x = x;
        this.y = y;
        this.speed = 5;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getSpeed(){return speed;}

    public void setY(int newY){
        this.y = newY;
    }
}
