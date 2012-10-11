/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.osgi;


import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.osgi.framework.Version;

/**
 * OSGi management operations
 *
 * @author thomas.diesler@jboss.com
 * @since 25-Sep-2012
 */
public final class FrameworkManagement {

    // Hide ctor
    private FrameworkManagement() {
    }

    interface ModelConstants {
        String BUNDLE = "bundle";
        String START = "start";
        String STATE = "state";
        String STOP = "stop";
        String SYMBOLIC_NAME = "symbolic-name";
        String VERSION = "version";
    }

    interface ModelDescriptionConstants {
        String FAILURE_DESCRIPTION = "failure-description";
        String INCLUDE_RUNTIME = "include-runtime";
        String READ_RESOURCE_OPERATION = "read-resource";
        String RECURSIVE = "recursive";
        String OUTCOME = "outcome";
        String RESULT = "result";
        String SUCCESS = "success";
    }

    public static Long getBundleId(ModelControllerClient client, String symbolicName, Version version) throws Exception {
        Long result = new Long(-1);
        ModelNode op = createOpNode("subsystem=osgi", ModelDescriptionConstants.READ_RESOURCE_OPERATION);
        op.get(ModelDescriptionConstants.INCLUDE_RUNTIME).set("true");
        op.get(ModelDescriptionConstants.RECURSIVE).set("true");
        ModelNode bundleNode = executeOperation(client, op).get(ModelConstants.BUNDLE);
        for (ModelNode node : bundleNode.asList()) {
            Property propNode = node.asProperty();
            ModelNode valueNode = propNode.getValue();
            ModelNode symbolicNameNode = valueNode.get(ModelConstants.SYMBOLIC_NAME);
            if (symbolicNameNode.asString().equals(symbolicName)) {
                if (version == null) {
                    result = new Long(propNode.getName());
                    break;
                }
                ModelNode versionNode = valueNode.get(ModelConstants.VERSION);
                if (versionNode.isDefined()) {
                    Version auxver = Version.parseVersion(versionNode.asString());
                    if (version.equals(auxver)) {
                        result = new Long(propNode.getName());
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static String getBundleState(ModelControllerClient client, Object resId) throws Exception {
        ModelNode op = createOpNode("subsystem=osgi/bundle=" + resId, ModelDescriptionConstants.READ_RESOURCE_OPERATION);
        op.get(ModelDescriptionConstants.INCLUDE_RUNTIME).set("true");
        op.get(ModelDescriptionConstants.RECURSIVE).set("true");
        ModelNode result = executeOperation(client, op);
        return result.get(ModelConstants.STATE).asString();
    }

    public static ModelNode getBundleInfo(ModelControllerClient client, Object resId) throws Exception {
        ModelNode op = createOpNode("subsystem=osgi/bundle=" + resId, ModelDescriptionConstants.READ_RESOURCE_OPERATION);
        op.get(ModelDescriptionConstants.INCLUDE_RUNTIME).set("true");
        op.get(ModelDescriptionConstants.RECURSIVE).set("true");
        return executeOperation(client, op);
    }

    public static boolean bundleStart(ModelControllerClient client, Object resId) throws Exception {
        ModelNode op = createOpNode("subsystem=osgi/bundle=" + resId, ModelConstants.START);
        ModelNode result = executeOperation(client, op, false);
        return ModelDescriptionConstants.SUCCESS.equals(result.get(ModelDescriptionConstants.OUTCOME).asString());
    }

    public static boolean bundleStop(ModelControllerClient client, Object resId) throws Exception {
        ModelNode op = createOpNode("subsystem=osgi/bundle=" + resId, ModelConstants.STOP);
        ModelNode result = executeOperation(client, op, false);
        return ModelDescriptionConstants.SUCCESS.equals(result.get(ModelDescriptionConstants.OUTCOME).asString());
    }

    private static ModelNode createOpNode(String address, String operation) {
        ModelNode op = new ModelNode();
        ModelNode list = op.get("address").setEmptyList();
        if (address != null) {
            String[] pathSegments = address.split("/");
            for (String segment : pathSegments) {
                String[] elements = segment.split("=");
                list.add(elements[0], elements[1]);
            }
        }
        op.get("operation").set(operation);
        return op;
    }

    private static ModelNode executeOperation(final ModelControllerClient client, ModelNode op) throws Exception {
        return executeOperation(client, op, true);
    }

    private static ModelNode executeOperation(final ModelControllerClient client, ModelNode op, boolean unwrapResult) throws Exception {
        ModelNode ret = client.execute(op);
        if (!unwrapResult) return ret;
        if (!ModelDescriptionConstants.SUCCESS.equals(ret.get(ModelDescriptionConstants.OUTCOME).asString())) {
            throw new IllegalStateException("Management operation failed: " + ret.get(ModelDescriptionConstants.FAILURE_DESCRIPTION));
        }
        return ret.get(ModelDescriptionConstants.RESULT);
    }
}
