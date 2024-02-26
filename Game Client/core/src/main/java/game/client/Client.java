package main.java.game.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import main.java.game.client.utils.ClientData;
import main.java.game.client.utils.SerializationUtils;
import main.java.game.client.utils.ServerData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client extends ApplicationAdapter {
    private boolean gamePaused = false;
    private BitmapFont font;
    private GlyphLayout layout;
    public OrthographicCamera camera;
    public SpriteBatch batch;
    public Texture wallImage;
    public Array<Rectangle> walls = null;
    public float cameraWidth = 1280;
    public float cameraHeight = 720;

    public ClientData clientData = null;
    public Labyrinth labyrinth = null;
    public Coin coin = null;
    public Map<String, Player> playersMap = null;
    public static final int PORT = 3000;
    public static final String SERVERADRESS = "localhost";
    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;
    public String winner;

    @Override
    public void create() {
        try {
            //System.out.println("ENTRO no TRY DO CREATE");
            wallImage = new Texture(Gdx.files.internal("wall.png"));
            camera = new OrthographicCamera();
            camera.setToOrtho(false, cameraWidth, cameraHeight);
            batch = new SpriteBatch();
            font = new BitmapFont();
            layout = new GlyphLayout();

            clientData = new ClientData();
            socket = new Socket(SERVERADRESS, PORT); // Conecta ao localhost na porta 8080
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            //System.out.println("ENTROU no catch DO CREATE");
            e.printStackTrace();
        }

    }

    public void updateClientData() {
        try {
            if (in.ready()) {
                String serverDataJson = in.readLine();
                ServerData serverData = SerializationUtils
                        .deserializeServerData(serverDataJson, ServerData.class);
                if (labyrinth == null && clientData.player == null) {
                    labyrinth = serverData.labyrinth;
                    clientData.player = serverData.lastPlayer;
                }
                if (serverData.playersMap != null) {
                    playersMap = serverData.playersMap;
//                    for (Player p : serverData.playersMap.values()) {
//                        System.out.println("player.id: " + p.id);
//                    }
                    Player serverPlayer = serverData.playersMap.get(clientData.player.id);
                    if (serverPlayer != null) {
                        clientData.player.coins = serverPlayer.coins;
                    }
                }
                this.coin = serverData.coin; // objeto coin
                if (serverData.winner != null) {
                    System.out.println("Vencedor player: " + serverData.winner);
                    winner = serverData.winner;
                    gamePaused = true; // pausar o Jogo AGORA
                }
//                if (clientData.player != null) {
//                    String clientDataJson = SerializationUtils.serializeClientData(clientData);
//                    this.out.println(clientDataJson);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (clientData.player != null) {
            //System.out.println("Testando input de movimento");
            boolean moved = clientData.player.move();
            if (moved) {
                // envia dados se se moveu
                if (clientData.player.body.x < 0)
                    clientData.player.body.x = 0;
                else if (clientData.player.body.x + clientData.player.body.width > cameraWidth)
                    clientData.player.body.x = cameraWidth - clientData.player.body.width;
                if (clientData.player.body.y < 0)
                    clientData.player.body.y = 0;
                else if (clientData.player.body.y + clientData.player.body.height > cameraHeight)
                    clientData.player.body.y = cameraHeight - clientData.player.body.height;
                String clientDataJson = SerializationUtils.serializeClientData(clientData);
                out.println(clientDataJson);
            }
        }
        updateClientData();
    }

    @Override
    public void render() {
        if (!gamePaused) {
            update();
            ScreenUtils.clear(0, 0, 0.1f, 1);
            if (labyrinth != null && clientData.player != null && playersMap != null) {
                camera.update();
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
//            for (Rectangle wall : walls) {
//                batch.draw(wallImage, wall.x, wall.y);
//            }
                for (Player p : playersMap.values()) {
                    font.draw(batch, p.id, p.body.x, p.body.y + 74);
                    font.draw(batch, p.coins.toString(), p.body.x, p.body.y);
                    p.draw(batch);
                }
                if (coin != null) {
                    coin.draw(batch);
                }
                batch.end();
            }
        } else {
            ScreenUtils.clear(0, 0, 0.1f, 1);
            String endGameText;
            if (clientData.player.id.equals(winner)) {
                endGameText = "Parabéns! Você é o vencedor: " + winner;
                layout.setText(font, endGameText);
            } else {
                endGameText = "Você perdeu! o vencedor é: " + winner;
                layout.setText(font, endGameText);
            }
            System.out.println(endGameText);
            float textX = (cameraWidth - layout.width) / 2;
            float textY = (cameraHeight - layout.height) / 2;
            batch.begin();
            font.draw(batch, endGameText, textX, textY);
            batch.end();
        }
    }

    public void dispose() {
        wallImage.dispose();
        batch.dispose();
        font.dispose();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}