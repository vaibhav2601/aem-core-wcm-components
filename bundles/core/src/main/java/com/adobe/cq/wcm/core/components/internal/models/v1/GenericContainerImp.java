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
package com.adobe.cq.wcm.core.components.internal.models.v1;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ContainerExporter;
import com.adobe.cq.wcm.core.components.models.Carousel;
import com.adobe.cq.wcm.core.components.models.GenericContainer;
import com.day.cq.commons.DownloadResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.components.Component;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = SlingHttpServletRequest.class, adapters = GenericContainer.class, resourceType = GenericContainerImp.RESOURCE_TYPE)
public class GenericContainerImp extends AbstractImageDelegatingModel implements GenericContainer{

    public static final String RESOURCE_TYPE = "core/wcm/components/container/v1/container";

    private final List<String> hiddenImageResourceProperties = new ArrayList<String>() {{
        add(JcrConstants.JCR_TITLE);
        add(JcrConstants.JCR_DESCRIPTION);
    }};

    @ScriptVariable
    private Component component;

    @ScriptVariable
    private ValueMap properties;

    @Self
    private SlingHttpServletRequest request;

    @Inject
    protected Resource resource;

    private String backgroundImageSrc;

    @ValueMapValue(optional = true)
    private String backgroundColor;

    @ValueMapValue(optional = true)
    private String backgroundType;

    @ValueMapValue(optional = true)
    private String genericContainerId;

    @PostConstruct
    private void initModel() {
        if(backgroundType != null && backgroundType.equals("backgroundImage"))
        {
            String fileReference = properties.get(DownloadResource.PN_REFERENCE, String.class);

            setImageResource(component, request.getResource(), hiddenImageResourceProperties);

            //ValueMap properties = resource.getValueMap();
            //String fileReference = properties.get("fileReference", String.class);
            if(fileReference != null && !fileReference.isEmpty())
            {
                backgroundImageSrc = fileReference;
            }
            else
            {
                Resource fileResource = resource.getChild("file");
                if(fileResource != null)
                {
                    backgroundImageSrc = fileResource.getPath();
                }
            }

        }

    }

    public String getStyleString()
    {
        String backgroundStyle = "background-color: " + backgroundColor + ";";
        //String backgroundStyle = "background: url('" + backgroundImageSrc + "') center; background-size: cover;";;

        StringBuilder sb = new StringBuilder();
        sb
            .append(backgroundStyle).append(" ")
            .append(backgroundStyle).append(" ");

        return sb.toString();
    }

    @Override
    public String getBackgroundImageSrc() {
        return backgroundImageSrc;
    }

    @Override
    public String getBackgroundType() {
        return backgroundType;
    }

    public String getGenericContainerId() {
        return genericContainerId;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
}
