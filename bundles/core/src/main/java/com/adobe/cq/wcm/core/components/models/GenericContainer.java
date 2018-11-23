package com.adobe.cq.wcm.core.components.models;

import com.day.text.StringAbbreviator;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Created by adracea on 23/11/2018.
 */
@ConsumerType
public interface GenericContainer extends Container {

    default String getBackgroundImageSrc() { throw new UnsupportedOperationException(); }

    default String getBackgroundType() { throw new UnsupportedOperationException(); }
}
