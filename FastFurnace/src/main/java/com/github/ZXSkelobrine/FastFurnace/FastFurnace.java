package com.github.ZXSkelobrine.FastFurnace;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FastFurnace extends JavaPlugin {
	private final Logger log = getLogger();
	public static Economy econ = null;

	private final int COOKING = 0;
	private final int BURNER = 1;
	private final int OUTPUT = 2;

	@Override
	public void onEnable() {
		if (!FFEconomy.setupEconomy(getServer())) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	public void onDisable() {

	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("fastfurnace") || command.getName().equalsIgnoreCase("ff")) {
			getLogger().info("1");
			if (sender instanceof Player) {
				Player player = (Player) sender;
				Block block = player.getTargetBlock(null, 20);
				getLogger().info("2");
				log.info(block.getType().name());
				if (block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE)) {
					getLogger().info("3");
					Furnace furnace = (Furnace) block.getState();
					FurnaceInventory fi = furnace.getInventory();
					ItemStack[] iss = fi.getContents();
					List<ItemStack> contents = Arrays.asList(iss);
					fi.setResult(new ItemStack(contents.get(OUTPUT).getType(), contents.get(OUTPUT).getAmount() + 8));
					fi.setSmelting(new ItemStack(contents.get(COOKING).getType(), contents.get(COOKING).getAmount() - 8));
					Material fuel = contents.get(BURNER).getType();
					switch (fuel) {
					case COAL:
						fi.setFuel(new ItemStack(fuel, contents.get(BURNER).getAmount() - 1));
						break;
					}
					FFEconomy.depleteMoney(player.getName(), 100D);
					return true;
				}
			}
		}
		return false;
	}

}
