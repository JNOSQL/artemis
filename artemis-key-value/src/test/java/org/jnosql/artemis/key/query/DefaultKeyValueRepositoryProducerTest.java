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
package org.jnosql.artemis.key.query;

import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.PersonRepository;
import org.jnosql.artemis.key.KeyValueRepositoryProducer;
import org.jnosql.artemis.key.KeyValueTemplate;
import org.jnosql.diana.api.key.BucketManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(CDIExtension.class)
class DefaultKeyValueRepositoryProducerTest {

    @Inject
    private KeyValueRepositoryProducer producer;


    @Test
    public void shouldCreateFromManager() {
        BucketManager manager= Mockito.mock(BucketManager.class);
        PersonRepository personRepository = producer.get(PersonRepository.class, manager);
        assertNotNull(personRepository);
    }


    @Test
    public void shouldCreateFromTemplate() {
        KeyValueTemplate template= Mockito.mock(KeyValueTemplate.class);
        PersonRepository personRepository = producer.get(PersonRepository.class, template);
        assertNotNull(personRepository);
    }

}