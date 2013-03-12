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
import java.lang.reflect.Method;
import java.security.AccessController;

import org.osgi.framework.launch.Framework;

/**
 * Handler used to proxify the {@link Framework} interface.
 * It will delegate all requests to the underlying framework except the init and start method that are intercepted by the Peergreen Kernel
 * @author Florent Benoit
 */
public class PeergreenKernelFrameworkHandler implements InvocationHandler {

    /**
     * Instance of the peergreen Kernel used to start the OSGi gateway.
     */
    private final Object kernel;

    /**
     * Internal OSGi framework used by the Peergreen kernel.
     */
    private final Framework wrappedFramework;


    /**
     * Framework has been initialized ?
     */
    private boolean initialized = false;

    /**
     * Builds a new proxy handler for the given kernel and wrapped OSGi framework
     * @param kernel the peergreen kernel for init/start methods
     * @param wrappedFramework the OSGi framework
     */
    public PeergreenKernelFrameworkHandler(Object kernel, Framework wrappedFramework) {
        this.kernel = kernel;
        this.wrappedFramework = wrappedFramework;
    }


    /**
     * Delegates all the call to the internal OSGi framework except for init/start methods
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // Intercept some methods

        // init
        if ("init".equals(method.getName())) {
            // call kernel.init()
            initialized = true;
            return invokeKernelMethod(getKernelMethod("init"));
        } else if ("start".equals(method.getName())) {

            // Not yet initialized, init
            if (!initialized) {
                invokeKernelMethod(getKernelMethod("init"));
            }

            // call kernel.start(false)
            return invokeKernelMethod(getKernelMethod("start", Boolean.TYPE), Boolean.FALSE);
        }

        // Else call the underlying OSGi framework
        try {
            return method.invoke(wrappedFramework, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Gets the method from the kernel.
     * @param methodName the name of the method
     * @param parameters the parameters of the method
     * @return the expected method
     * @throws Throwable if method is not found
     */
    protected Method getKernelMethod(String methodName, Class<?>... parameters) throws Throwable {
        return kernel.getClass().getDeclaredMethod(methodName, parameters);
    }

    /**
     * Invokes the method on the kernel.
     * @param kernelMethod the method to execute
     * @param args the arguments of the method
     * @return the result of the method
     * @throws Throwable if invocation fails
     */
    protected Object invokeKernelMethod(Method kernelMethod, Object... args) throws Throwable {
        // get method on the kernel
        boolean isAccessible = kernelMethod.isAccessible();
        try {
            AccessController.doPrivileged(new SetAccessibleAction(kernelMethod));
            return kernelMethod.invoke(kernel, args);
        } finally {
            // reset
            AccessController.doPrivileged(new SetAccessibleAction(kernelMethod, isAccessible));
        }
    }


}
