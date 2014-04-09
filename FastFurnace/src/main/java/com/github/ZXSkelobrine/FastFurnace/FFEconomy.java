package com.github.ZXSkelobrine.FastFurnace;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

public class FFEconomy {
	public static Economy econ = null;

	public static boolean setupEconomy(Server server) {
		if (server.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static double getMoney(String playerName) {
		return econ.getBalance(playerName);
	}

	public static boolean depleteMoney(String playerName, double amount) {
		EconomyResponse r = econ.withdrawPlayer(playerName, amount);
		return r.transactionSuccess();
	}
}
