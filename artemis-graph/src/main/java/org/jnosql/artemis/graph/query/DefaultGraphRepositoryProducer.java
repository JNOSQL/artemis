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
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.Repository;
import org.jnosql.artemis.graph.GraphConverter;
import org.jnosql.artemis.graph.GraphRepositoryProducer;
import org.jnosql.artemis.graph.GraphTemplate;
import org.jnosql.artemis.graph.GraphTemplateProducer;
import org.jnosql.artemis.reflection.ClassRepresentations;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.Objects;

@ApplicationScoped
class DefaultGraphRepositoryProducer implements GraphRepositoryProducer {

    @Inject
    private ClassRepresentations classRepresentations;

    @Inject
    private GraphConverter converter;

    @Inject
    private GraphTemplateProducer producer;

    @Inject
    private Converters converters;

    @Override
    public <E, ID, T extends Repository<E, ID>> T get(Class<T> repositoryClass, Graph manager) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(manager, "manager class is required");
        GraphTemplate template = producer.get(manager);
        GraphRepositoryProxy<T, ID> handler = new GraphRepositoryProxy(template,
                classRepresentations, repositoryClass, manager, converter, converters);
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class[]{repositoryClass},
                handler);
    }

}
