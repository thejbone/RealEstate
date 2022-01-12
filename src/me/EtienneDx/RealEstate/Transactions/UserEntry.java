package me.EtienneDx.RealEstate.Transactions;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UserEntry implements ConfigurationSerializable {

    private int numberOfRents;
    private String uuid;

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("numberOfRents", this.numberOfRents);
        return map;
    }

    public UserEntry(int numberOfRents, String uuid) {
        this.numberOfRents = numberOfRents;
        this.uuid = uuid;
    }

    public int getNumberOfRents() {
        return numberOfRents;
    }

    public void setNumberOfRents(int numberOfRents) {
        this.numberOfRents = numberOfRents;
    }

    public void incrementRents(){
        this.numberOfRents++;
    }
    public void decrementRents(){
        if(this.numberOfRents > 0){
            this.numberOfRents--;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
