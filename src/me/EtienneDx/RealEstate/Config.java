package me.EtienneDx.RealEstate;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import me.EtienneDx.AnnotationConfig.AnnotationConfig;
import me.EtienneDx.AnnotationConfig.ConfigField;
import me.EtienneDx.AnnotationConfig.ConfigFile;

@ConfigFile(header = "RealEstate wiki and newest versions are available at http://www.github.com/EtienneDx/RealEstate")
public class Config extends AnnotationConfig
{
    public PluginDescriptionFile pdf;

    public final String configFilePath = RealEstate.pluginDirPath + "config.yml";
    public final String logFilePath = RealEstate.pluginDirPath + "GriefProtection_RealEstate.log";
    public final String chatPrefix = "[" + ChatColor.GOLD + "RealEstate" + ChatColor.WHITE + "] ";
    
    @ConfigField(name="RealEstate.Keywords.SignsHeader", comment = "What is displayed in top of the signs")
    public String cfgSignsHeader = ChatColor.GOLD + "[RealEstate]";
    //public List<String> cfgSigns;

    @ConfigField(name="RealEstate.Keywords.Sell", comment = "List of all possible possible signs headers to sell a claim")
    public List<String> cfgSellKeywords = Arrays.asList("[sell]", "[sell claim]", "[sc]", "[re]", "[realestate]");
    @ConfigField(name="RealEstate.Keywords.Rent", comment = "List of all possible possible signs headers to rent a claim")
    public List<String> cfgRentKeywords = Arrays.asList("[rent]", "[rent claim]", "[rc]");
    @ConfigField(name="RealEstate.Keywords.ContainerRent", comment = "List of all possible possible signs headers to rent a claim")
    public List<String> cfgContainerRentKeywords = Arrays.asList("[container rent]", "[crent]");
    @ConfigField(name="RealEstate.Keywords.Lease", comment = "List of all possible possible signs headers to lease a claim")
    public List<String> cfgLeaseKeywords = Arrays.asList("[lease]", "[lease claim]", "[lc]");

    @ConfigField(name="RealEstate.Keywords.Replace.Sell", comment = "What is displayed on signs for preoperties to sell")
    public String cfgReplaceSell = "FOR SALE";
    @ConfigField(name="RealEstate.Keywords.Replace.Rent", comment = "What is displayed on signs for preoperties to rent")
    public String cfgReplaceRent = "FOR RENT";
    @ConfigField(name="RealEstate.Keywords.Replace.Lease", comment = "What is displayed on signs for preoperties to lease")
    public String cfgReplaceLease = "FOR LEASE";
    @ConfigField(name="RealEstate.Keywords.Replace.Ongoing.Rent", comment = "What is displayed on the first line of the sign once someone rents a claim.")
    public String cfgReplaceOngoingRent = "[Rented]";
    @ConfigField(name="RealEstate.Keywords.Replace.ContainerRent", comment = "What is displayed on the third line of the sign when renting container access only.")
    public String cfgContainerRentLine = ChatColor.BLUE + "Containers only";

    @ConfigField(name="RealEstate.Rules.Sell", comment = "Is selling claims enabled?")
    public boolean cfgEnableSell = true;
    @ConfigField(name="RealEstate.Rules.Rent", comment = "Is renting claims enabled?")
    public boolean cfgEnableRent = true;
    @ConfigField(name="RealEstate.Rules.Lease", comment = "Is leasing claims enabled?")
    public boolean cfgEnableLease = true;

    @ConfigField(name="RealEstate.Rules.AutomaticRenew", comment = "Can players renting claims enable automatic renew of their contracts?")
    public boolean cfgEnableAutoRenew = true;
    @ConfigField(name="RealEstate.Rules.RentPeriods", comment = "Can a rent contract last multiple periods?")
    public boolean cfgEnableRentPeriod = true;
    @ConfigField(name="RealEstate.Rules.DestroySigns.Rent", comment = "Should the rent signs get destroyed once the claim is rented?")
    public boolean cfgDestroyRentSigns = false;
    @ConfigField(name="RealEstate.Rules.DestroySigns.Lease", comment = "Should the lease signs get destroyed once the claim is leased?")
    public boolean cfgDestroyLeaseSigns = true;

    @ConfigField(name="RealEstate.Rules.TransferClaimBlocks", comment = "Are the claim blocks transfered to the new owner on purchase or should the buyer provide them?")
    public boolean cfgTransferClaimBlocks = true;

    @ConfigField(name="RealEstate.Rules.UseCurrencySymbol", comment = "Should the signs display the prices with a currency symbol instead of the full currency name?")
    public boolean cfgUseCurrencySymbol = false;
    @ConfigField(name="RealEstate.Rules.CurrencySymbol", comment = "In case UseCurrencySymbol is true, what symbol should be used?")
    public String cfgCurrencySymbol = "$";
    @ConfigField(name="RealEstate.Rules.UseDecimalCurrency", comment = "Allow players to use decimal currency e.g. $10.15")
    public boolean cfgUseDecimalCurrency = true;
    @ConfigField(name="RealEstate.Rules.UseVaultCurrencyFormat", comment = "Uses vault currency formatting. Ignores above currency settings")
    public boolean cfgUseVaultCurrencyFormat = true;

