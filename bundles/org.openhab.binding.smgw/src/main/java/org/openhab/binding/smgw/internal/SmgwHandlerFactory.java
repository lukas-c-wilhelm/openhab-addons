/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smgw.internal;

import static org.openhab.binding.smgw.internal.SmgwBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SmgwHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class)
public class SmgwHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SMGW);
    private final HttpClientFactory clientFactory;
    private final CronScheduler cronScheduler;

    @Activate
    public SmgwHandlerFactory(@Reference HttpClientFactory clientFactory, @Reference CronScheduler cronScheduler) {
        this.cronScheduler = cronScheduler;
        this.clientFactory = clientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SMGW.equals(thingTypeUID)) {
            HttpClient client = clientFactory.createHttpClient(
                    ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null),
                    new SslContextFactory.Client(true));
            try {
                client.start();
            } catch (Exception e) {
                // catching exception is necessary due to the signature of HttpClient.start()
                logger.warn("Failed to start http client: {}", e.getMessage());
                throw new IllegalStateException("Could not create HttpClient");
            }
            return new SmgwHandler(thing, client, cronScheduler);
        }

        return null;
    }
}
