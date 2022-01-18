package me.EtienneDx.RealEstate.Transactions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

public abstract class ClaimTransaction implements ConfigurationSerializable, Transaction
{
	public UUID claimId;
	public UUID owner = null;
	public double price;
	public Location sign = null;
	public Location insideBlock = null;
	
	public ClaimTransaction(Claim claim, Player player, double price, Location sign, Location insideBlock)
	{
		this.claimId = claim.getUniqueId();
		if (claim.isAdminClaim() || GriefDefender.getCore().getAdminUser().getUniqueId().equals(claim.getOwnerUniqueId())) {
		    this.owner = null;
		} else {
		    this.owner = player != null ? player.getUniqueId() : null;
		}
		this.price = price;
		this.sign = sign;
		this.insideBlock = insideBlock;
	}

	public ClaimTransaction(Claim claim, Player player, double price)
	{
		this.claimId = claim.getUniqueId();
		if (claim.isAdminClaim() || GriefDefender.getCore().getAdminUser().getUniqueId().equals(claim.getOwnerUniqueId())) {
		    this.owner = null;
		} else {
		    this.owner = player != null ? player.getUniqueId() : null;
		}
		this.price = price;
		this.sign = null;
	}

	public ClaimTransaction(Map<String, Object> map)
	{
		this.claimId = UUID.fromString(String.valueOf(map.get("claimId")));
		if(map.get("owner") != null)
			this.owner = UUID.fromString((String) map.get("owner"));
		this.price = (double) map.get("price");
		if(map.get("signLocation") != null)
			this.sign = (Location) map.get("signLocation");
		if(map.get("insideBlockLocation") != null){
			this.insideBlock = (Location) map.get("insideBlockLocation");
		}

	}
	
	public ClaimTransaction()
	{
		
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		
		map.put("claimId", this.claimId.toString());
		if(owner != null)
			map.put("owner", owner.toString());
		map.put("price", this.price);
		if(sign != null)
			map.put("signLocation", sign);
		if(insideBlock != null){
			map.put("insideBlockLocation", insideBlock);
		}

		return map;
	}

	@Override
	public Block getHolder()
	{
		return sign.getBlock().getState() instanceof Sign ? sign.getBlock() : null;
	}

	@Override
	public Block getInsideBlock(){
		return insideBlock.getBlock();
	}

	@Override
	public UUID getOwner()
	{
		return owner;
	}
	
	@Override
	public boolean tryCancelTransaction(Player p)
	{
		return this.tryCancelTransaction(p, false);
	}
}