    @ConfigField(name="RealEstate.Messaging.MessageOwner", comment = "Should the owner get messaged once one of his claim is rented/leased/bought and on end of contracts?")
    public boolean cfgMessageOwner = true;
    @ConfigField(name="RealEstate.Messaging.MessageBuyer", comment = "Should the buyer get messaged once one of his claim is rented/leased/bought and on end of contracts?")
    public boolean cfgMessageBuyer = true;
    @ConfigField(name="RealEstate.Messaging.BroadcastSell", comment = "Should a message get broadcasted when a player put a claim for rent/lease/sell?")
    public boolean cfgBroadcastSell = true;
    @ConfigField(name="RealEstate.Messaging.MailOffline", comment = "Should offline owner/buyers receive mails (using the Essentials plugin) when they're offline?")
    public boolean cfgMailOffline = true;

    @ConfigField(name="RealEstate.Default.PricesPerBlock.Sell", comment = "Chat is the default price per block when selling a claim")
    public double cfgPriceSellPerBlock = 5.0;
    @ConfigField(name="RealEstate.Default.PricesPerBlock.Rent", comment = "Chat is the default price per block when renting a claim")
    public double cfgPriceRentPerBlock = 2.0;
    @ConfigField(name="RealEstate.Default.PricesPerBlock.Lease", comment = "Chat is the default price per block when leasing a claim")
    public double cfgPriceLeasePerBlock = 2.0;
    @ConfigField(name="RealEstate.Default.InTownMultiplier", comment = "Multiplier for claim in town")
    public double cfgInTownMultiplier = 1.5;

    @ConfigField(name="RealEstate.Default.CuboidPerBlockCost", comment = "Should cuboid claims be per block 3d or same as 2D claims? False for same as 2D claims")
    public boolean cfgCuboidPerBlockCost = true;
    @ConfigField(name="RealEstate.Default.MaxRentals", comment = "What is the max rentals?")
    public int cfgRentMax = 2;
    @ConfigField(name="RealEstate.Default.PriceBrackets", comment="Should there be price brackets?")
    public boolean isCfgPriceBrackets = true;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket1", comment="What is the price bracket one? realestate.pricebracket.one")
    public double cfgPriceBracket1 = 1000;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket1Msg", comment="What should the comment be for price bracket one?")
    public String cfgPriceBracket1Msg = "You do not have access to rent in price bracket one";
    @ConfigField(name="RealEstate.Default.cfgPriceBracket2", comment="What is the price bracket two? realestate.pricebracket.two")
    public double cfgPriceBracket2= 10000;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket2Msg", comment="What should the comment be for price bracket three?")
    public String cfgPriceBracket2Msg = "You do not have access to rent in price bracket three";
    @ConfigField(name="RealEstate.Default.cfgPriceBracket3", comment="What is the price bracket three? realestate.pricebracket.three")
    public double cfgPriceBracket3 = 100000;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket3Msg", comment="What should the comment be for price bracket four?")
    public String cfgPriceBracket3Msg = "You do not have access to rent in price bracket four";
    @ConfigField(name="RealEstate.Default.cfgPriceBracket4", comment="What is the price bracket four? realestate.pricebracket.four")
    public double cfgPriceBracket4 = 1000000;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket4Msg", comment="What should the comment be for price bracket five?")
    public String cfgPriceBracket4Msg = "You do not have access to rent in price bracket five";
    @ConfigField(name="RealEstate.Default.cfgPriceBracket5", comment="What is the price bracket five? realestate.pricebracket.five")
    public double cfgPriceBracket5 = 10000000;
    @ConfigField(name="RealEstate.Default.cfgPriceBracket5Msg", comment="What should the comment be for price bracket five?")
    public String cfgPriceBracket5Msg = "You do not have access to rent in price bracket five";

    @ConfigField(name="RealEstate.Default.Duration.Rent", comment = "How long is a rent period by default")
    public String cfgRentTime = "7D";
    @ConfigField(name="RealEstate.Default.Duration.Lease", comment = "How long is a lease period by default")
    public String cfgLeaseTime = "7D";

    @ConfigField(name="RealEstate.Default.LeasePaymentsCount", comment = "How many lease periods are required before the buyer gets the claim's ownership by default")
    public int cfgLeasePayments = 5;
    
    @ConfigField(name="RealEstate.Settings.PageSize", comment = "How many Real Estate offer should be shown by page using the '/re list' command")
    public int cfgPageSize = 8;
    
    @ConfigField(name="RealEstate.Settings.MessagesFiles", comment="Language file to be used. You can see all languages files in the languages directory. If the language file does not exist, it will be created and you'll be able to modify it later on.")
    public String languageFile = "en.yml";
    
    public Config()
    {
        this.pdf = RealEstate.instance.getDescription();
    }
    
    public String getString(List<String> li)
    {
    	return String.join(";", li);
    }
    
    public List<String> getList(String str)
    {
    	return Arrays.asList(str.split(";"));
    }
    
    List<String> getConfigList(YamlConfiguration config, String path, List<String> defVal)
    {
    	config.addDefault(path, defVal);
    	List<String> ret = config.getStringList(path);
    	ret.replaceAll(String::toLowerCase);
    	return ret;
    }
    
    @Override
    public void loadConfig()
    {
        //YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(this.configFilePath));
        this.loadConfig(this.configFilePath);
    }
}
