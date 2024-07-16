package me.dovide.coinflip.game;

import java.util.Random;
import me.dovide.coinflip.CFMain;
import me.dovide.utils.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FlipGame {

    private final CFMain instance;
    private final Economy eco;
    Player player1;
    Player player2;
    GameState gamestate;
    Player winner;
    int stake;

    public FlipGame(Player player1, int stake, CFMain instance) {
        this.player1 = player1;
        this.stake = stake;
        this.gamestate = GameState.WAITING;
        this.instance = instance;
        this.eco = instance.getEconomy();
    }

    public int getWin() {
        return this.stake * 2;
    }

    private Player getWinner() {
        Random flip = new Random();
        boolean flipped = flip.nextBoolean();
        return flipped ? this.player1 : this.player2;
    }

    public void addPlayer(Player addedPlayer) {
        this.player2 = addedPlayer;
        this.gamestate = GameState.FLIPPING;
        this.player1.sendMessage(Util.format("&a&lCF &7%s joined your coinflip."), this.player2.getName());
        this.player2.sendMessage(Util.format("&a&lCF &7You joined %s coinflip"), this.player1.getName());
        startCountdown();
    }

    public void startCountdown() {
        new BukkitRunnable() {
            int countdown = 5;

            public void run() {
                if (this.countdown > 0) {
                    player1.sendMessage(Util.format("&a&lCF &7Coinflip starting in &a%s", this.countdown));
                    player2.sendMessage(Util.format("&a&lCF &7Coinflip starting in &a%s", this.countdown));
                    this.countdown--;
                } else {
                    flip();
                    cancel();
                }
            }
        }.runTaskTimer(this.instance, 0L, 20L);
    }

    public void flip() {
        this.winner = getWinner();
        this.player1.sendMessage(Util.format("&a&lCF %s won the coinflip", this.winner.getName()));
        this.player2.sendMessage(Util.format("&a&lCF %s won the coinflip", this.winner.getName()));
        this.eco.depositPlayer(this.winner, this.getWin());
        this.winner.sendMessage(Util.format("&aCF &7You won &s", this.getWin()));
    }

    public double getStake() {
        return this.stake;
    }

    public GameState getGamestate() {
        return this.gamestate;
    }

    public enum GameState {
        WAITING, FLIPPING
    }
}
