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
package org.jnosql.artemis.graph;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jnosql.artemis.Repository;


/**
 * The producer of {@link Repository}
 *
 */
public interface GraphRepositoryProducer {

    /**
     * Produces a Repository class from repository class and {@link Graph}
     * @param repositoryClass the repository class
     * @param graph the graph
     * @param <E> the entity of repository
     * @param <ID> the ID of the entity
     * @param <T> the repository type
     * @return a {@link Repository} interface
     * @throws NullPointerException when there is null parameter
     */
    <E,ID, T extends Repository<E,ID>> T get(Class<T> repositoryClass, Graph graph);

}
