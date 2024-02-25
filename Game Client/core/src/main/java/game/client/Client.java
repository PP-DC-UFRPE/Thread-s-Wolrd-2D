package main.java.game.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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

    @Override
    public void create() {
        try {
            //System.out.println("ENTRO no TRY DO CREATE");
            wallImage = new Texture(Gdx.files.internal("wall.png"));
            camera = new OrthographicCamera();
            camera.setToOrtho(false, cameraWidth, cameraHeight);
            batch = new SpriteBatch();

            clientData = new ClientData();
            socket = new Socket(SERVERADRESS, PORT); // Conecta ao localhost na porta 8080
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            //System.out.println("ENTROU no catch DO CREATE");
            e.printStackTrace();
        }

    }

    public void createLabyrinth() {
        try {
            if (in.ready()) {
                String Labyrinthjson = in.readLine();
                if (Labyrinthjson == null) return;
                Labyrinth obj = SerializationUtils.deserializeLabyrinth(Labyrinthjson, Labyrinth.class);
                if (obj == null) return;
                //System.out.println("Labirinto chegou: " + obj);
                labyrinth = obj;
//                walls = new Array<>();
//                for (int x = 0; x < 12; x++) {
//                    for (int y = 0; y < 7; y++) {
//                        if (labyrinth.walls[x][y]) {
//                            Rectangle wall = new Rectangle();
//                            wall.x = x * 100;
//                            wall.y = x * 100;
//                            wall.width = labyrinth.wallsWidth;
//                            wall.height = labyrinth.wallsHeight;
//                            walls.add(wall);
//                        }
//                    }
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer() {
        try {
            if (in.ready()) {
                String playerJson = in.readLine();
                if (playerJson == null) return;
                Player obj = SerializationUtils.deserializePlayer(playerJson, Player.class);
                if (obj == null) return;
                //System.out.println("Player chegou: " + obj);
                clientData.player = obj;
                // a lista de players sera atualizada pelo server, e não pelo Client que receberá em UpdatePlayers
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateClientData() {
        try {
            if (in.ready()) {
                String serverDataJson = in.readLine();
                ServerData serverData = SerializationUtils
                        .deserializeServerData(serverDataJson, ServerData.class);
                if(serverData.playersMap!=null){
                    playersMap = serverData.playersMap;
                }
                if(serverData.playersMap.get(clientData.player.id)!=null) {
                    System.out.println("PLAYER diferente de null"+ clientData.player.id);
                    Player serverPlayer = serverData.playersMap.get(clientData.player.id);
                    clientData.player.coins = serverPlayer.coins;
                }
                this.coin = serverData.coin; // objeto coin
            }
            if(clientData.player!=null){
                String clientDataJson = SerializationUtils.serializeClientData(clientData);
                this.out.println(clientDataJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // roda apenas no inicio
        if (labyrinth == null || clientData.player == null || playersMap == null) {
            if (labyrinth == null) {
                // PRIMEIRO dado enviado no create do server
                createLabyrinth();
            }
            if (clientData.player == null) {
                // SEGUNDO dado enviado no create do server - Player com id da THREAD do server
                createPlayer();
            }
            if (playersMap == null) {
                // TERCEIRO dado enviado no create do server - HashMap com Players atualizando players no client
                //updatePlayers();
                updateClientData();
            }
        }
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
                // clientData.player.body = player.body;
                //String playerUpdated = SerializationUtils.serializePlayer(player);
                //out.println(playerUpdated);
                //System.out.println("Player enviou posição atual");
            }
        }
        if (playersMap != null)
            updateClientData();
    }

    @Override
    public void render() {
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
                p.draw(batch);
            }
            if (coin != null) {
                coin.draw(batch);
            }
            batch.end();
        }
    }

    public void dispose() {
        wallImage.dispose();
        batch.dispose();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}