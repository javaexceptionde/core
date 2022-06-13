/*
 *     Copyright (C) 2022  Jonathan Benedikt Bull<jonathan@jbull.dev>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.jbull.core;

import dev.jbull.core.bridge.CoreBridge;
import dev.jbull.core.bridge.CoreBridgeImpl;
import dev.jbull.core.service.ServiceLoader;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Core {
    private static Core core;
    private ServiceLoader serviceLoader;
    private CoreBridge coreBridge;
    private JavaPlugin javaPlugin;
    private Plugin plugin;
    private boolean isSpigot;
    private String defaultPath;

    public Core(Plugin plugin){
        core = this;
        this.plugin = plugin;
        this.isSpigot = false;
        this.defaultPath = plugin.getDataFolder().getPath();
    }

    public Core(JavaPlugin javaPlugin) {
        core = this;
        this.javaPlugin = javaPlugin;
        this.isSpigot = true;
        this.defaultPath = javaPlugin.getDataFolder().getPath();
    }

    public void initialize(){
        File file = new File(defaultPath + "/modules");
        file.mkdirs();
        coreBridge = new CoreBridgeImpl();
        serviceLoader = new ServiceLoader();
        serviceLoader.loadAllServices();

    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public static Core getCore() {
        return core;
    }

    public ServiceLoader getServiceLoader() {
        return serviceLoader;
    }

    public CoreBridge getCoreBridge() {
        return coreBridge;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isSpigot() {
        return isSpigot;
    }

    public String getDefaultPath() {
        return defaultPath;
    }
}
