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


import org.jnosql.artemis.RepositoryAsync;
import org.jnosql.artemis.document.DocumentTemplateAsync;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.FieldRepresentation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.jnosql.artemis.IdNotFoundException.KEY_NOT_FOUND_EXCEPTION_SUPPLIER;

/**
 * The {@link org.jnosql.artemis.RepositoryAsync} template method
 */
public abstract class AbstractDocumentRepositoryAsync<T, ID> implements RepositoryAsync<T, ID> {


    protected abstract DocumentTemplateAsync getTemplate();

    protected abstract ClassRepresentation getClassRepresentation();


    @Override
    public <S extends T> void save(S entity) {
        Objects.requireNonNull(entity, "Entity is required");
        Object id = getIdField().read(entity);
        if (isNull(id)) {
            getTemplate().insert(entity);
            return;
        }

        Consumer<Boolean> callBack = exist -> {
            if (exist) {
                getTemplate().update(entity);
            } else {
                getTemplate().insert(entity);
            }
        };
        existsById((ID) id, callBack);
    }

    @Override
    public <S extends T> void save(Iterable<S> entities) {
        Objects.requireNonNull(entities, "entities is required");
        entities.forEach(this::save);
    }


    @Override
    public void deleteById(ID id) {
        requireNonNull(id, "is is required");
        getTemplate().delete(getEntityClass(), id);
    }


    @Override
    public void existsById(ID id, Consumer<Boolean> callBack) {
        Consumer<Optional<T>> as = o -> callBack.accept(o.isPresent());
        findById(id, as);
    }


    @Override
    public void findById(ID id, Consumer<Optional<T>> callBack) {
        requireNonNull(id, "id is required");
        requireNonNull(callBack, "callBack is required");

        getTemplate().find(getEntityClass(), id, callBack);
    }

    @Override
    public void count(Consumer<Long> callback) {
        getTemplate().count(getEntityClass(), callback);
    }


    private Class<T> getEntityClass() {
        return (Class<T>) getClassRepresentation().getClassInstance();
    }

    private FieldRepresentation getIdField() {
        return getClassRepresentation().getId().orElseThrow(KEY_NOT_FOUND_EXCEPTION_SUPPLIER);
    }

}
