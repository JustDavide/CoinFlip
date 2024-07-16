package me.dovide.coinflip;

import me.dovide.coinflip.commands.CoinFlip;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class CFMain extends JavaPlugin {

    private static Economy econ = null;

    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Disabled the plugin. Didn't find Vault!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("coinflip").setExecutor(new CoinFlip(this));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = rsp.getProvider();
        return (econ != null);
    }

    public Economy getEconomy() {
        return econ;
    }
}
