package main.java.game.client;

import com.badlogic.gdx.math.Vector3;
import com.google.gson.annotations.Expose;

import java.util.Random;

public class Labyrinth {

    @Expose(deserialize = true)
    public boolean[][] walls;

    @Expose(deserialize = true)
    public int wallsWidth = 64, wallsHeight = 64;

    public Labyrinth(float width, float height) {
//        walls = new boolean[(int) width / 100][(int) height / 100];
//        // Crie um padrão em zigue-zague
//        Random random = new Random();
//
//        for (int i = 0; i < 15; i++) {
//            int randomX = random.nextInt((int) width / 100);
//            int randomY = random.nextInt((int) height / 100);
//            walls[randomX][randomY] = true;
//        }
    }

    public Vector3 randomPosition() {
        Random random = new Random();
        int randomX = random.nextInt(1280 - 64) + 64;
        int randomY = random.nextInt(720 - 64) + 64;
        return new Vector3(randomX, randomY, 0);
    }
}