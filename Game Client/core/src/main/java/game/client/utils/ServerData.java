package main.java.game.client.utils;

import main.java.game.client.Player;

import java.util.HashMap;
import java.util.Map;

public class ServerData {
    public Map<String, Player> playersMap = new HashMap<>();
    public String winner = null;

    public ServerData(Map<String, Player> playersMap) {
        this.playersMap = playersMap;
    }
}
