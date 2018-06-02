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


import org.jnosql.artemis.PreparedStatement;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.KeyValueEntity;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * This class provides a skeletal implementation of the {@link KeyValueTemplate} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractKeyValueTemplate implements KeyValueTemplate {

    protected abstract KeyValueEntityConverter getConverter();

    protected abstract BucketManager getManager();


    protected abstract KeyValueWorkflow getFlow();

    @Override
    public <T> T put(T entity) {
        requireNonNull(entity, "entity is required");

        UnaryOperator<KeyValueEntity<?>> putAction = k -> {
            getManager().put(k);
            return k;

        };
        return getFlow().flow(entity, putAction);
    }

    @Override
    public <T> T put(T entity, Duration ttl) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl class is required");

        UnaryOperator<KeyValueEntity<?>> putAction = k -> {
            getManager().put(k, ttl);
            return k;

        };
        return getFlow().flow(entity, putAction);
    }

    @Override
    public <K, T> Optional<T> get(K key, Class<T> clazz) {
        requireNonNull(key, "key is required");
        requireNonNull(clazz, "entity class is required");

        Optional<Value> value = getManager().get(key);
        return value.map(v -> getConverter().toEntity(clazz, v))
                .filter(Objects::nonNull);
    }

    @Override
    public <K, T> Iterable<T> get(Iterable<K> keys, Class<T> clazz) {
        requireNonNull(keys, "keys is required");
        requireNonNull(clazz, "entity class is required");
        return StreamSupport.stream(getManager()
                .get(keys).spliterator(), false)
                .map(v -> getConverter().toEntity(clazz, v))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public <K> void remove(K key) {
        requireNonNull(key, "key is required");
        getManager().remove(key);
    }

    @Override
    public <K> void remove(Iterable<K> keys) {
        requireNonNull(keys, "keys is required");
        getManager().remove(keys);
    }

    @Override
    public <T> List<T> query(String query, Class<T> entityClass) {
        requireNonNull(query, "query is required");
        List<Value> values = getManager().query(query);
        if (!values.isEmpty()) {
            requireNonNull(entityClass, "entityClass is required");
            return values.stream().map(v -> v.get(entityClass)).collect(toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void query(String query) {
        requireNonNull(query, "query is required");
        getManager().query(query);
    }

    @Override
    public <T> PreparedStatement prepare(String query, Class<T> entityClass) {
        requireNonNull(query, "query is required");
        return new org.jnosql.artemis.key.KeyValuePreparedStatement(getManager().prepare(query), entityClass);
    }
}
