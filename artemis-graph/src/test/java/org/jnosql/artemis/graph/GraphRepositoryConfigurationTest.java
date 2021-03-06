/*
 *  Copyright (c) 2019 Otávio Santana and others
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
package org.jnosql.artemis.graph;


import org.jnosql.artemis.graph.cdi.CDIExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(CDIExtension.class)
public class GraphRepositoryConfigurationTest {


    @Inject
    private RepositoryService services;

    @Test
    public void shouldRepository() {
        GraphRepositorySupplier<BookRepository> supplier = services.getRepository();
        assertNotNull(supplier);
        BookRepository repository = supplier.get();
        assertNotNull(repository);

    }
}
