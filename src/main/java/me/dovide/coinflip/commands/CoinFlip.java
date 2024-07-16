package me.dovide.coinflip.commands;

import java.util.HashMap;
import java.util.List;
import me.dovide.coinflip.CFMain;
import me.dovide.coinflip.game.FlipGame;
import me.dovide.utils.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CoinFlip implements TabExecutor {

    private final HashMap<Player, FlipGame> activeGames = new HashMap<>();
    private final CFMain instance;
    private final Economy eco;

    public CoinFlip(CFMain instance) {
        this.instance = instance;
        this.eco = instance.getEconomy();
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You have to be a player to use this command");
            return true;
        }

        Player player = (Player) commandSender;

        if (args.length == 0) {
            player.sendMessage(Util.cc("&cUsage: &7/coinflip <create|join|delete|info> [stake/player]"));
            return true;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("create")) {

            int stake;

            if (args.length != 2) {
                player.sendMessage(Util.cc("&cUsage: &7/coinflip create <stake>"));
                return true;
            }

            if (this.activeGames.containsKey(player)) {
                player.sendMessage(Util.cc("&c&lCF &7You already have an active coinflip game!"));
                return true;
            }

            try {
                stake = Integer.parseInt(args[1]);
            } catch (NumberFormatException err) {
                player.sendMessage(Util.cc("&cHas to be a number"));
                return true;
            }

            if (eco.getBalance(player) < stake) {
                player.sendMessage(Util.cc("&c&lCF &7You don't have enough coins to bet."));
                return true;
            }

            eco.withdrawPlayer(player, stake);
            FlipGame FG = new FlipGame(player, stake, instance);
            player.sendMessage(Util.cc("&a&lCF &7You created a new CoinFlip Game"));
            activeGames.put(player, FG);

        } else if (action.equalsIgnoreCase("join")) {

            if (args.length != 2) {
                player.sendMessage(Util.cc("&cUsage: &7/coinflip join <player>"));
                return true;
            }

            Player player1 = Bukkit.getPlayerExact(args[1]);

            if (player1 == null) {
                player.sendMessage(Util.cc("&cPlayer not found"));
                return true;
            }

            if (player == player1) {
                player.sendMessage(Util.cc("&cCF &7You cannot join yourself"));
                return true;
            }

            final FlipGame targetGame = activeGames.get(player1);

            if (targetGame == null) {
                player.sendMessage(Util.cc("&cCF &7That player does not have any active games"));
                return true;
            }

            if (targetGame.getGamestate() != FlipGame.GameState.WAITING) {
                player.sendMessage(Util.cc("&cCF &7You cannot enter this player's game"));
                return true;
            }

            if (eco.getBalance(player) < targetGame.getStake()) {
                player.sendMessage(Util.cc("&c&lCF &7You don't have enough coins to bet."));
                return true;
            }

            eco.withdrawPlayer(player, targetGame.getStake());
            targetGame.addPlayer(player);

            new BukkitRunnable() {
                public void run() {
                    activeGames.remove(player1, targetGame);
                }
            }.runTaskLater(instance, 100L);

        } else if (action.equalsIgnoreCase("delete")) {

            final FlipGame targetGame = activeGames.get(player);

            if (targetGame == null) {
                player.sendMessage(Util.cc("&c&lCF &7You don't have games to delete"));
                return true;
            }

            if (targetGame.getGamestate() == FlipGame.GameState.FLIPPING) {
                player.sendMessage(Util.cc("&a&lCF &7You can't delete a game that's starting..."));
                return true;
            }

            player.sendMessage(Util.cc("&a&lCF &7Deleted your game. You have been refunded"));
            eco.depositPlayer(player, targetGame.getStake());
            activeGames.remove(player, targetGame);

        } else if (action.equalsIgnoreCase("info")) {

            if (args.length != 2) {
                player.sendMessage(Util.cc("&c&lCF &7Usage: /coinflip info <Player>"));
                return true;
            }

            Player player2 = Bukkit.getPlayerExact(args[1]);

            if (player2 == null) {
                player.sendMessage(Util.cc("&c&lCF &7Player not found"));
                return true;
            }

            final FlipGame targetGame = this.activeGames.get(player2);

            if (targetGame == null) {
                player.sendMessage(Util.cc("&c&lCF &7This player does not have any active games"));
                return true;
            }

            player.sendMessage(Util.format("&7-- %s CoinFlip --", player2.getName()));
            player.sendMessage(Util.cc("&aStake: &7" + targetGame.getStake()));
            player.sendMessage(Util.cc("&aState: &7" + targetGame.getGamestate()));
            player.sendMessage(Util.cc("&aPossible Win: &7" + targetGame.getWin()));
            player.sendMessage(Util.cc("&7------------------"));
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 1)
            return List.of("create", "join", "delete", "info");
        if (args.length == 2)
            return null;
        return List.of();
    }
}
