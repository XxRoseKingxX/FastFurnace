package com.github.ZXSkelobrine.FastFurnace;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.print.DocFlavor.CHAR_ARRAY;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class FastFurnace extends JavaPlugin {
	private final Logger log = getLogger();
	public static Economy econ = null;
	public Thread cooldown = new Thread("Cooldown Thread") {
		@Override
		public void run() {
			while (true) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.hasMetadata("cooldown")) {
						if (p.getMetadata("cooldown").get(0).asInt() > 0) {
							p.setMetadata("cooldown", new FixedMetadataValue(FastFurnace.this, p.getMetadata("cooldown").get(0).asInt() - 1));
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private final int COOKING = 0;
	private final int BURNER = 1;
	private final int OUTPUT = 2;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		cooldownThread();
		if (!FFEconomy.setupEconomy(getServer())) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	public void onDisable() {
		cooldown.interrupt();
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("fastfurnace") || command.getName().equalsIgnoreCase("ff")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				Block block = player.getTargetBlock(null, 20);
				if (block.getType().equals(Material.FURNACE) || block.getType().equals(Material.BURNING_FURNACE)) {
					if (player.hasMetadata("cooldown")) {
						if (!(player.getMetadata("cooldown").get(0).asInt() > 0)) {
							Location bloc = block.getLocation();
							bloc.setY(block.getY() + 1);
							if (player.getWorld().getBlockAt(bloc).getType().equals(Material.IRON_BLOCK)) {
								this.reloadConfig();
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
								player.setMetadata("cooldown", new FixedMetadataValue(this, this.getConfig().getInt("cooldown")));
								om("Done!", player);
							} else {
								om("Sorry, you must have an iron block on top of your furnace to do that!", player);
							}
						} else {
							om("Sorry - you cant do that. Please wait " + player.getMetadata("cooldown").get(0).asInt() + " seconds before doing that command again.", player);
						}
					} else {
						player.setMetadata("cooldown", new FixedMetadataValue(this, 0));
						om("Blast! Something went wrong. Please try the command again. Dont worry no money has been taken!", player);
					}
				} else {
					om("You must be looking at a furnace to do that!", player);
				}
				return true;
			} else {
				log.info("Sorry you must be a player to do that");
			}
		}
		return false;
	}

	public void cooldownThread() {
		cooldown.start();
	}

	public void om(String message, Player player) {
		player.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "[FF]" + ChatColor.RESET + ChatColor.GREEN + message);
	}

}
