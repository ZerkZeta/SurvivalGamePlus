package me.Zen3515.EssecoBank;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import me.Zen3515.EssecoBank.Exception.EssBankException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.earth2me.essentials.api.NoLoanPermittedException;

public class EssecoBankMain extends JavaPlugin implements Listener {
	
	public Logger logger = this.getLogger();
	public static EssecoBankMain plugin;
	public EssecoData EssecoDataManager;

	@Override
	public void onEnable() {
		logger = this.getLogger();
		this.saveDefaultConfig();
		EssecoDataManager = new EssecoData(this);
		getServer().getPluginManager().registerEvents(this, this);
		this.logger.info(this.getDescription().getName() + " Version " + this.getDescription().getVersion() + " Has Been Enabled!");
	}
	@Override
	public void onDisable() {
		EssecoDataManager.SaveData();
		EssecoDataManager.stop();
		this.logger.info(this.getDescription().getName() + " Version " + this.getDescription().getVersion() + " Has Been Disabled!");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onSignChangeEvent(SignChangeEvent e){
		Player player = e.getPlayer();
		//player.sendMessage("in sign place event");
		try{
			if(e.getLine(0).equals("[Bank]")){
				//player.sendMessage("in sign place event : BANK");
				if(!player.hasPermission("Bank.Admin")){
					e.setLine(0, "§4[Bank]");
					//player.sendMessage("you don't have permission");
					return;
				};
				e.setLine(0, "§1[Bank]");
				this.logger.info(player.getName() + " Has create Bank sign");
				//player.sendMessage("you can place it");
			}
		}
		catch(IndexOutOfBoundsException ex){
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent e){
		Player player = e.getPlayer();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(e.getClickedBlock().getState() instanceof Sign){
				Sign sign = (Sign)e.getClickedBlock().getState();
				try{
					if(sign.getLine(0).equals("§1[Bank]")){
						e.setCancelled(true);
						switch(sign.getLine(1)){
						case"deposit":
							BigDecimal ammount = BigDecimal.valueOf(Integer.valueOf(sign.getLine(2)));
							if(Economy.hasEnough(player.getName(), ammount)){
								try{
									EssecoDataManager.addmoney(player.getName(), ammount);
									//EssecoDataManager.setmoneyinbank(player, EssecoDataManager.getmoneyinbank(e.getPlayer()).add(ammount) );
									player.sendMessage(ChatColor.GREEN + "You have add " + ChatColor.RED + "$" + ammount + ChatColor.GREEN + " to your account, new balance in your account is " + ChatColor.RED + "$" + EssecoDataManager.getmoneyinbank(player.getName()) );
									Economy.substract(player.getName(), ammount);
								}
								catch (EssBankException e1) {
									//money has set to maximum but need to send back to player
									BigDecimal exceedamount = e1.getammount();
									player.sendMessage(ChatColor.GREEN + "You have add " + ChatColor.RED + "$" + ammount.subtract(exceedamount) + ChatColor.GREEN +  " to your account, new balance in your account is " + ChatColor.RED + "$" + EssecoDataManager.getmoneyinbank(player.getName()) + ChatColor.GREEN +  " and recive " + ChatColor.RED + "$" + exceedamount + ChatColor.GREEN +  " back");
								}
							}
							else{
								player.sendMessage(ChatColor.RED + "You don't have enought money");
							}
							break;
						case"withdraw":
							BigDecimal amount = BigDecimal.valueOf(Integer.valueOf(sign.getLine(2)));
							try{
								EssecoDataManager.takemoney(player.getName(), amount);
								Economy.add(player.getName(), amount);
								player.sendMessage(ChatColor.GREEN  + "You have took " + ChatColor.RED + "$" + amount + ChatColor.GREEN + " from your account, new balance in your account is " + ChatColor.RED + "$" + EssecoDataManager.getmoneyinbank(player.getName()));
							}
							catch (EssBankException ex){
								if(ex.getMessage().equals("don't have enough")){
									player.sendMessage(ChatColor.RED + "Your account don't have enough money");
								}
								else if(ex.getMessage().equals("your pocket is full")){
									player.sendMessage(ChatColor.RED + "your pocket is full");
								}
								else{
									player.sendMessage(ChatColor.RED + "Error please tell Zen3515");
								}
							}
							break;
						default:
							e.getPlayer().sendMessage(ChatColor.RED+"Incorrect sign");
							return;
						}
					}
					return;
				}
				catch(IndexOutOfBoundsException|NumberFormatException|UserDoesNotExistException|NoLoanPermittedException ex){
					return;
				}
			}
		}
	}
	
	@Override
    public boolean onCommand(final CommandSender sender, Command command, String CommandLable, String[] args){
		try{
			if(CommandLable.equalsIgnoreCase("Bank")){
				switch(args[0]){
				case"getmoney":
					if(sender instanceof Player){
						Player player = (Player)sender;
						player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.GREEN + "$" + EssecoDataManager.getmoneyinbank(player.getName()) + ChatColor.GOLD +  " in bank");
					}
					else{
						if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
							sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.GOLD + " have " + ChatColor.GREEN + "$" + EssecoDataManager.getmoneyinbank(Bukkit.getOfflinePlayer(args[1]).getName()) + ChatColor.GOLD +  " in bank");
						}
						else{
							sender.sendMessage(ChatColor.RED + args[1] + " isn't player");
						}
					}
					return true;
				case"money":
					if(sender instanceof Player){
						Player player = (Player)sender;
						player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.GREEN + "$" + EssecoDataManager.getmoneyinbank(player.getName()) + ChatColor.GOLD +  " in bank");
					}
					else{
						if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
							sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.GOLD + " have " + ChatColor.GREEN + "$" + EssecoDataManager.getmoneyinbank(Bukkit.getOfflinePlayer(args[1]).getName()) + ChatColor.GOLD +  " in bank");
						}
						else{
							sender.sendMessage(ChatColor.RED + args[1] + " isn't player");
						}
					}
					return true;
				case"time":
					Long timeleft =  EssecoDataManager.getTickLeft();
					sender.sendMessage(ChatColor.AQUA + "Time left before next interest: " + ChatColor.GREEN + TimeUnit.MILLISECONDS.toHours(timeleft) + " Hr " +
										TimeUnit.MILLISECONDS.toMinutes(timeleft) + " min " + (timeleft / 1000) % 60 + " sec ");
					return true;
				case"timeleft":
					Long timeleft2 =  EssecoDataManager.getTickLeft();
					sender.sendMessage(ChatColor.AQUA + "Time left before next interest: " + ChatColor.GREEN + TimeUnit.MILLISECONDS.toHours(timeleft2) + " Hr " +
										TimeUnit.MILLISECONDS.toMinutes(timeleft2) + " min " + TimeUnit.MILLISECONDS.toSeconds(timeleft2) + " sec ");
					return true;
				case"getlasttick":
					if(sender instanceof Player){
						Player player = (Player)sender;
						if(!player.hasPermission("Bank.Admin")){
							return false;
						};
					};
					sender.sendMessage("LastTick is " + EssecoDataManager.getlastTick());
					return true;
				case"getnexttick":
					if(sender instanceof Player){
						Player player = (Player)sender;
						if(!player.hasPermission("Bank.Admin")){
							return false;
						};
					};
					sender.sendMessage("time left for next tick: " + EssecoDataManager.getTickLeft()/60000 + "min");
					return true;
				default:
					return false;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException ex){
			
		}
		return false;
	}
	
}
