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
package com.adobe.cq.wcm.core.components.sandbox.internal.servlets;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.wcm.core.components.sandbox.internal.models.PageViewsImpl;

/**
 * Servlet that adds/removes page views below the pageviews resource
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.methods=" + HttpConstants.METHOD_POST,
        "sling.servlet.resourceTypes=" + PageViewsImpl.RESOURCE_TYPE,
        "sling.servlet.selectors=" + PageViewsServlet.SELECTOR,
        "sling.servlet.extensions=" + PageViewsServlet.EXTENSION
    }
)
public class PageViewsServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageViewsServlet.class);

    protected static final String SELECTOR = "pageviews";
    protected static final String EXTENSION = "html";

    private static final String PARAM_ADD_NEW_VIEW = "add";
    private static final String PARAM_REMOVE_OLD_VIEWS = "remove";

    // Period (in ms) after which a view is considered outdated
    private final static long PERIOD = 5*1000;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          final SlingHttpServletResponse response)
        throws ServletException, IOException {

        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = request.getResource();

        // Add a new page view

        String addPageView = request.getParameter(PARAM_ADD_NEW_VIEW);
        if (addPageView != null) {
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

        // Remove outdated page views

        String removePageViews = request.getParameter(PARAM_REMOVE_OLD_VIEWS);
        if (removePageViews != null) {
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
    }

}
