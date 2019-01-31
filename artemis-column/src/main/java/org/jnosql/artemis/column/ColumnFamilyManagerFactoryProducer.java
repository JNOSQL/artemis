/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.column;

import org.jnosql.artemis.ConfigurationReader;
import org.jnosql.artemis.ConfigurationSettingsUnit;
import org.jnosql.artemis.ConfigurationUnit;
import org.jnosql.artemis.reflection.Reflections;
import org.jnosql.diana.api.column.ColumnConfiguration;
import org.jnosql.diana.api.column.ColumnConfigurationAsync;
import org.jnosql.diana.api.column.ColumnFamilyManager;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsync;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsyncFactory;
import org.jnosql.diana.api.column.ColumnFamilyManagerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static org.jnosql.artemis.util.ConfigurationUnitUtils.getConfigurationUnit;

/**
 * The class that creates {@link ColumnFamilyManagerFactory} and {@link ColumnFamilyManagerAsyncFactory}
 * from the {@link ConfigurationUnit}
 */
@ApplicationScoped
class ColumnFamilyManagerFactoryProducer {

    @Inject
    private Reflections reflections;

    @Inject
    private Instance<ConfigurationReader> configurationReader;


    @ConfigurationUnit
    @Produces
    public <T extends ColumnFamilyManager> ColumnFamilyManagerFactory<T> getColumnConfigurationGenerics(InjectionPoint injectionPoint) {
        return gettColumnFamilyManagerFactory(injectionPoint);
    }

    @ConfigurationUnit
    @Produces
    public ColumnFamilyManagerFactory getColumnConfiguration(InjectionPoint injectionPoint) {
        return gettColumnFamilyManagerFactory(injectionPoint);
    }


    @ConfigurationUnit
    @Produces
    public <T extends ColumnFamilyManagerAsync> ColumnFamilyManagerAsyncFactory<T> getColumnConfigurationAsyncGeneric(InjectionPoint injectionPoint) {
        return gettColumnFamilyManagerAsyncFactory(injectionPoint);
    }


    @ConfigurationUnit
    @Produces
    public ColumnFamilyManagerAsyncFactory getColumnConfigurationAsync(InjectionPoint injectionPoint) {
        return gettColumnFamilyManagerAsyncFactory(injectionPoint);
    }


    private <T extends ColumnFamilyManagerAsync> ColumnFamilyManagerAsyncFactory<T> gettColumnFamilyManagerAsyncFactory(InjectionPoint injectionPoint) {
        Annotated annotated = injectionPoint.getAnnotated();

        ConfigurationUnit annotation = getConfigurationUnit(injectionPoint, annotated);

        ConfigurationSettingsUnit unit = configurationReader.get().read(annotation, ColumnConfigurationAsync.class);
        Class<ColumnConfigurationAsync> configurationClass = unit.<ColumnConfigurationAsync>getProvider()
                .orElseThrow(() -> new IllegalStateException("The ColumnConfiguration provider is required in the configuration"));

        ColumnConfigurationAsync columnConfiguration = reflections.newInstance(configurationClass);

        return columnConfiguration.getAsync(unit.getSettings());
    }

    private <T extends ColumnFamilyManager> ColumnFamilyManagerFactory<T> gettColumnFamilyManagerFactory(InjectionPoint injectionPoint) {
        Annotated annotated = injectionPoint.getAnnotated();

        ConfigurationUnit annotation = getConfigurationUnit(injectionPoint, annotated);

        ConfigurationSettingsUnit unit = configurationReader.get().read(annotation, ColumnConfiguration.class);
        Class<ColumnConfiguration> configurationClass = unit.<ColumnConfiguration>getProvider()
                .orElseThrow(() -> new IllegalStateException("The ColumnConfiguration provider is required in the configuration"));

        ColumnConfiguration configuration = reflections.newInstance(configurationClass);

        return configuration.get(unit.getSettings());
    }
}
