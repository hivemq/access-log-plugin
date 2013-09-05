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

import com.dcsquare.hivemq.spi.HiveMQPluginModule;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.config.Configurations;
import com.dcsquare.hivemq.spi.plugin.meta.Information;
import com.google.inject.Provider;
import org.apache.commons.configuration.AbstractConfiguration;

/**
 * This is the plugin starting point and configuration class.
 *
 * @author Dominik Obermaier
 * @author Christian Goetz
 */
@Information(name = "Access Log Plugin", version = "1.1")
public class AccessLogModule extends HiveMQPluginModule {

    @Override
    public Provider<Iterable<? extends AbstractConfiguration>> getConfigurations() {
        return Configurations.noConfigurationNeeded();
    }

    @Override
    protected void configurePlugin() {

    }

    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return AccessLog.class;
    }
}
