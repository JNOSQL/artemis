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
package org.jnosql.artemis.graph.query;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jnosql.artemis.graph.GraphRepositoryProducer;
import org.jnosql.artemis.graph.cdi.CDIExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(CDIExtension.class)
class DefaultGraphRepositoryProducerTest {


    @Inject
    private GraphRepositoryProducer producer;


    @Test
    public void shouldCreateFromManager() {
        Graph manager= Mockito.mock(Graph.class);
        GraphRepositoryProxyTest.PersonRepository personRepository = producer.get(GraphRepositoryProxyTest.PersonRepository.class, manager);
        assertNotNull(personRepository);
    }



}