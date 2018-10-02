/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2018 Adobe Systems Incorporated
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.sandbox.internal.models;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.sandbox.models.PageViews;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = {PageViews.class, ComponentExporter.class},
       resourceType = PageViewsImpl.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class PageViewsImpl implements PageViews {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageViewsImpl.class);

    public final static String RESOURCE_TYPE = "core/wcm/sandbox/components/pageviews/v1/pageviews";

    // Period (in ms) after which a view is considered outdated
    private final static long PERIOD = 5*1000;

    @Self
    private SlingHttpServletRequest request;

    @ScriptVariable
    private Resource resource;

    @ScriptVariable
    private ResourceResolver resolver;

    @Override
    public int getTotal() {
        // TODO: add or remvove views only if the original request resource is a page
        // TODO: don't add/remove in case of refresh
        // TODO: move addView() and removeOldViews() to a POST servlet that is triggered by client code when the page is requested
        addView();
        removeOldViews();
        return getViews();
    }

    private void addView() {
        Map<String, Object> properties = new HashMap<>();
        long now =  Calendar.getInstance().getTimeInMillis();
        properties.put("date", now);
        try {
            resolver.create(resource, Long.toString(now), properties);
            resolver.commit();
        } catch (PersistenceException e) {
            // TODO: manage concurrent views
            LOGGER.error("Could not create a new view for the page: {}", resource.getPath(), e);
        }
    }

    private void removeOldViews() {
        Iterator<Resource> views = resource.listChildren();
        long now =  Calendar.getInstance().getTimeInMillis();
        try {
            while (views.hasNext()) {
                Resource view = views.next();
                ValueMap props = view.getValueMap();
                long viewDate = (long) props.get("date");
                if (now - viewDate > PERIOD) {
                    resolver.delete(view);
                }
            }
            resolver.commit();
        } catch (PersistenceException e) {
            LOGGER.error("Could not remove outdated page views at: {}", resource.getPath(), e);
        }
    }

    private int getViews() {
        int currentViews = 0;
        Iterator<Resource> views = resource.listChildren();
        while (views.hasNext()) {
            currentViews++;
            views.next();
        }
        return currentViews;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return resource.getResourceType();
    }

}
