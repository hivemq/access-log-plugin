/*
 * Copyright 2013 dc-square GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dcsquare.hivemq.plugin.accesslog;

import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.callback.CallbackPriority;
import com.dcsquare.hivemq.spi.callback.events.*;
import com.dcsquare.hivemq.spi.callback.exception.InvalidSubscriptionException;
import com.dcsquare.hivemq.spi.callback.exception.OnPublishReceivedException;
import com.dcsquare.hivemq.spi.callback.exception.RefusedConnectionException;
import com.dcsquare.hivemq.spi.callback.registry.CallbackRegistry;
import com.dcsquare.hivemq.spi.message.CONNECT;
import com.dcsquare.hivemq.spi.message.PUBLISH;
import com.dcsquare.hivemq.spi.message.SUBSCRIBE;
import com.dcsquare.hivemq.spi.message.UNSUBSCRIBE;
import com.dcsquare.hivemq.spi.security.ClientData;
import com.dcsquare.hivemq.spi.util.PathUtils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingLevel;
import org.pmw.tinylog.labellers.TimestampLabeller;
import org.pmw.tinylog.policies.DailyPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * This is the main class of the plugin, which adds the callbacks to necessary events, which should be logged.
 *
 * @author Dominik Obermaier
 * @author Christian Goetz
 */
public class AccessLog extends PluginEntryPoint {

    org.slf4j.Logger log = LoggerFactory.getLogger(AccessLog.class);

    /**
     * In the constructor the callbacks and the logging configuration are initialized.
     *
     * @param callbackRegistry injected HiveMQ Callback Registry
     */
    @Inject
    public AccessLog(CallbackRegistry callbackRegistry) {

        try {
            Configurator.defaultConfig()
                    .writer(new RollingFileWriter(new File(PathUtils.getHiveMQLogFolder(), "hivemq-access.log").getAbsolutePath(), 10, new TimestampLabeller("yyyMMdd"), new DailyPolicy()))
                    .level(LoggingLevel.INFO)
                    .formatPattern("{date:yyyy-MM-dd HH:mm:ss}  {message}")
                    .activate();

            callbackRegistry.addCallback(onConnectCallback());
            callbackRegistry.addCallback(disconnectCallback());
            callbackRegistry.addCallback(onSubscribe());
            callbackRegistry.addCallback(onPublish());
            callbackRegistry.addCallback(onUnsubscribeCallback());
        } catch (IOException e) {
            log.error("An error occured while configuring the AccessLogger. No Callbacks registered", e);
        }
    }

    /**
     * @return Anonymous class implementing OnUnsubscribeCallback
     */
    private OnUnsubscribeCallback onUnsubscribeCallback() {
        return new OnUnsubscribeCallback() {
            @Override
            public void onUnsubscribe(UNSUBSCRIBE unsubscribe, ClientData clientData) {
                Logger.info("{0} UNSUBSCRIBED FROM {1}", clientData.getClientId(), Arrays.toString(unsubscribe.getTopics().toArray()));
            }
        };
    }

    /**
     * @return Anonymous class implementing OnPublishReceivedCallback
     */
    private OnPublishReceivedCallback onPublish() {
        return new OnPublishReceivedCallback() {

            @Override
            public void onPublishReceived(PUBLISH publish, ClientData clientData) throws OnPublishReceivedException {
                String s = null;
                try {
                    s = new String(publish.getPayload(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Logger.info("{0} PUBLISHED MESSAGE TO {1} PAYLOAD: {2}", clientData.getClientId(), publish.getTopic(), s);
            }

            @Override
            public int priority() {
                return CallbackPriority.MEDIUM;
            }
        };

    }

    /**
     * @return Anonymous class implementing OnSubscribeCallback
     */
    private OnSubscribeCallback onSubscribe() {
        return new OnSubscribeCallback() {

            @Override
            public void onSubscribe(SUBSCRIBE subscribe, ClientData clientData) throws InvalidSubscriptionException {
                Logger.info("{0} SUBSCRIBED TO {1}", clientData.getClientId(), Arrays.toString(subscribe.getTopics().toArray()));
            }

            @Override
            public int priority() {
                return CallbackPriority.MEDIUM;
            }
        };
    }

    /**
     * @return Anonymous class implementing OnDisconnectCallback
     */
    private OnDisconnectCallback disconnectCallback() {
        return new OnDisconnectCallback() {

            @Override
            public void onDisconnect(ClientData clientData, boolean b) {
                Logger.info("{0} DISCONNECTED", clientData.getClientId());
            }

        };
    }

    /**
     * @return Anonymous class implementing OnConnectCallback
     */
    private OnConnectCallback onConnectCallback() {
        return new OnConnectCallback() {

            @Override
            public void onConnect(CONNECT connect, ClientData clientData) throws RefusedConnectionException {
                Logger.info("{0} CONNECTED", connect.getClientIdentifier());
            }

            @Override
            public int priority() {
                return CallbackPriority.MEDIUM;
            }


        };
    }
}