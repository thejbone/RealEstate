package me.EtienneDx.RealEstate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import me.EtienneDx.RealEstate.Transactions.*;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class RealEstate extends JavaPlugin
{
	public Logger log;
    public Config config;
	public Messages messages;
    BukkitCommandManager manager;
	public final static String pluginDirPath = "plugins" + File.separator + "RealEstate" + File.separator;
	final static String languagesDirectory = RealEstate.pluginDirPath + "languages";
    public static boolean vaultPresent = false;
    public static Economy econ = null;
    public static Permission perms = null;
    public static Essentials ess = null;
    
    public static RealEstate instance = null;
    
    public static TransactionsStore transactionsStore = null;
	public static UserStore userStore;

	@SuppressWarnings("deprecation")
	public void onEnable()
	{
		RealEstate.instance = this;
        this.log = getLogger();
        
        if (checkVault())
        {
            this.log.info("Vault has been detected and enabled.");
            if (setupEconomy())
            {
                this.log.info("Vault is using " + econ.getName() + " as the economy plugin.");
            }
            else
            {
                this.log.warning("No compatible economy plugin detected [Vault].");
                this.log.warning("Disabling plugin.");
                getPluginLoader().disablePlugin(this);
                return;
            }
            if (setupPermissions())
            {
                this.log.info("Vault is using " + perms.getName() + " for the permissions.");
            }
            else
            {
                this.log.warning("No compatible permissions plugin detected [Vault].");
                this.log.warning("Disabling plugin.");
                getPluginLoader().disablePlugin(this);
                return;
            }
        }
        if((ess = (Essentials)getServer().getPluginManager().getPlugin("Essentials")) != null)
        {
        	this.log.info("Found Essentials, using version " + ess.getDescription().getVersion());
        }
        this.config = new Config();
        this.config.loadConfig();// loads config or default
        this.config.saveConfig();// save eventual default

		this.messages = new Messages();
		this.messages.loadConfig();// loads customizable messages or defaults
		this.messages.saveConfig();// save eventual default
		this.log.info("Customizable messages loaded.");

        ConfigurationSerialization.registerClass(ClaimSell.class);
        ConfigurationSerialization.registerClass(ClaimRent.class);
        ConfigurationSerialization.registerClass(ClaimLease.class);
        ConfigurationSerialization.registerClass(ExitOffer.class);
        
        RealEstate.transactionsStore = new TransactionsStore();
		RealEstate.userStore = new UserStore();
        
        new REListener().registerEvents();
        
        manager = new BukkitCommandManager(this);
        manager.enableUnstableAPI("help");
        registerConditions();
        manager.registerCommand(new RECommand());
        new GD_RealEstateHook();
	}

    private void registerConditions()
    {
        manager.getCommandConditions().addCondition("inClaim", (context) -> {
            final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(context.getIssuer().isPlayer() && 
        			claim == null || !claim.isWilderness())
        	{
        		return;
        	}
        	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        });
        manager.getCommandConditions().addCondition("claimHasTransaction", (context) -> {
        	if(!context.getIssuer().isPlayer())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorPlayerOnly);
        	}
        	final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(claim == null || claim.isWilderness())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        	}
        	Transaction tr = transactionsStore.getTransaction(claim);
        	if(tr == null)
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNoOngoingTransaction);
        	}
        });
        manager.getCommandConditions().addCondition("inPendingTransactionClaim", (context) -> {
        	if(!context.getIssuer().isPlayer())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorPlayerOnly);
        	}
        	final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(claim == null || claim.isWilderness())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        	}
        	Transaction tr = transactionsStore.getTransaction(claim);
        	if(tr == null)
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNotRentNorLease);
        	}
        	else if(tr instanceof BoughtTransaction && ((BoughtTransaction)tr).getBuyer() != null)
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorAlreadyBought);
        	}
        });
        manager.getCommandConditions().addCondition("inBoughtClaim", (context) -> {
        	if(!context.getIssuer().isPlayer())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorPlayerOnly);
        	}
        	final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(claim == null || claim.isWilderness())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        	}
        	Transaction tr = transactionsStore.getTransaction(claim);
        	if(tr == null || !(tr instanceof BoughtTransaction))
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNotRentNorLease);
        	}
        });
        manager.getCommandConditions().addCondition("partOfBoughtTransaction", context -> {
        	if(!context.getIssuer().isPlayer())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorPlayerOnly);
        	}
        	final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(claim == null || claim.isWilderness())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        	}
        	Transaction tr = transactionsStore.getTransaction(claim);
        	if(tr == null)
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNoOngoingTransaction);
        	}
        	if(!(tr instanceof BoughtTransaction))
        	{
            	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNotRentNorLease);
        	}
        	if((((BoughtTransaction)tr).buyer != null && ((BoughtTransaction)tr).buyer.equals(context.getIssuer().getPlayer().getUniqueId())) || 
        			(tr.getOwner() != null && (tr.getOwner().equals(context.getIssuer().getPlayer().getUniqueId()))) || 
        			(claim.isAdminClaim() && RealEstate.perms.has(context.getIssuer().getPlayer(), "realestate.admin")))
        	{
        		return;
        	}
        	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNotPartOfTransaction);
        });
        manager.getCommandConditions().addCondition("partOfRent", context -> {
        	if(!context.getIssuer().isPlayer())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorPlayerOnly);
        	}
        	final Claim claim = GriefDefender.getCore().getClaimAt(context.getIssuer().getPlayer().getLocation());
        	if(claim == null || claim.isWilderness())
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorOutOfClaim);
        	}
        	Transaction tr = transactionsStore.getTransaction(claim);
        	if(tr == null)
        	{
        		throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNoOngoingTransaction);
        	}
        	if(!(tr instanceof ClaimRent))
        	{
            	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorRentOnly);
        	}
        	if((((ClaimRent)tr).buyer != null && ((ClaimRent)tr).buyer.equals(context.getIssuer().getPlayer().getUniqueId())) || 
        			(tr.getOwner() != null && (tr.getOwner().equals(context.getIssuer().getPlayer().getUniqueId()))) || 
        			(claim.isAdminClaim() && RealEstate.perms.has(context.getIssuer().getPlayer(), "realestate.admin")))
        	{
        		return;
        	}
        	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorNotPartOfTransaction);
        });
        manager.getCommandConditions().addCondition(Double.class, "positiveDouble", (c, exec, value) -> {
        	if(value > 0) return;
        	throw new ConditionFailedException(config.chatPrefix + messages.msgErrorValueGreaterThanZero);
        });
	}

	public void addLogEntry(String entry)
    {
        try
        {
            File logFile = new File(this.config.logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fw = new FileWriter(logFile, true);
            PrintWriter pw = new PrintWriter(fw);

            pw.println(entry);
            pw.flush();
            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkVault()
    {
        vaultPresent = getServer().getPluginManager().getPlugin("Vault") != null;
        return vaultPresent;
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = (Economy)rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = (Permission)rsp.getProvider();
        return perms != null;
    }
}
