package main.java.game.server.utils;

import main.java.game.server.Coin;
import main.java.game.server.Labyrinth;
import main.java.game.server.Player;

import java.util.HashMap;
import java.util.Map;

public class ServerData {
    public Map<String, Player> playersMap = new HashMap<>();
    public String winner = null;
    public Coin coin = null;
    public Labyrinth labyrinth = null;
    public Player lastPlayer = null;

    public ServerData(Map<String, Player> playersMap) {
        this.playersMap = playersMap;
    }
}
