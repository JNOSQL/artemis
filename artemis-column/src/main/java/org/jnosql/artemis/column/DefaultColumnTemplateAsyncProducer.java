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


import org.jnosql.artemis.Converters;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import java.util.Objects;

/**
 * The default implementation of {@link ColumnTemplateAsyncProducer}
 */
@ApplicationScoped
class DefaultColumnTemplateAsyncProducer implements ColumnTemplateAsyncProducer<ColumnTemplateAsync> {

    @Inject
    private ColumnEntityConverter converter;

    @Inject
    private ClassRepresentations classRepresentations;

    @Inject
    private Converters converters;

    @Override
    public ColumnTemplateAsync get(ColumnFamilyManagerAsync columnFamilyManager) {
        Objects.requireNonNull(columnFamilyManager, "columnFamilyManager is required");
        return new ProducerColumnTemplateAsync(converter, columnFamilyManager, classRepresentations, converters);
    }

    @Vetoed
    static class ProducerColumnTemplateAsync extends AbstractColumnTemplateAsync {

        private ColumnEntityConverter converter;

        private ColumnFamilyManagerAsync columnFamilyManager;

        private ClassRepresentations classRepresentations;

        private Converters converters;

        ProducerColumnTemplateAsync(ColumnEntityConverter converter, ColumnFamilyManagerAsync columnFamilyManager
        ,ClassRepresentations classRepresentations, Converters converters) {
            this.converter = converter;
            this.columnFamilyManager = columnFamilyManager;
            this.classRepresentations = classRepresentations;
            this.converters = converters;
        }

        ProducerColumnTemplateAsync() {
        }

        @Override
        protected ColumnEntityConverter getConverter() {
            return converter;
        }

        @Override
        protected ColumnFamilyManagerAsync getManager() {
            return columnFamilyManager;
        }

        @Override
        protected ClassRepresentations getClassRepresentations() {
            return classRepresentations;
        }

        @Override
        protected Converters getConverters() {
            return converters;
        }
    }
}
