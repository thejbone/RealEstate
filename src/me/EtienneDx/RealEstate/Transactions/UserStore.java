package me.EtienneDx.RealEstate.Transactions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.EtienneDx.RealEstate.RealEstate;

public class UserStore
{
    public final String dataFilePath = RealEstate.pluginDirPath + "users.data";
    public HashMap<String, UserEntry> userList;

    public UserStore()
    {
        loadData();
    }

    public void loadData()
    {
        userList = new HashMap<>();
        File file = new File(this.dataFilePath);

        if(file.exists())
        {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            try {
                RealEstate.instance.addLogEntry(new String(Files.readAllBytes(FileSystems.getDefault().getPath(this.dataFilePath))));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ConfigurationSection users = config.getConfigurationSection("Users");
            if(users != null)
            {
                RealEstate.instance.addLogEntry(users.toString());
                RealEstate.instance.addLogEntry(users.getKeys(false).size() + "");
                for(String key : users.getKeys(false))
                {
                    UserEntry u = (UserEntry)users.get(key);
                    userList.put(key, u);
                }
            }
        }
    }

    public void saveData()
    {
        YamlConfiguration config = new YamlConfiguration();
        for (UserEntry u : userList.values())
            config.set("User." + u.getUuid(), u);
        try
        {
            config.save(new File(this.dataFilePath));
        }
        catch (IOException e)
        {
            RealEstate.instance.log.info("Unable to write to the data file at \"" + this.dataFilePath + "\"");
        }
    }

    public UserEntry addUser(String uuid){
        userList.put(uuid, new UserEntry(0, uuid));
        saveData();
        return userList.get(uuid);
    }

    public UserEntry getUser(UUID uuid)
    {
        if(uuid == null)
            return null;
        try {
            if(userList.containsKey(uuid.toString())){
                return userList.get(uuid.toString());
            }
        } catch (Exception ignored){}
        return addUser(uuid.toString());
    }
}
