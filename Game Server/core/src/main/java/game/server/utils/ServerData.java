package main.java.game.server.utils;

import main.java.game.server.Coin;
import main.java.game.server.Labyrinth;
import main.java.game.server.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class ServerData {
    public Map<String, Player> playersMap;
    public String winner = null;
    public Coin coin = null;
    public Labyrinth labyrinth = null;
    public Player lastPlayer = null;

    public ServerData(ConcurrentMap<String, Player> playersMap) {
        this.playersMap = playersMap;
    }
}
