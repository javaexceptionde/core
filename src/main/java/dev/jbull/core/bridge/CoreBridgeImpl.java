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

package dev.jbull.core.bridge;

import dev.jbull.core.Core;

public class CoreBridgeImpl<T> implements CoreBridge {

    @Override
    public T getService(Class service) {
        if (Core.getCore().getServiceLoader().getMutableClassToInstanceMap().containsKey(service)) {
            return (T) Core.getCore().getServiceLoader().getMutableClassToInstanceMap().get(service);
        }else {
            for (Object loadedService : Core.getCore().getServiceLoader().getLoadedServices().values()) {
                for (Class<?> anInterface : loadedService.getClass().getInterfaces()) {
                    try {
                        System.out.println(anInterface.getPackageName() + " first package name " + service.getPackageName());
                        if (anInterface.getPackageName().equals(service.getPackageName())){
                            System.out.println("true");
                            T type = (T) loadedService;
                            Core.getCore().getServiceLoader().getMutableClassToInstanceMap().put(service, type);
                            return type;
                        }
                    }catch (ClassCastException e){
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
        return null;
    }

}
