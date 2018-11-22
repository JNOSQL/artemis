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
import org.jnosql.artemis.IdNotFoundException;
import org.jnosql.artemis.PreparedStatement;
import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.FieldMapping;
import org.jnosql.artemis.util.ConverterUtil;
import org.jnosql.diana.api.NonUniqueResultException;
import org.jnosql.diana.api.column.ColumnDeleteQuery;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.ColumnFamilyManager;
import org.jnosql.diana.api.column.ColumnObserverParser;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.column.ColumnQueryParser;
import org.jnosql.diana.api.column.query.ColumnQueryBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * The template method to {@link ColumnTemplate}
 */
public abstract class AbstractColumnTemplate implements ColumnTemplate {


    private static final ColumnQueryParser PARSER = ColumnQueryParser.getParser();

    protected abstract ColumnEntityConverter getConverter();

    protected abstract ColumnFamilyManager getManager();

    protected abstract ColumnWorkflow getFlow();

    protected abstract ColumnEventPersistManager getEventManager();

    protected abstract ClassRepresentations getClassRepresentations();

    protected abstract Converters getConverters();

    private final UnaryOperator<ColumnEntity> insert = e -> getManager().insert(e);

    private final UnaryOperator<ColumnEntity> update = e -> getManager().update(e);

    private ColumnObserverParser observer;


    private ColumnObserverParser getObserver() {
        if (Objects.isNull(observer)) {
            observer = new ColumnMapperObserver(getClassRepresentations());
        }
        return observer;
    }

    @Override
    public <T> T insert(T entity) {
        requireNonNull(entity, "entity is required");

        return getFlow().flow(entity, insert);
    }


    @Override
    public <T> T insert(T entity, Duration ttl) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl is required");
        return getFlow().flow(entity, e -> getManager().insert(e, ttl));
    }


    @Override
    public <T> T update(T entity) {
        requireNonNull(entity, "entity is required");
        return getFlow().flow(entity, update);
    }


    @Override
    public void delete(ColumnDeleteQuery query) {
        requireNonNull(query, "query is required");
        getEventManager().firePreDeleteQuery(query);
        getManager().delete(query);
    }


    @Override
    public <T> List<T> select(ColumnQuery query) {
        requireNonNull(query, "query is required");
        getEventManager().firePreQuery(query);
        List<ColumnEntity> entities = getManager().select(query);
        Function<ColumnEntity, T> function = e -> getConverter().toEntity(e);
        return entities.stream().map(function).collect(toList());
    }

    @Override
    public <T, ID> Optional<T> find(Class<T> entityClass, ID id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");
        ClassMapping classMapping = getClassRepresentations().get(entityClass);
        FieldMapping idField = classMapping.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classMapping, idField.getFieldName(), getConverters());
        ColumnQuery query = ColumnQueryBuilder.select().from(classMapping.getName())
                .where(idField.getName()).eq(value).build();

        return singleResult(query);
    }

    @Override
    public <T, ID> void delete(Class<T> entityClass, ID id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");

        ClassMapping classMapping = getClassRepresentations().get(entityClass);
        FieldMapping idField = classMapping.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));
        Object value = ConverterUtil.getValue(id, classMapping, idField.getFieldName(), getConverters());

        ColumnDeleteQuery query = ColumnQueryBuilder.delete().from(classMapping.getName())
                .where(idField.getName()).eq(value).build();
        getManager().delete(query);
    }


    @Override
    public <T> List<T> query(String query) {
        requireNonNull(query, "query is required");
        return PARSER.query(query,getManager(), getObserver()).stream().map(c -> (T) getConverter().toEntity(c))
                .collect(toList());
    }

    @Override
    public <T> Optional<T> singleResult(String query) {
        List<T> entities = query(query);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        if (entities.size() == 1) {
            return Optional.ofNullable(entities.get(0));
        }
        throw new NonUniqueResultException("No unique result found to the query: " + query);
    }

    @Override
    public PreparedStatement prepare(String query) {
        return new ColumnPreparedStatement(PARSER.prepare(query, getManager(), getObserver()), getConverter());
    }


    @Override
    public long count(String columnFamily) {
        return getManager().count(columnFamily);
    }


    @Override
    public <T> long count(Class<T> entityClass){
        requireNonNull(entityClass, "entity class is required");
        ClassMapping classMapping = getClassRepresentations().get(entityClass);
        return getManager().count(classMapping.getName());
    }
}
