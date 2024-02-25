package main.java.game.server.utils;

import main.java.game.server.Player;

public class ClientData {
    public Player player = null;

    public ClientData(Player player, Integer coins) {
        this.player = player;
    }
}
