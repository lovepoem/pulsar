/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.worker.rest.api.v3;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.pulsar.common.functions.UpdateOptions;
import org.apache.pulsar.common.io.ConnectorDefinition;
import org.apache.pulsar.common.io.SourceConfig;
import org.apache.pulsar.common.policies.data.SourceStatus;
import org.apache.pulsar.functions.worker.rest.FunctionApiResource;
import org.apache.pulsar.functions.worker.rest.api.SourcesImpl;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(value = "/sources", description = "Sources admin apis", tags = "sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/sources")
public class SourcesApiV3Resource extends FunctionApiResource {

    protected final SourcesImpl source;

    public SourcesApiV3Resource() {
        this.source = new SourcesImpl(this);
    }

    @POST
    @Path("/{tenant}/{namespace}/{sourceName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void registerSource(final @PathParam("tenant") String tenant,
                                   final @PathParam("namespace") String namespace,
                                   final @PathParam("sourceName") String sourceName,
                                   final @FormDataParam("data") InputStream uploadedInputStream,
                                   final @FormDataParam("data") FormDataContentDisposition fileDetail,
                                   final @FormDataParam("url") String functionPkgUrl,
                                   final @FormDataParam("sourceConfig") String sourceConfigJson) {

        source.registerFunction(tenant, namespace, sourceName, uploadedInputStream, fileDetail,
                functionPkgUrl, sourceConfigJson, clientAppId(), clientAuthData());

    }

    @PUT
    @Path("/{tenant}/{namespace}/{sourceName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void updateSource(final @PathParam("tenant") String tenant,
                             final @PathParam("namespace") String namespace,
                             final @PathParam("sourceName") String sourceName,
                             final @FormDataParam("data") InputStream uploadedInputStream,
                             final @FormDataParam("data") FormDataContentDisposition fileDetail,
                             final @FormDataParam("url") String functionPkgUrl,
                             final @FormDataParam("sourceConfig") String sourceConfigJson,
                             final @FormDataParam("updateOptions") UpdateOptions updateOptions) {

        source.updateFunction(tenant, namespace, sourceName, uploadedInputStream, fileDetail,
                functionPkgUrl, sourceConfigJson, clientAppId(), clientAuthData(), updateOptions);
    }


    @DELETE
    @Path("/{tenant}/{namespace}/{sourceName}")
    public void deregisterSource(final @PathParam("tenant") String tenant,
                                 final @PathParam("namespace") String namespace,
                                 final @PathParam("sourceName") String sourceName) {
        source.deregisterFunction(tenant, namespace, sourceName, clientAppId(), clientAuthData());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tenant}/{namespace}/{sourceName}")
    public SourceConfig getSourceInfo(final @PathParam("tenant") String tenant,
                                      final @PathParam("namespace") String namespace,
                                      final @PathParam("sourceName") String sourceName)
            throws IOException {
        return source.getSourceInfo(tenant, namespace, sourceName);
    }

    @GET
    @ApiOperation(
            value = "Displays the status of a Pulsar Source instance",
            response = SourceStatus.SourceInstanceStatus.SourceInstanceStatusData.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 404, message = "The source doesn't exist")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tenant}/{namespace}/{sourceName}/{instanceId}/status")
    public SourceStatus.SourceInstanceStatus.SourceInstanceStatusData getSourceInstanceStatus(
            final @PathParam("tenant") String tenant,
            final @PathParam("namespace") String namespace,
            final @PathParam("sourceName") String sourceName,
            final @PathParam("instanceId") String instanceId) throws IOException {
        return source.getSourceInstanceStatus(
            tenant, namespace, sourceName, instanceId, uri.getRequestUri(), clientAppId(), clientAuthData());
    }

    @GET
    @ApiOperation(
            value = "Displays the status of a Pulsar Source running in cluster mode",
            response = SourceStatus.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 404, message = "The source doesn't exist")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tenant}/{namespace}/{sourceName}/status")
    public SourceStatus getSourceStatus(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace,
                                    final @PathParam("sourceName") String sourceName) throws IOException {
        return source.getSourceStatus(tenant, namespace, sourceName, uri.getRequestUri(), clientAppId(), clientAuthData());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tenant}/{namespace}")
    public List<String> listSources(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace) {
        return source.listFunctions(tenant, namespace, clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Restart source instance", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/{instanceId}/restart")
    @Consumes(MediaType.APPLICATION_JSON)
    public void restartSource(final @PathParam("tenant") String tenant,
                              final @PathParam("namespace") String namespace,
                              final @PathParam("sourceName") String sourceName,
                              final @PathParam("instanceId") String instanceId) {
        source.restartFunctionInstance(tenant, namespace, sourceName, instanceId, this.uri.getRequestUri(), clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Restart all source instances", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
    @ApiResponse(code = 404, message = "The function does not exist"),
    @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/restart")
    @Consumes(MediaType.APPLICATION_JSON)
    public void restartSource(final @PathParam("tenant") String tenant,
                              final @PathParam("namespace") String namespace,
                              final @PathParam("sourceName") String sourceName) {
        source.restartFunctionInstances(tenant, namespace, sourceName, clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Stop source instance", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/{instanceId}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    public void stopSource(final @PathParam("tenant") String tenant,
                           final @PathParam("namespace") String namespace,
                           final @PathParam("sourceName") String sourceName,
                           final @PathParam("instanceId") String instanceId) {
        source.stopFunctionInstance(tenant, namespace, sourceName, instanceId, this.uri.getRequestUri(), clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Stop all source instances", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    public void stopSource(final @PathParam("tenant") String tenant,
                           final @PathParam("namespace") String namespace,
                           final @PathParam("sourceName") String sourceName) {
        source.stopFunctionInstances(tenant, namespace, sourceName, clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Start source instance", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/{instanceId}/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public void startSource(final @PathParam("tenant") String tenant,
                            final @PathParam("namespace") String namespace,
                            final @PathParam("sourceName") String sourceName,
                            final @PathParam("instanceId") String instanceId) {
        source.startFunctionInstance(tenant, namespace, sourceName, instanceId, this.uri.getRequestUri(), clientAppId(), clientAuthData());
    }

    @POST
    @ApiOperation(value = "Start all source instances", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{sourceName}/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public void startSource(final @PathParam("tenant") String tenant,
                            final @PathParam("namespace") String namespace,
                            final @PathParam("sourceName") String sourceName) {
        source.startFunctionInstances(tenant, namespace, sourceName, clientAppId(), clientAuthData());
    }

    @GET
    @ApiOperation(
            value = "Fetches a list of supported Pulsar IO source connectors currently running in cluster mode",
            response = List.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 408, message = "Request timeout")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/builtinsources")
    public List<ConnectorDefinition> getSourceList() {
        List<ConnectorDefinition> connectorDefinitions = source.getListOfConnectors();
        List<ConnectorDefinition> retval = new ArrayList<>();
        for (ConnectorDefinition connectorDefinition : connectorDefinitions) {
            if (!StringUtils.isEmpty(connectorDefinition.getSourceClass())) {
                retval.add(connectorDefinition);
            }
        }
        return retval;
    }
}
