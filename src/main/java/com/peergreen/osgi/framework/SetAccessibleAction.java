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

import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

/**
 * Privileged action used to set the accessible flag.
 * @author Florent Benoit
 */
public class SetAccessibleAction implements PrivilegedAction<Void> {

    /**
     * Accessible object used to call setAccessible method.
     */
    private final AccessibleObject accessibleObject;

    /**
     * Flag to apply.
     */
    private final boolean accessible;

    /**
     * Default constructor for a given accessible object.
     * @param accessibleObject the accessible object
     */
    public SetAccessibleAction(AccessibleObject accessibleObject) {
        this(accessibleObject, true);
    }

    /**
     * Default constructor for a given accessible object.
     * @param accessibleObject the accessible object
     * @param accessible the accessible flag
     */
    public SetAccessibleAction(AccessibleObject accessibleObject, boolean accessible) {
        this.accessibleObject = accessibleObject;
        this.accessible = accessible;
    }



    /**
     * Apply the setAcessible
     */
    @Override
    public Void run() {
        accessibleObject.setAccessible(accessible);
        return null;
    }

}
