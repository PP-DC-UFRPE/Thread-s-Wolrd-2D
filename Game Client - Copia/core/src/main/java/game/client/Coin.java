package main.java.game.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.annotations.Expose;

public class Coin {
    @Expose(deserialize = true)
    public Vector3 position = new Vector3(0, 0, 0);
    @Expose(deserialize = true)
    public Rectangle body;

    public Coin() {
        this.body = new Rectangle();
        this.body.x = position.x;
        this.body.y = position.y;
        this.body.width = 64;
        this.body.height = 64;
    }


    public void draw(SpriteBatch batch) {
        batch.draw(new Texture(Gdx.files.internal("coin.png")), this.body.x, this.body.y);
    }
}