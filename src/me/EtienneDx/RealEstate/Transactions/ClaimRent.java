package me.EtienneDx.RealEstate.Transactions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import me.EtienneDx.RealEstate.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;

import me.EtienneDx.RealEstate.RealEstate;
import me.EtienneDx.RealEstate.Utils;
import net.md_5.bungee.api.ChatColor;
import org.checkerframework.checker.units.qual.A;

public class ClaimRent extends BoughtTransaction
{
	LocalDateTime startDate = null;
	int duration;
	public boolean autoRenew = false;
	public boolean buildTrust = true;
	public int periodCount = 0;
	public int maxPeriod;
	
	public ClaimRent(Map<String, Object> map)
	{
		super(map);
		if(map.get("startDate") != null)
			startDate = LocalDateTime.parse((String) map.get("startDate"), DateTimeFormatter.ISO_DATE_TIME);
		duration = (int)map.get("duration");
		autoRenew = (boolean) map.get("autoRenew");
		periodCount = (int) map.get("periodCount");
		maxPeriod = (int) map.get("maxPeriod");
		try {
			buildTrust = (boolean) map.get("buildTrust");
		}
		catch (Exception e) {
			buildTrust = true;
		}
	}
	
	public ClaimRent(Claim claim, Player player, double price, Location sign, Location insideBlock, int duration, int rentPeriods, boolean buildTrust)
	{
		super(claim, player, price, sign, insideBlock);
		this.duration = duration;
		this.maxPeriod = RealEstate.instance.config.cfgEnableRentPeriod ? rentPeriods : 1;
		this.buildTrust = buildTrust;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();

		if(startDate != null)
			map.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME));
		map.put("duration", duration);
		map.put("autoRenew",  autoRenew);
		map.put("periodCount", periodCount);
		map.put("maxPeriod", maxPeriod);
		map.put("buildTrust", buildTrust);
		
		return map;
	}

	@Override
	public boolean update()
	{
		final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);
		if(buyer == null)
		{
			if(sign.getBlock().getState() instanceof Sign)
			{
				Sign s = (Sign) sign.getBlock().getState();
				s.setLine(0, RealEstate.instance.config.cfgSignsHeader);
				s.setLine(1, ChatColor.DARK_GREEN + RealEstate.instance.config.cfgReplaceRent);
				//s.setLine(2, owner != null ? Bukkit.getOfflinePlayer(owner).getName() : "SERVER");
				String price_line = "";
				if(RealEstate.instance.config.cfgUseVaultCurrencyFormat){
					price_line = RealEstate.econ.format(price);
				}
				else if(RealEstate.instance.config.cfgUseCurrencySymbol)
				{
					if(RealEstate.instance.config.cfgUseDecimalCurrency == false)
					{
						price_line = RealEstate.instance.config.cfgCurrencySymbol + " " + (int)Math.round(price);
					}
					else
					{
						price_line = RealEstate.instance.config.cfgCurrencySymbol + " " + price;
					}

				}
				else
				{
					if(RealEstate.instance.config.cfgUseDecimalCurrency == false)
					{
						price_line = (int)Math.round(price) + " " + RealEstate.econ.currencyNamePlural();
					}
					else
					{
						price_line = price + " " + RealEstate.econ.currencyNamePlural();
					}
				}
				String period = (maxPeriod > 1 ? maxPeriod + "x " : "") + Utils.getTime(duration, null, false);
				if(this.buildTrust) {
					s.setLine(2, price_line);
					s.setLine(3, period);
				} else {
					s.setLine(2, RealEstate.instance.config.cfgContainerRentLine);
					s.setLine(3, price_line + " - " + period);
				}
				s.update(true);
				final Component mainTitle = Component.text("For Rent!", NamedTextColor.GREEN);
				final Component subTitle = Component.text(RealEstate.econ.format(price), NamedTextColor.GREEN);
				final Title newTitle = Title.title(mainTitle, subTitle);
				claim.getData().setEnterTitle(newTitle);
				claim.getData().setDisplayName(mainTitle.append(Component.text(" - ", NamedTextColor.GREEN)).append(subTitle));


			}
			else
			{
				return true;
			}
		}
		else
		{
			// we want to know how much time has gone by since startDate
			int days = Period.between(startDate.toLocalDate(), LocalDate.now()).getDays();
			Duration hours = Duration.between(startDate.toLocalTime(), LocalTime.now());
			if(hours.isNegative() && !hours.isZero())
	        {
	            hours = hours.plusHours(24);
	            days--;
	        }
			if(days >= duration)// we exceeded the time limit!
			{
				payRent();
			}
			else
			{
				if(sign.getBlock().getState() instanceof Sign){
					Sign s = (Sign) sign.getBlock().getState();
					s.setLine(0, ChatColor.GOLD + RealEstate.instance.config.cfgReplaceOngoingRent); //Changed the header to "[Rented]" so that it won't waste space on the next line and allow the name of the player to show underneath.
					s.setLine(1, Utils.getSignString(Bukkit.getOfflinePlayer(buyer).getName()));//remove "Rented by"
					s.setLine(2, "Time remaining : ");

					int daysLeft = duration - days - 1;// we need to remove the current day
					Duration timeRemaining = Duration.ofHours(24).minus(hours);

					s.setLine(3, Utils.getTime(daysLeft, timeRemaining, false));
					s.update(true);
					try {
						final Component mainTitle = Component.text(Bukkit.getOfflinePlayer(buyer).getName() + "'s rental!", NamedTextColor.AQUA);
						final Component subTitle = Component.text(RealEstate.econ.format(price), NamedTextColor.GREEN);
						final Title newTitle = Title.title(mainTitle, subTitle);
						claim.getData().setEnterTitle(newTitle);
						claim.getData().setDisplayName(mainTitle.append(Component.text(" - ", NamedTextColor.DARK_GRAY)).append(subTitle));
					} catch (Exception ignored){}
				} else {
					unRent(true);
				}
			}
		}
		return false;
		
	}

	private void unRent(boolean msgBuyer)
	{
		final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);
		claim.removeUserTrust(buyer, TrustTypes.NONE);
		friends.forEach(a -> {
			claim.removeUserTrust(a, TrustTypes.NONE);
		});
		if(msgBuyer && Bukkit.getOfflinePlayer(buyer).isOnline() && RealEstate.instance.config.cfgMessageBuyer)
		{
			Bukkit.getPlayer(buyer).sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + 
					"The rent for the " + (claim.getParent() == null ? "claim" : "subclaim") + " at " + ChatColor.BLUE + "[" + 
					sign.getWorld().getName() + ", X: " + sign.getBlockX() + ", Y: " + 
					sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + ChatColor.AQUA + " is now over, your access has been revoked.");
		}
		buyer = null;
		friends = new ArrayList<>();

		RealEstate.transactionsStore.saveData();
		update();
	}

	private void payRent()
	{
		if(buyer == null) return;

		OfflinePlayer buyerPlayer = Bukkit.getOfflinePlayer(this.buyer);
		OfflinePlayer seller = owner == null ? null : Bukkit.getOfflinePlayer(owner);

		final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);
		String claimType = claim.getParent() == null ? "claim" : "subclaim";

		if((autoRenew || periodCount < maxPeriod) && Utils.makePayment(owner, this.buyer, price, false, false))
		{
			periodCount = (periodCount + 1) % maxPeriod;
			startDate = LocalDateTime.now();

			if(buyerPlayer.isOnline() && RealEstate.instance.config.cfgMessageBuyer)
			{
				((Player)buyerPlayer).sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + 
						"Paid rent for the " + claimType + " at " + ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + sign.getBlockX() + 
						", Y: " + sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + 
						ChatColor.AQUA + "for the price of " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
			}
			else if(RealEstate.instance.config.cfgMailOffline && RealEstate.ess != null)
        	{
        		User u = RealEstate.ess.getUser(this.buyer);
        		u.addMail(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + 
						"Paid rent for the " + claimType + " at " + ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + sign.getBlockX() + 
						", Y: " + sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + 
						ChatColor.AQUA + "for the price of " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
        	}
			
			if(seller != null)
			{
				if(seller.isOnline() && RealEstate.instance.config.cfgMessageOwner)
				{
					((Player)seller).sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + buyerPlayer.getName() + 
							" has paid rent for the " + claimType + " at " + ChatColor.BLUE + "[" + 
							sign.getWorld().getName() + ", X: " + sign.getBlockX() + ", Y: " + 
							sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + 
							ChatColor.AQUA + "at the price of " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
				}
				else if(RealEstate.instance.config.cfgMailOffline && RealEstate.ess != null)
	        	{
	        		User u = RealEstate.ess.getUser(this.owner);
	        		u.addMail(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + buyerPlayer.getName() + 
							" has paid rent for the " + claimType + " at " + ChatColor.BLUE + "[" + 
							sign.getWorld().getName() + ", X: " + sign.getBlockX() + ", Y: " + 
							sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + 
							ChatColor.AQUA + "at the price of " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
	        	}
			}
			
		}
		else if (autoRenew)
		{
			if(buyerPlayer.isOnline() && RealEstate.instance.config.cfgMessageBuyer)
			{
				((Player)buyerPlayer).sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + 
						"Couldn't pay the rent for the " + claimType + " at " + ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + 
						sign.getBlockX() + ", Y: " + 
						sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + ChatColor.RED + ", your access has been revoked.");
			}
			else if(RealEstate.instance.config.cfgMailOffline && RealEstate.ess != null)
        	{
        		User u = RealEstate.ess.getUser(this.buyer);
        		u.addMail(RealEstate.instance.config.chatPrefix + ChatColor.RED + 
						"Couldn't pay the rent for the " + claimType + " at " + ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + 
						sign.getBlockX() + ", Y: " + 
						sign.getBlockY() + ", Z: " + sign.getBlockZ() + "]" + ChatColor.RED + ", your access has been revoked.");
        	}
			unRent(false);
			return;
		}
		else
		{
			unRent(true);
			return;
		}
		update();
		RealEstate.transactionsStore.saveData();
	}

	@Override
	public boolean tryCancelTransaction(Player p, boolean force)
	{
		if(buyer != null)
		{
			if(p.hasPermission("realestate.admin") && force == true)
			{
				this.unRent(true);
				RealEstate.transactionsStore.cancelTransaction(this);
				return true;
			}
			else
			{
				final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);
				if(p != null)
					p.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "This " + (claim.getParent() == null ? "claim" : "subclaim") + 
	            		" is currently rented, you can't cancel the transaction!");
	            return false;
			}
		}
		else
		{
			RealEstate.transactionsStore.cancelTransaction(this);
			return true;
		}
	}

	@Override
	public void interact(Player player)
	{
		final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);// getting by id creates errors for subclaims
		if(claim == null || claim.isWilderness())
		{
            player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "This claim does not exist!");
            RealEstate.transactionsStore.cancelTransaction(claim);
            return;
		}
		String claimType = claim.getParent() == null ? "claim" : "subclaim";

		if (owner != null && owner.equals(player.getUniqueId()))
        {
            player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You already own this " + claimType + "!");
            return;
        }
		if(claim.getParent() == null && owner != null && !owner.equals(claim.getOwnerUniqueId()))
		{
            player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + Bukkit.getOfflinePlayer(owner).getName() + 
            		" does not have the right to rent this " + claimType + "!");
            RealEstate.transactionsStore.cancelTransaction(claim);
            return;
		}
		if(!player.hasPermission("realestate." + claimType + ".rent"))
		{
            player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You do not have the permission to rent " + 
            		claimType + "s!");
            return;
		}
		if(player.getUniqueId().equals(buyer))
		{
			if(!RealEstate.instance.config.cfgEnableAutoRenew)
			{
				player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "Automatic renew is disabled!");
				return;
			}
			else
			{
				autoRenew = !autoRenew;
				RealEstate.transactionsStore.saveData();
				Messages.sendMessage(player, RealEstate.instance.messages.msgRenewRentNow, autoRenew ? "enabled" : "disabled", claimType);
			}
            return;
		}
		if(buyer != null)
		{
            player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "Someone already rents this " + 
            		claimType + "!");
            return;
		}
		if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax+5 && player.hasPermission("realestate.extrarental.five"))
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax+5 + " plots!");
			return;
		}
		else if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax+4 && player.hasPermission("realestate.extrarental.four"))
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax+4 + " plots!");
			return;
		}
		else if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax+3 && player.hasPermission("realestate.extrarental.three"))
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax+3 + " plots!");
			return;
		}
		else if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax+2 && player.hasPermission("realestate.extrarental.two"))
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax+2 + " plots!");
			return;
		}
		else if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax+1 && player.hasPermission("realestate.extrarental.one"))
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax+1 + " plots!");
			return;
		}
		else if(RealEstate.transactionsStore.getTransactionsRentals(player) >= RealEstate.instance.config.cfgRentMax)
		{ // doesnt exceed rental limit
			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.RED + "You have hit your max rentals of " +
					RealEstate.instance.config.cfgRentMax + " plots!");
			return;
		}
		if(Utils.makePayment(owner, player.getUniqueId(), price, false, true))
			// if payment succeed
		{
			buyer = player.getUniqueId();
			startDate = LocalDateTime.now();
			autoRenew = false;
			TrustType trustType = buildTrust ? TrustTypes.BUILDER : TrustTypes.CONTAINER;
			claim.addUserTrust(buyer, trustType);



			update();
			RealEstate.transactionsStore.saveData();

			RealEstate.instance.addLogEntry(
                    "[" + RealEstate.transactionsStore.dateFormat.format(RealEstate.transactionsStore.date) + "] " + player.getName() +
                    " has rented a " + claimType + " at " +
                    "[" + player.getLocation().getWorld() + ", " +
                    "X: " + player.getLocation().getBlockX() + ", " +
                    "Y: " + player.getLocation().getBlockY() + ", " +
                    "Z: " + player.getLocation().getBlockZ() + "] " +
                    "Price: " + price + " " + RealEstate.econ.currencyNamePlural());

			if(owner != null)
			{
				OfflinePlayer seller = Bukkit.getOfflinePlayer(owner);

				if(RealEstate.instance.config.cfgMessageOwner && seller.isOnline())
				{
					((Player)seller).sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.GREEN + player.getName() + ChatColor.AQUA +
							" has just rented your " + claimType + " at " +
							ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + sign.getBlockX() + ", Y: " + sign.getBlockY() + ", Z: "
							+ sign.getBlockZ() + "]" + ChatColor.AQUA +
	                        " for " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
				}
				else if(RealEstate.instance.config.cfgMailOffline && RealEstate.ess != null)
	        	{
	        		User u = RealEstate.ess.getUser(this.owner);
	        		u.addMail(RealEstate.instance.config.chatPrefix + ChatColor.GREEN + player.getName() + ChatColor.AQUA +
							" has just rented your " + claimType + " at " +
							ChatColor.BLUE + "[" + sign.getWorld().getName() + ", X: " + sign.getBlockX() + ", Y: " + sign.getBlockY() + ", Z: "
							+ sign.getBlockZ() + "]" + ChatColor.AQUA +
	                        " for " + ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural());
	        	}
			}

			player.sendMessage(RealEstate.instance.config.chatPrefix + ChatColor.AQUA + "You have successfully rented this " + claimType +
					" for " + ChatColor.GREEN + price + RealEstate.econ.currencyNamePlural() + ChatColor.AQUA + ". Do /rent autorenew to enable auto renewal!");
			destroySign();
		}
	}

	@Override
	public void preview(Player player)
	{
		final Claim claim = GriefDefender.getCore().getClaimAt(insideBlock);
		String msg = "";
		if(player.hasPermission("realestate.info"))
		{
			String claimType = claim.getParent() == null ? "claim" : "subclaim";
			msg = ChatColor.BLUE + "-----= " + ChatColor.WHITE + "[" + ChatColor.GOLD + "RealEstate Rent Info" + ChatColor.WHITE + "]" + 
					ChatColor.BLUE + " =-----\n";
			if(buyer == null)
			{
				msg += ChatColor.AQUA + "This " + claimType + " is for rent for " +
						ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural() + ChatColor.AQUA + " for " + 
						(maxPeriod > 1 ? "" + ChatColor.GREEN + maxPeriod + ChatColor.AQUA + " periods of " : "") +
						ChatColor.GREEN + Utils.getTime(duration, null, true) + "\n";
				
				if(claimType.equalsIgnoreCase("claim"))
				{
					msg += ChatColor.AQUA + "The current owner is: " + ChatColor.GREEN + claim.getOwnerName();
	            }
	            else
	            {
	            	msg += ChatColor.AQUA + "The main claim owner is: " + ChatColor.GREEN + claim.getOwnerName() + "\n";
	            	msg += ChatColor.LIGHT_PURPLE + "Note: " + ChatColor.AQUA + "You will only rent access to this subclaim!";
	            }
			}
			else
			{
				int days = Period.between(startDate.toLocalDate(), LocalDate.now()).getDays();
				Duration hours = Duration.between(startDate.toLocalTime(), LocalTime.now());
				if(hours.isNegative() && !hours.isZero())
		        {
		            hours = hours.plusHours(24);
		            days--;
		        }
				int daysLeft = duration - days - 1;// we need to remove the current day
				Duration timeRemaining = Duration.ofHours(24).minus(hours);
				
				msg += ChatColor.AQUA + "This " + claimType + " is currently rented by " + 
						ChatColor.GREEN + Bukkit.getOfflinePlayer(buyer).getName() + ChatColor.AQUA + " for " +
						ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural() + ChatColor.AQUA + " for " + 
						(maxPeriod - periodCount > 1 ? "" + ChatColor.GREEN + (maxPeriod - periodCount) + ChatColor.AQUA + " periods of " + 
						ChatColor.GREEN + Utils.getTime(duration, null, false) + ChatColor.AQUA + ". The current period will end in " : "another ") +
						ChatColor.GREEN + Utils.getTime(daysLeft, timeRemaining, true) + "\n";
				if((owner != null && owner.equals(player.getUniqueId()) || buyer.equals(player.getUniqueId())) && RealEstate.instance.config.cfgEnableAutoRenew)
				{
					msg += ChatColor.AQUA + "Automatic renew is currently " + ChatColor.LIGHT_PURPLE + (autoRenew ? "enabled" : "disabled") + "\n";
				}
				if(claimType.equalsIgnoreCase("claim"))
				{
					msg += ChatColor.AQUA + "The current owner is: " + ChatColor.GREEN + claim.getOwnerName();
	            }
	            else
	            {
	            	msg += ChatColor.AQUA + "The main claim owner is: " + ChatColor.GREEN + claim.getOwnerName();
	            }
			}
		}
		else
		{
			msg = RealEstate.instance.config.chatPrefix + ChatColor.RED + "You don't have the permission to view real estate informations!";
		}
		player.sendMessage(msg);
	}

	@Override
	public void msgInfo(CommandSender cs)
	{
        final Claim claim = GriefDefender.getCore().getClaim(claimId);
        final World world = Bukkit.getWorld(claim.getWorldUniqueId());
        cs.sendMessage(ChatColor.DARK_GREEN + "" + claim.getArea() + 
                ChatColor.AQUA + " blocks to " + ChatColor.DARK_GREEN + "Lease " + ChatColor.AQUA + "at " + ChatColor.DARK_GREEN + 
                "[" + world.getName() + ", " +
                "X: " + claim.getLesserBoundaryCorner().getX() + ", " +
                "Y: " + claim.getLesserBoundaryCorner().getY() + ", " +
                "Z: " + claim.getLesserBoundaryCorner().getZ() + "] " + ChatColor.AQUA + "for " + 
                ChatColor.GREEN + price + " " + RealEstate.econ.currencyNamePlural() + ChatColor.AQUA + " per period of " + ChatColor.GREEN + 
                Utils.getTime(duration, Duration.ZERO, false));
	}

}
