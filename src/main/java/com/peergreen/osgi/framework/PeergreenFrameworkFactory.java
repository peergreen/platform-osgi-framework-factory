/**
 * Copyright 2013 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.peergreen.osgi.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.util.Map;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.peergreen.bootstrap.Bootstrap;
import com.peergreen.bootstrap.BootstrapException;

/**
 * {@link FrameworkFactory} implementation that allows to use the Peergreen platform as an OSGi Framework.
 * @author Florent Benoit
 */
public class PeergreenFrameworkFactory implements FrameworkFactory {

    /**
     * Create a new Peergreen {@link Framework} instance.
     * @param configuration the properties to use
     * @return a new instance of the peergreen framework
     */
    @Override
    public Framework newFramework(Map<String, String> configuration) {
        // Needs to load the bootstrap
        Bootstrap bootstrap = Bootstrap.newBootstrap(null);

        Class<?> kernelClass = null;
        try {
            kernelClass = bootstrap.load();
        } catch (BootstrapException e) {
            throw new IllegalStateException("Unable to initialize the peergreen bootstrap", e);
        }

        // Creates a new instance of the kernel
        Object kernel;
        try {
            kernel = kernelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to load the framework", e);
        }

        // Gets prepare method
        final Method prepareMethod;
        try {
            prepareMethod = kernelClass.getDeclaredMethod("prepare", Map.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Unable to load the framework", e);
        }

        // .. and call it to gets the internal framework of the kernel...
        boolean isAccessible = prepareMethod.isAccessible();
        Framework framework = null;
        try {
            AccessController.doPrivileged(new SetAccessibleAction(prepareMethod));
            framework = (Framework) prepareMethod.invoke(kernel, configuration);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to load the framework", e);
        } finally {
            // reset
            AccessController.doPrivileged(new SetAccessibleAction(prepareMethod, isAccessible));
        }

        // ...  and wrap it
        InvocationHandler invocationHandler = new PeergreenKernelFrameworkHandler(kernel, framework);

        // Now, return a proxy which acts as a Framework
        return (Framework) Proxy.newProxyInstance(PeergreenFrameworkFactory.class.getClassLoader(), new Class[] {Framework.class}, invocationHandler);
    }

}
