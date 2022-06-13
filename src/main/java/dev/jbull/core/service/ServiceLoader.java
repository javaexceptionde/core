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

package dev.jbull.core.service;

import com.google.common.collect.MutableClassToInstanceMap;
import dev.jbull.core.Core;
import dev.jbull.core.utils.Classes;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServiceLoader<T> {
    private Map<String, Class<?>> loadedServices = new HashMap<>();
    private Map<String, Service> serviceClassesPrepared = new HashMap<>();
    private Map<String, File> serviceFilePrepared = new HashMap<>();
    public MutableClassToInstanceMap<T> mutableClassToInstanceMap = MutableClassToInstanceMap.create();

    public Map<String, Class<?>> getLoadedServices() {
        return loadedServices;
    }

    public void loadAllServices(){
        System.out.println("Instances load all");
        File file = new File(Core.getCore().getDefaultPath() + "/modules");
        List<URL> urls = new ArrayList<>();
        for (File file1 : file.listFiles()){
            try {
                urls.add(file1.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        URL[] urlArray = new URL[urls.size()];
        ClassLoader loader = URLClassLoader.newInstance(
                urls.toArray(urlArray),
                Core.getCore().getClass().getClassLoader()
        );
        for (File listFile : file.listFiles()) {
            try {
                JarFile jarFile = new JarFile(listFile);
                JarEntry entry = jarFile.getJarEntry("service.yml");
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.loadAs(jarFile.getInputStream(entry), Map.class);
                String main = String.valueOf(data.get("main"));
                JarEntry mainClass = jarFile.getJarEntry(main.replaceAll(".", "/"));
                Class<?> clazz = Class.forName(main, true, loader);
                if (!clazz.isAnnotationPresent(CoreService.class)){
                    System.out.println("Cant load service because its missing the CoreService annotation");
                    continue;
                }
                Class<T> clazz1 = (Class<T>) loader.loadClass(main.replace("Impl", ""));
                T service = (T) clazz.newInstance();
                mutableClassToInstanceMap.put(clazz1, service);
                serviceClassesPrepared.put(clazz.getAnnotation(CoreService.class).name(), (Service) service);
                serviceFilePrepared.put(clazz.getAnnotation(CoreService.class).name(), listFile);


            } catch (IOException e) {
                continue;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        serviceClassesPrepared.keySet().forEach(s -> {
            if (loadedServices.containsKey(s)){
                return;
            }
            CoreService coreService = serviceClassesPrepared.get(s).getClass().getAnnotation(CoreService.class);
            for (String requiredService : coreService.requiredServices()) {
                loadService(requiredService);
            }
            loadService(s);
        });
    }

    private void loadService(String name){
        if (loadedServices.containsKey(name))return;
        CoreService coreService = serviceClassesPrepared.get(name).getClass().getAnnotation(CoreService.class);
        for (String service : coreService.requiredServices()){
            loadService(service);
        }
        Service service = serviceClassesPrepared.get(name);
        try {
            service.onEnable();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        System.out.println("Loaded service" + service);
        loadedServices.put(name, serviceClassesPrepared.get(name).getClass());
    }

    private Service findServiceByName(String name){
        CompletableFuture<Service> future = new CompletableFuture<Service>();
        Classes.forEach("dev.jbull", Service.class, instance -> {
            if (instance.getClass().isAnnotationPresent(CoreService.class)){
                CoreService coreService = instance.getClass().getAnnotation(CoreService.class);
                if (coreService.name().equalsIgnoreCase(name)){
                    future.complete(instance);
                }
            }
        }, Core.getCore().getJavaPlugin().getClass());
        return future.join();
    }

    public MutableClassToInstanceMap<T> getMutableClassToInstanceMap() {
        return mutableClassToInstanceMap;
    }
}
