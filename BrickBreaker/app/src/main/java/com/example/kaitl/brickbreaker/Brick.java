package com.example.kaitl.brickbreaker;

public class Brick
{
    private int x, y, health;

    public Brick(int x, int y, int health)
    {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getHealth() { return health; }

    public void setHealth(int health) { this.health = health; }

}
