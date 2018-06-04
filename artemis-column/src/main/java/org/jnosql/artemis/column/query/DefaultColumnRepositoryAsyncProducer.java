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
package org.jnosql.artemis.column.query;

import org.jnosql.artemis.Converters;
import org.jnosql.artemis.RepositoryAsync;
import org.jnosql.artemis.column.ColumnRepositoryAsyncProducer;
import org.jnosql.artemis.column.ColumnTemplateAsync;
import org.jnosql.artemis.column.ColumnTemplateAsyncProducer;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.Reflections;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.Objects;

@ApplicationScoped
class DefaultColumnRepositoryAsyncProducer implements ColumnRepositoryAsyncProducer {

    @Inject
    private ClassRepresentations classRepresentations;
    @Inject
    private Reflections reflections;
    @Inject
    private Converters converters;
    @Inject
    private ColumnTemplateAsyncProducer producer;

    @Override
    public <E, ID, T extends RepositoryAsync<E, ID>> T get(Class<T> repositoryClass, ColumnFamilyManagerAsync manager) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(manager, "manager class is required");
        ColumnTemplateAsync template = producer.get(manager);
        return get(repositoryClass, template);
    }

    @Override
    public <E, ID, T extends RepositoryAsync<E, ID>> T get(Class<T> repositoryClass, ColumnTemplateAsync template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");

        ColumnRepositoryAsyncProxy handler = new ColumnRepositoryAsyncProxy<>(template,
                classRepresentations, repositoryClass, reflections, converters);
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class[]{repositoryClass},
                handler);
    }
}
