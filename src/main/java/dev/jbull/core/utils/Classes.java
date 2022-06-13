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

package dev.jbull.core.utils;

import com.google.common.reflect.ClassPath;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * The Classes Utility presents a lot of useful methods for the work with multiple classes or
 * packages.
 *
 * <p>Reflection is used in almost every method so you might wan't to call some of them async, but a
 * view methods should be called sync because of the ClassPath.
 *
 * @author Merlin
 * @since 1.0
 */
public class Classes {
    /**
     * Scans the packet for all classes of the given type and creates a new instance using the {@link
     * Class#newInstance()} method. If the packet contains classes that don't have a default
     * constructor, an exception will be thrown. When a new instance is created the consumer will be
     * executed.
     *
     * @param url      root package url
     * @param type     type of classes
     * @param consumer consumer that takes all instances
     * @param <E>      generic type
     */
    public static <E> void forEach(String url, Class<E> type, Consumer<E> consumer, Class<? extends JavaPlugin> main) {
        forEach(url, type, consumer, new String[0], main);
    }

    /**
     * Scans the packet for all classes of the given type and creates a new instance using the {@link
     * Class#newInstance()} method. If the packet contains classes that don't have a default
     * constructor, an exception will be thrown. When a new instance is created the consumer will be
     * executed.
     *
     * @param url      root package url
     * @param type     type of classes
     * @param consumer consumer that takes all instances
     * @param subPaths sub packages
     * @param <E>      generic type
     */
    public static <E> void forEach(
            String url, Class<E> type, Consumer<E> consumer, String[] subPaths, Class<? extends JavaPlugin> main) {
        forEach(url, type, consumer, subPaths, true, main);
    }

    /**
     * Scans the packet for all classes of the given type and creates a new instance using the {@link
     * Class#newInstance()} method. If the packet contains classes that don't have a default
     * constructor, an exception will be thrown. When a new instance is created the consumer will be
     * executed.
     *
     * @param url         root package url
     * @param type        type of classes
     * @param consumer    consumer that takes all instances
     * @param subPaths    sub packages
     * @param executeRoot should the consumer accept classes from the root package or only sub
     *                    packages?
     * @param <E>         generic type
     */
    public static <E> void forEach(
            String url, Class<E> type, Consumer<E> consumer, String[] subPaths, boolean executeRoot, final Class<? extends JavaPlugin> main) {

        try {
            ClassPath classPath = ClassPath.from(main.getClassLoader());

            if (executeRoot) {
                for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(url)) {
                    Class<?> clazz = classInfo.load();
                    System.out.println(clazz.getSimpleName());
                    if (!type.isAssignableFrom(clazz)) {
                        continue;
                    }

                    consumer.accept((E) clazz.newInstance());
                }
            }

            for (int i = 0; i < subPaths.length; i++) {
                for (ClassPath.ClassInfo classInfo :
                        classPath.getTopLevelClasses(url.concat(".".concat(subPaths[i])))) {
                    Class<?> clazz = classInfo.load();
                    System.out.println(clazz.getSimpleName());
                    if (!type.isAssignableFrom(clazz)) {
                        continue;
                    }

                    consumer.accept((E) clazz.newInstance());
                }
            }

        } catch (IOException | InstantiationException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
