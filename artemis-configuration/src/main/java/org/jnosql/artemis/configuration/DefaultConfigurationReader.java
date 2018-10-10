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
package org.jnosql.artemis.configuration;

import org.jnosql.artemis.ConfigurationReader;
import org.jnosql.artemis.ConfigurationSettingsUnit;
import org.jnosql.artemis.ConfigurationUnit;
import org.jnosql.artemis.reflection.ConstructorException;
import org.jnosql.artemis.reflection.Reflections;
import org.jnosql.diana.api.Settings;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.jnosql.artemis.util.StringUtils.isBlank;

/**
 * The default class to {@link ConfigurationReader}
 */
@ApplicationScoped
class DefaultConfigurationReader implements ConfigurationReader {

    private static final String META_INF = "META-INF/";

    private static final String WEB_INF = "WEB-INF/";

    private static final Logger LOGGER = Logger.getLogger(DefaultConfigurationReader.class.getName());

    @Inject
    private Reflections reflections;

    @Inject
    @Any
    private Instance<ConfigurableReader> readers;

    @Override
    public <T> ConfigurationSettingsUnit read(ConfigurationUnit annotation, Class<T> configurationClass) {

        requireNonNull(annotation, "annotation is required");
        requireNonNull(configurationClass, "configurationClass is required");


        List<Configurable> configurations = getConfigurations(annotation);
        Configurable configuration = getConfiguration(annotation, configurations);

        String name = configuration.getName();
        String description = configuration.getDescription();
        Map<String, Object> settings = new HashMap<>(ofNullable(configuration.getSettings()).orElse(emptyMap()));
        settings.putAll(System.getenv());
        System.getProperties().forEach((k,v) -> settings.put(k.toString(), v));
        Class<?> provider = getProvider(configurationClass, configuration);

        return new DefaultConfigurationSettingsUnit(name, description, provider, Settings.of(settings));
    }


    @Override
    public <T> ConfigurationSettingsUnit read(ConfigurationUnit annotation) {

        requireNonNull(annotation, "annotation is required");

        List<Configurable> configurations = getConfigurations(annotation);
        Configurable configuration = getConfiguration(annotation, configurations);

        String name = configuration.getName();
        String description = configuration.getDescription();
        Map<String, Object> settings = new HashMap<>(ofNullable(configuration.getSettings()).orElse(emptyMap()));

        return new DefaultConfigurationSettingsUnit(name, description, null, Settings.of(settings));
    }


    private List<Configurable> getConfigurations(ConfigurationUnit annotation) {
        Supplier<InputStream> stream = readStream(annotation);
        String extension = getExtension(annotation);
        Instance<ConfigurableReader> select = readers.select(new NamedLiteral(extension));
        if (select.isUnsatisfied()) {
            throw new ConfigurationException(String.format("The extension %s is not supported", extension));
        }


        return select.get().read(stream, annotation);
    }

    private String getExtension(ConfigurationUnit annotation) {
        String[] fileName = annotation.fileName().split("\\.");
        if (fileName.length != 2) {
            throw new ConfigurationException("The cofinguration file is invalid: " + annotation.fileName());
        }
        return fileName[1];
    }

    private <T> Class<?> getProvider(Class<T> configurationClass, Configurable configuration) {
        if (isBlank(configuration.getProvider())) {
            return null;
        }
        try {
            Class<?> provider = Class.forName(configuration.getProvider());
            if (!configurationClass.isAssignableFrom(provider)) {
                throw new ConfigurationException(String.format("The class %s does not match with %s",
                        provider.toString(), configurationClass));
            }
            reflections.makeAccessible(provider);
            return provider;
        } catch (ClassNotFoundException| ConstructorException e) {
            throw new ConfigurationException("An error to load the provider class: " + configuration.getProvider(), e);
        }
    }

    private Configurable getConfiguration(ConfigurationUnit annotation, List<Configurable> configurations) {


        String name = annotation.name();
        String fileName = annotation.fileName();
        if (isBlank(name)) {

            if (configurations.size() > 1) {
                throw new ConfigurationException(String.format("An ambitious error happened once the file %s has" +
                        " more than one configuration unit.", name));
            }

            return configurations.stream()
                    .findFirst()
                    .orElseThrow(() -> new ConfigurationException("There is not unit in the file: " + fileName));
        }
        return configurations.stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new ConfigurationException(format("The unit %s does not find in the file %s"
                        , name, fileName)));
    }


    private Supplier<InputStream> readStream(ConfigurationUnit annotation) {
        return () -> {
            String metaInfFile = META_INF + annotation.fileName();


            LOGGER.fine("Reading the configuration file: " + metaInfFile);

            InputStream stream = DefaultConfigurationReader.class.getClassLoader().getResourceAsStream(metaInfFile);

            if (isNull(stream)) {
                String webInfFile = WEB_INF + annotation.fileName();
                LOGGER.fine("Reading the configuration file: " + webInfFile);
                stream = DefaultConfigurationReader.class.getClassLoader().getResourceAsStream(webInfFile);
            }
            return ofNullable(stream)
                    .orElseThrow(() -> new ConfigurationException("The File does not found at: " + annotation.fileName()));
        };
    }
}
