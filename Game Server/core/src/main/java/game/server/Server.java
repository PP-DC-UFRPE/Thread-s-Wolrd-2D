package main.java.game.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.sun.security.auth.module.NTSystem;
import main.java.game.server.utils.ClientData;
import main.java.game.server.utils.SerializationUtils;
import main.java.game.server.utils.ServerData;

import java.util.Random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends ApplicationAdapter {
    public static final int PORT = 3000;
    public Labyrinth labyrinth;
    public List<ClientHandler> clients = new ArrayList<>();
    public Integer coinsLimit = 10;
    public ServerData serverData;
    public float cameraWidth = 1280;
    public float cameraHeight = 720;
    public ResourcesHandler resourcesHandler = null;
    public boolean endGame = false;

    public Server() {
        serverData = new ServerData(new HashMap<>());
        labyrinth = new Labyrinth(cameraWidth, cameraHeight); // Supondo que você tenha um construtor padrão para Labyrinth
        serverData.labyrinth = labyrinth;
        resourcesHandler = new ResourcesHandler();
        resourcesHandler.start();
        this.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // fica voltando para cá a cada novo accept de socket
                // System.out.println("passou do accept");
                // System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            //System.out.println("catch do server socket");
            e.printStackTrace();
        }
    }

    public class ResourcesHandler extends Thread {

        public boolean coinSent = false; // moeda já foi enviada - usar novamente para reenviar

        public ResourcesHandler() {
            System.out.println("Entrou no Resource Handler");
        }

        public boolean checkCoinFound() {
            boolean coinFound = false;
            System.out.println("checkando colisão com a Coin");
            for (Map.Entry<String, Player> entry : serverData.playersMap.entrySet()) {
                Player player = entry.getValue();
                if (player.body.x < serverData.coin.body.x + 64 &&
                        player.body.x + 64 > serverData.coin.body.x &&
                        player.body.y < serverData.coin.body.y + 64 &&
                        player.body.y + 64 > serverData.coin.body.y) {
                    // Colisão detectada
                    coinFound = true;

                    player.coins = player.coins + 1;
                    synchronized (serverData){
                        serverData.playersMap.put(player.id, player);
                    }

//                    String serverDataJson = SerializationUtils.serializeServerData(serverData);
//                    for (ClientHandler client : clients) {
//                        client.out.println(serverDataJson);
//                        // envia inclusive para quem enviou a informaçõa incial
//                    }
                    System.out.println("Coins: " + player.coins);
                    System.out.println("Colidiu com a Coin");
                    System.out.println("coin x - " + serverData.coin.body.x);
                    System.out.println("coin y - " + serverData.coin.body.y);
                }
            }
            return coinFound;
        }


        @Override
        public void run() {
            System.out.println("Entrou no while");

            while (!endGame) {
                while (serverData.coin != null) {
                    System.out.println("no checkCoinFound");
                    if (checkCoinFound()) {
                        serverData.coin = null; // Define a moeda como nula novamente
                        coinSent = false; // Define coinSent como false novamente
                        for (Player player : serverData.playersMap.values()) {
                            if (player.coins >= coinsLimit) {
                                serverData.winner = player.id;
                                String serveDataJson = SerializationUtils.serializeServerData(serverData);
                                for (ClientHandler client : clients) {
                                    client.out.println(serveDataJson);
                                }
                                endGame = true;
                            }
                        }
//                        for (ClientHandler client : clients) {
//                            String serverDataJson = SerializationUtils.serializeServerData(serverData);
//                            client.out.println(serverDataJson);
//                            // FIM DO JOGO - existe um vencedor para todos os clientes se existir um winner
//                        }
                    }
                }
                try {
                    if (!coinSent && !endGame) {
                        Thread.sleep(2000);
                        this.sendCoin();
                        coinSent = true;
                    }
                } catch (InterruptedException e) {
                    serverData.coin = null;
                    e.printStackTrace();

                }
            }
        }

        public void sendCoin() {
            Random random = new Random();
            int randomX = random.nextInt(1280 - 64);
            int randomY = random.nextInt(720 - 64);
            serverData.coin = new Coin(randomX, randomY);
            String serveDataJson = SerializationUtils.serializeServerData(serverData);
//            // enviando a todos os clients
            for (ClientHandler client : clients) {
                client.out.println(serveDataJson);
            }
        }
    }

    public class ClientHandler extends Thread {
        public Socket socket;
        public BufferedReader in;
        public PrintWriter out;
        public Player clientPlayer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                //System.out.println("ENTRO no TRY DO RUN - inicio da buffer de input e do stream de saida");
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
                // novo player do client
                Player player = new Player(labyrinth);
                this.clientPlayer = player;
                serverData.lastPlayer = player;
                // enviando primeiros dados do servidor para o client
                serverData.playersMap.put(player.id, player);// Atualiza o jogador no HashMap
                String serverDataJson = SerializationUtils.serializeServerData(serverData);
                for (ClientHandler c : clients) {
                    c.out.println(serverDataJson);// atualizando novo jogador
                }
                while (true) {
                    updateServerData();
                    if (endGame) {
                        System.out.println("Fim de jogo! Vencedor: " + serverData.winner);
                    }
                }
            } catch (IOException e) {
                // System.out.println("Caiu no catch do CLient hanler");
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(this);
                    serverData.playersMap.remove(clientPlayer.id);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateServerData() {
            try {
                if (in.ready()) {
                    String clientDataJson = in.readLine();
                    ClientData clientData = SerializationUtils.deserializeClientData(clientDataJson, ClientData.class);
                    synchronized (serverData){
                        Player p = serverData.playersMap.get(clientData.player.id);
                        clientData.player.coins = p.coins; // adcionando coins se existirem
                        serverData.playersMap.put(clientData.player.id, clientData.player);
                    }
                    String serverDataJson = SerializationUtils.serializeServerData(serverData);
                    for (ClientHandler client : clients) {
                        client.out.println(serverDataJson);
//                        System.out.println(client.getId());
                        // envia inclusive para quem enviou a informaçõa incial
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}