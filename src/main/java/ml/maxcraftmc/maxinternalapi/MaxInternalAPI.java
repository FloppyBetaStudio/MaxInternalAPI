package ml.maxcraftmc.maxinternalapi;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public final class MaxInternalAPI extends JavaPlugin {
    public static AuthMeApi authmeapi;
    public static Logger log;

    public static HTTPServer apiserver;


    public static Economy econ = null;
    public static Permission perms = null;
    //public static Chat chat = null;


    @Override
    public void onEnable() {
        // Plugin startup logic
        log = this.getLogger();
        authmeapi = AuthMeApi.getInstance();
        econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        perms = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        //chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();

        // 读取config.yml
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {  // 判断文件是否存在
            configFile.getParentFile().mkdirs();  // 创建父文件夹
            saveDefaultConfig();  // 创建默认的配置文件
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);


            // 创建HttpServer
            apiserver = new HTTPServer(config.getInt("http-port"), config.getString("auth-token"));


        //apiserver = new HTTPServer(config.getInt("http-port"), config.getString("auth-token"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        apiserver.app.stop();
    }

    public void saveDefaultConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("http-port", 8000);
        config.set("auth-token", "abc123");
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
