/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.xslt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedOperation;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.builder.xml.ResultHandlerFactory;
import org.apache.camel.builder.xml.XsltBuilder;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedResource(description = "Managed XsltEndpoint")
@UriEndpoint(scheme = "xslt", label = "core,transformation")
public class XsltEndpoint extends ProcessorEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(XsltEndpoint.class);
    private static final String SAXON_TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";

    private volatile boolean cacheCleared;
    private volatile XsltBuilder xslt;
    private Map<String, Object> parameters;

    @UriPath
    private String resourceUri;
    @UriParam(defaultValue = "true")
    private boolean contentCache = true;
    @UriParam
    private XmlConverter converter;
    @UriParam
    private String transformerFactoryClass;
    @UriParam
    private TransformerFactory transformerFactory;
    @UriParam(defaultValue = "false")
    private boolean saxon;
    @UriParam
    private ResultHandlerFactory resultHandlerFactory;
    @UriParam(defaultValue = "true")
    private boolean failOnNullBody = true;
    @UriParam(defaultValue = "string")
    private XsltOutput output = XsltOutput.string;
    @UriParam(defaultValue = "0")
    private int transformerCacheSize;
    @UriParam
    private ErrorListener errorListener;
    @UriParam
    private URIResolver uriResolver;
    @UriParam(defaultValue = "true")
    private boolean allowStAX = true;
    @UriParam(defaultValue = "false")
    private boolean deleteOutputFile;

    @Deprecated
    public XsltEndpoint(String endpointUri, Component component, XsltBuilder xslt, String resourceUri,
            boolean cacheStylesheet) throws Exception {
        super(endpointUri, component, xslt);
        this.xslt = xslt;
        this.resourceUri = resourceUri;
        this.contentCache = cacheStylesheet;
    }

    public XsltEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    @ManagedOperation(description = "Clears the cached XSLT stylesheet, forcing to re-load the stylesheet on next request")
    public void clearCachedStylesheet() {
        this.cacheCleared = true;
    }

    @ManagedAttribute(description = "Whether the XSLT stylesheet is cached")
    public boolean isCacheStylesheet() {
        return contentCache;
    }

    @ManagedAttribute(description = "Endpoint State")
    public String getState() {
        return getStatus().name();
    }

    @ManagedAttribute(description = "Camel ID")
    public String getCamelId() {
        return getCamelContext().getName();
    }

    @ManagedAttribute(description = "Camel ManagementName")
    public String getCamelManagementName() {
        return getCamelContext().getManagementName();
    }

    public XsltEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(resourceUri, newResourceUri);
        LOG.trace("Getting endpoint with URI: {}", newUri);
        return getCamelContext().getEndpoint(newUri, XsltEndpoint.class);
    }

    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        if (!contentCache || cacheCleared) {
            loadResource(resourceUri);
        }
        super.onExchange(exchange);
    }

    public boolean isCacheCleared() {
        return cacheCleared;
    }

    public void setCacheCleared(boolean cacheCleared) {
        this.cacheCleared = cacheCleared;
    }

    public XsltBuilder getXslt() {
        return xslt;
    }

    public void setXslt(XsltBuilder xslt) {
        this.xslt = xslt;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public XmlConverter getConverter() {
        return converter;
    }

    public void setConverter(XmlConverter converter) {
        this.converter = converter;
    }

    public String getTransformerFactoryClass() {
        return transformerFactoryClass;
    }

    public void setTransformerFactoryClass(String transformerFactoryClass) {
        this.transformerFactoryClass = transformerFactoryClass;
    }

    public TransformerFactory getTransformerFactory() {
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public boolean isSaxon() {
        return saxon;
    }

    public void setSaxon(boolean saxon) {
        this.saxon = saxon;
    }

    public ResultHandlerFactory getResultHandlerFactory() {
        return resultHandlerFactory;
    }

    public void setResultHandlerFactory(ResultHandlerFactory resultHandlerFactory) {
        this.resultHandlerFactory = resultHandlerFactory;
    }

    public boolean isFailOnNullBody() {
        return failOnNullBody;
    }

    public void setFailOnNullBody(boolean failOnNullBody) {
        this.failOnNullBody = failOnNullBody;
    }

    public XsltOutput getOutput() {
        return output;
    }

    public void setOutput(XsltOutput output) {
        this.output = output;
    }

    public int getTransformerCacheSize() {
        return transformerCacheSize;
    }

    public void setTransformerCacheSize(int transformerCacheSize) {
        this.transformerCacheSize = transformerCacheSize;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public boolean isContentCache() {
        return contentCache;
    }

    public void setContentCache(boolean contentCache) {
        this.contentCache = contentCache;
    }

    public URIResolver getUriResolver() {
        return uriResolver;
    }

    public void setUriResolver(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    public boolean isAllowStAX() {
        return allowStAX;
    }

    public void setAllowStAX(boolean allowStAX) {
        this.allowStAX = allowStAX;
    }

    public boolean isDeleteOutputFile() {
        return deleteOutputFile;
    }

    public void setDeleteOutputFile(boolean deleteOutputFile) {
        this.deleteOutputFile = deleteOutputFile;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Loads the resource.
     *
     * @param resourceUri  the resource to load
     * @throws TransformerException is thrown if error loading resource
     * @throws IOException is thrown if error loading resource
     */
    protected void loadResource(String resourceUri) throws TransformerException, IOException {
        LOG.trace("{} loading schema resource: {}", this, resourceUri);
        Source source = xslt.getUriResolver().resolve(resourceUri, null);
        if (source == null) {
            throw new IOException("Cannot load schema resource " + resourceUri);
        } else {
            xslt.setTransformerSource(source);
        }
        // now loaded so clear flag
        cacheCleared = false;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        LOG.debug("{} using schema resource: {}", this, resourceUri);

        this.xslt = getCamelContext().getInjector().newInstance(XsltBuilder.class);
        if (converter != null) {
            xslt.setConverter(converter);
        }

        if (transformerFactoryClass == null && saxon) {
            transformerFactoryClass = SAXON_TRANSFORMER_FACTORY_CLASS_NAME;
        }

        TransformerFactory factory = transformerFactory;
        if (factory == null && transformerFactoryClass != null) {
            // provide the class loader of this component to work in OSGi environments
            Class<?> factoryClass = getCamelContext().getClassResolver().resolveMandatoryClass(transformerFactoryClass, XsltComponent.class.getClassLoader());
            LOG.debug("Using TransformerFactoryClass {}", factoryClass);
            factory = (TransformerFactory) getCamelContext().getInjector().newInstance(factoryClass);
        }

        if (factory != null) {
            LOG.debug("Using TransformerFactory {}", factory);
            xslt.getConverter().setTransformerFactory(factory);
        }
        if (resultHandlerFactory != null) {
            xslt.setResultHandlerFactory(resultHandlerFactory);
        }
        if (errorListener != null) {
            xslt.errorListener(errorListener);
        }
        xslt.setFailOnNullBody(failOnNullBody);
        xslt.transformerCacheSize(transformerCacheSize);
        xslt.setUriResolver(uriResolver);
        xslt.setAllowStAX(allowStAX);
        xslt.setDeleteOutputFile(deleteOutputFile);

        configureOutput(xslt, output.name());

        // any additional transformer parameters then make a copy to avoid side-effects
        if (parameters != null) {
            Map<String, Object> copy = new HashMap<>(parameters);
            xslt.setParameters(copy);
        }

        // must load resource first which sets a template and do a stylesheet compilation to catch errors early
        loadResource(resourceUri);

        // and then inject camel context and start service
        xslt.setCamelContext(getCamelContext());

        // the processor is the xslt builder
        setProcessor(xslt);

        ServiceHelper.startService(xslt);
    }

    protected void configureOutput(XsltBuilder xslt, String output) throws Exception {
        if (ObjectHelper.isEmpty(output)) {
            return;
        }

        if ("string".equalsIgnoreCase(output)) {
            xslt.outputString();
        } else if ("bytes".equalsIgnoreCase(output)) {
            xslt.outputBytes();
        } else if ("DOM".equalsIgnoreCase(output)) {
            xslt.outputDOM();
        } else if ("file".equalsIgnoreCase(output)) {
            xslt.outputFile();
        } else {
            throw new IllegalArgumentException("Unknown output type: " + output);
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        ServiceHelper.stopService(xslt);
    }
}
