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
package org.jnosql.artemis.key;

import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.ConfigurationUnit;
import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.key.BucketManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@ExtendWith(CDIExtension.class)
public class BucketManagerConfigurationFactoryXMLTest {

    @Inject
    @ConfigurationUnit(fileName = "key-value.xml", name = "name")
    private BucketManagerFactory<?> factoryA;

    @Inject
    @ConfigurationUnit(fileName = "key-value.xml", name = "name-2")
    private BucketManagerFactory factoryB;


    @Test
    public void shouldReadBucketManager() {
        factoryA.getBucketManager("database");
        Assertions.assertTrue(KeyValueConfigurationMock.BucketManagerFactoryMock.class.isInstance(factoryA));
        KeyValueConfigurationMock.BucketManagerFactoryMock mock = KeyValueConfigurationMock.BucketManagerFactoryMock.class.cast(factoryA);
        Map<String, Object> settings = new HashMap<>();
        settings.put("key","value");
        settings.put("key2","value2");
        assertEquals(Settings.of(settings), mock.getSettings());
    }

    @Test
    public void shouldReadBucketManagerB() {
        factoryB.getBucketManager("database");
        Assertions.assertTrue(KeyValueConfigurationMock.BucketManagerFactoryMock.class.isInstance(factoryB));
        KeyValueConfigurationMock.BucketManagerFactoryMock mock = KeyValueConfigurationMock.BucketManagerFactoryMock.class.cast(factoryB);
        Map<String, Object> settings = new HashMap<>();
        settings.put("key","value");
        settings.put("key2","value2");
        settings.put("key3","value3");
        assertEquals(Settings.of(settings), mock.getSettings());
    }
}