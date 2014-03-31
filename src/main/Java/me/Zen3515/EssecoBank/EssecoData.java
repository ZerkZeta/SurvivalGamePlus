package me.Zen3515.EssecoBank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import me.Zen3515.EssecoBank.Exception.EssBankException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class EssecoData {

	private final EssecoBankMain plugin;
	private BukkitRunnable BankTask;
	private HashMap<String,BigDecimal> BankData = new HashMap<String,BigDecimal>();
	private Long lastTick;
	public boolean isDebugging = false;
	private Long TimePeriod = 1200L; // in sec
	
	public EssecoData(EssecoBankMain pl){
		isDebugging = false;
		plugin = pl;
		BankTask = getTaskinterest();
		this.LoadData();
		Long Delay = 0L;
		if(isDebugging == false){
			if(System.currentTimeMillis() <= lastTick + (TimePeriod*1000L) && lastTick != 0L){
				Delay = ((lastTick + (TimePeriod * 1000/*to milli*/)) - System.currentTimeMillis());
				Delay = TimeUnit.MILLISECONDS.toSeconds(Delay) * 20; //convert to minecraft tick
			}
			BankTask.runTaskTimer(plugin, Delay, TimePeriod * 20); // to minecraft tick
			plugin.logger.info("new task delay for " + Delay + " Task id : " + BankTask.getTaskId() );
		}
		else{
			BankTask.runTaskTimer(plugin, Delay, 500L);
			plugin.logger.info("new task delay for " + Delay + " Task id : " + BankTask.getTaskId() );
		}
	}
	
	public void sendDebugMessage(String message){
		if(isDebugging == true && Bukkit.getPlayer("Zen3515") != null){
			Bukkit.getPlayer("Zen3515").sendMessage(message);
		}
		else{
			return;
		}
	}
	
	public void stop(){
		BankTask.cancel();
	}
	
	public Long getlastTick(){
		return lastTick;
	}
	
	public Long getTickLeft(){
		return (lastTick + (TimePeriod * 1000/*to milli*/)) - System.currentTimeMillis();
	}
	
	private BukkitRunnable getTaskinterest(){
		return new BukkitRunnable(){
			public void run(){
				lastTick = System.currentTimeMillis();
				for (Iterator<String> e = BankData.keySet().iterator(); e.hasNext();){
				      String Playername = e.next();
				      plugin.logger.info(ChatColor.GOLD + "Giving interest for " + Playername);
				      sendDebugMessage("Giving interest for " + Playername);
				      
				      if(getmoneyinbank(Playername).equals(BigDecimal.ZERO)){
				    	  plugin.logger.info("no interest for " + Playername + " cause of money in bank is 0");
				    	  //e.next();
				      }
				      
				      BigDecimal Interest = (BigDecimal.valueOf(plugin.getConfig().getDouble("interest"))).divide(BigDecimal.valueOf(100));
				      //BigDecimal newmoney = (getmoneyinbank(Playername).multiply(Interest)).divide(BigDecimal.valueOf(100));   //.add((BankData.get(Playername).multiply(BigDecimal.valueOf(plugin.getConfig().getDouble("interest"))).divide(BigDecimal.valueOf(100))));
				      
				      Interest = getmoneyinbank(Playername).multiply(Interest);
				      Interest = BigDecimal.valueOf(Math.floor(Double.valueOf(Interest.toString())));
				      
				      try {
						addmoney(Playername,Interest);
						plugin.logger.info("adding interest for " + Playername + " : $" + Interest);
				      } catch (EssBankException e1) {
				    	  sendDebugMessage("too much amount");
				    	  if(e1.getMessage().equals("too much amount")){
				    		  try {
									Economy.add(Playername, e1.getammount());
									if(Bukkit.getOfflinePlayer(Playername).isOnline()){
										BigDecimal newInterest = e1.getammount().subtract(Interest);
										Bukkit.getPlayer(Playername).sendMessage(ChatColor.GOLD + "You have earn interest for $" + newInterest);
										//Bukkit.getPlayer(Playername).sendMessage("But you have reach maximum money in your account so you recive $" + e1.getammount() + " back");
									};
				    		  } catch (NoLoanPermittedException | ArithmeticException | UserDoesNotExistException e2) {
									e2.printStackTrace();
				    		  }
				    	  }
				    	  //e.next();
				      }
				      if(Bukkit.getOfflinePlayer(Playername).isOnline()){
				    	  Bukkit.getPlayer(Playername).sendMessage(ChatColor.GOLD + "You have earn interest for $" + Interest);
				      }
				}
			}
		};
	}
	
	public void setmoneyinbank(Player player, BigDecimal ammount){
		BankData.put(player.getName(), ammount);
	}
	
	public BigDecimal getmoneyinbank(String playerName){
		if(!BankData.containsKey(playerName)){
			BankData.put(playerName, BigDecimal.ZERO);
		}
		return BankData.get(playerName);
	}
	
	public boolean isHasEnough(String playername,BigDecimal amount){
		if(this.getmoneyinbank(playername).compareTo(amount) == 0 || this.getmoneyinbank(playername).compareTo(amount) == 1){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void addmoney(String playerName,BigDecimal amount)throws EssBankException{
		if(BankData.containsKey(playerName)){			
			BigDecimal Maxmoney = BigDecimal.valueOf(plugin.getConfig().getDouble("Maximum-money"));
			sendDebugMessage(ChatColor.RED + "Maximummoney is : " + Maxmoney);
			BigDecimal newamount = amount.add(getmoneyinbank(playerName));
			sendDebugMessage(ChatColor.RED + "newamount is : " + newamount);
			if(newamount.compareTo(Maxmoney) == 1){
				sendDebugMessage(ChatColor.RED + "newamount.compareTo(Maxmoney) == 1");
				BigDecimal Exchange = newamount.subtract(Maxmoney);
				sendDebugMessage(ChatColor.RED + "Exchange is : " + Exchange);
				newamount = Maxmoney;
				BankData.put(playerName, newamount);
				throw new EssBankException("too much amount",Exchange);
			}
			else{
				BankData.put(playerName, newamount);
			}
		}
		else{
			BankData.put(playerName, amount);
		}
	}
	
	public void takemoney(String PlayerName,BigDecimal amount) throws EssBankException{
		BigDecimal PMoneyExact;
		try{
			PMoneyExact = Economy.getMoneyExact(PlayerName);
		}
		catch (UserDoesNotExistException e) {
			PMoneyExact = BigDecimal.ZERO;
		}
		sendDebugMessage(ChatColor.RED + "PMoneyExact : " + PMoneyExact);
		BigDecimal Pmoney = getmoneyinbank(PlayerName);
		sendDebugMessage(ChatColor.RED + "Pmoney : " + Pmoney);
		BigDecimal MaxPlayerMoney = BigDecimal.valueOf(plugin.getConfig().getDouble("Maxmoney-InPocket", 30000));
		sendDebugMessage(ChatColor.RED + "MaxPlayerMoney : " + MaxPlayerMoney);
		if(BankData.containsKey(PlayerName)){
			BigDecimal newamount = Pmoney.subtract(amount);
			if(newamount.compareTo(BigDecimal.ZERO) == -1){
				throw new EssBankException("don't have enough");
			};
			if(PMoneyExact.add(amount).compareTo(MaxPlayerMoney) == 1){
				throw new EssBankException("your pocket is full");
			}
			BankData.put(PlayerName, newamount);
		}
		else{
			throw new EssBankException("no user found");
		}		
	}
	
	public void LoadData(){
		if(!plugin.getDataFolder().exists())
        {
			plugin.getDataFolder().mkdir();
        }
		File Datafolder = new File(plugin.getDataFolder() + "/Data/");
		if (!Datafolder.exists()){
			Datafolder.mkdir();
		}
		File playerDataFile = new File(plugin.getDataFolder() + "/Data/" + "DATA.yml");
		if(!playerDataFile.exists()){
			try {
				playerDataFile.createNewFile();
				HashMapSaverSave(new HashMap<String,BigDecimal>(),(plugin.getDataFolder() + "/Data/" + "/DATA.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File tickfile = new File(plugin.getDataFolder() + "/Data/" + "LastTick.txt");
		if(!tickfile.exists()){
			try {
				tickfile.createNewFile();
				longSaverSave(lastTick,(plugin.getDataFolder() + "/Data/" + "LastTick.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		lastTick = longLoaderLoad(plugin.getDataFolder() + "/Data/" + "LastTick.txt");
		BankData = HashMapLoaderLoad(plugin.getDataFolder() + "/Data/" + "/DATA.yml");
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, BigDecimal> HashMapLoaderLoad(String path)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			Object result = ois.readObject();
			ois.close();
			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
			return (HashMap<String, BigDecimal>)result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new HashMap<String,BigDecimal>();
		}
	}
	private Long longLoaderLoad(String path)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			Object result = ois.readObject();
			ois.close();
			//you can feel free to cast result to HashMap<String, Integer> if you know there's that HashMap in the file
			return (Long)result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return Long.valueOf(0);
		}
	}
	private void longSaverSave(Long varible, String path){
		if(varible == null){
			varible = 0L;
		}
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(varible);
			oos.flush();
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void HashMapSaverSave(HashMap<String, BigDecimal> map, String path){
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}	
	public void SaveData(){
		if(!plugin.getDataFolder().exists())
        {
			plugin.getDataFolder().mkdir();
        }
		File Datafolder = new File(plugin.getDataFolder() + "/Data/");
		if (!Datafolder.exists()){
			Datafolder.mkdir();
		}
		File playerDataFile = new File(plugin.getDataFolder() + "/Data/" + "DATA.yml");
		if(!playerDataFile.exists()){
			try {
				playerDataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File lasttick = new File(plugin.getDataFolder() + "/Data/" + "LastTick.txt");
		if(!lasttick.exists()){
			try {
				lasttick.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		longSaverSave(lastTick,plugin.getDataFolder() + "/Data/" + "LastTick.txt");
		HashMapSaverSave(BankData,(plugin.getDataFolder() + "/Data/" + "/DATA.yml"));
	}
	
}
