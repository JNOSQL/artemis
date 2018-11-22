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
package org.jnosql.artemis.document.query;


import org.jnosql.artemis.Converters;
import org.jnosql.artemis.RepositoryAsync;
import org.jnosql.artemis.document.DocumentTemplateAsync;
import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.artemis.reflection.ClassRepresentations;

import java.lang.reflect.ParameterizedType;

/**
 * Proxy handle to generate {@link org.jnosql.artemis.RepositoryAsync}
 *
 * @param <T> the type
 */
class DocumentRepositoryAsyncProxy<T> extends AbstractDocumentRepositoryAsyncProxy {

    private final DocumentTemplateAsync template;


    private final DocumentRepositoryAsync repository;

    private final ClassMapping classMapping;


    private final Converters converters;



    DocumentRepositoryAsyncProxy(DocumentTemplateAsync template, ClassRepresentations classRepresentations,
                                 Class<?> repositoryType, Converters converters) {
        this.template = template;
        Class<T> typeClass = Class.class.cast(ParameterizedType.class.cast(repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0]);
        this.classMapping = classRepresentations.get(typeClass);
        this.repository = new DocumentRepositoryAsync(template, classMapping);
        this.converters = converters;
    }

    @Override
    protected RepositoryAsync getRepository() {
        return repository;
    }


    @Override
    protected DocumentTemplateAsync getTemplate() {
        return template;
    }

    @Override
    protected ClassMapping getClassMapping() {
        return classMapping;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }

    class DocumentRepositoryAsync extends AbstractDocumentRepositoryAsync implements RepositoryAsync {

        private final DocumentTemplateAsync template;

        private final ClassMapping classMapping;

        DocumentRepositoryAsync(DocumentTemplateAsync template,
                                ClassMapping classMapping) {

            this.template = template;
            this.classMapping = classMapping;
        }

        @Override
        protected DocumentTemplateAsync getTemplate() {
            return template;
        }

        @Override
        protected ClassMapping getClassMapping() {
            return classMapping;
        }

    }
}
