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
import org.jnosql.artemis.PreparedStatementAsync;
import org.jnosql.artemis.column.util.ConverterUtil;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.FieldRepresentation;
import org.jnosql.diana.api.column.ColumnDeleteQuery;
import org.jnosql.diana.api.column.ColumnEntity;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsync;
import org.jnosql.diana.api.column.ColumnObserverParser;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.column.ColumnQueryParserAsync;
import org.jnosql.diana.api.column.query.ColumnQueryBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


/**
 * The template method to {@link ColumnTemplateAsync}
 */
public abstract class AbstractColumnTemplateAsync implements ColumnTemplateAsync {

    private static final Consumer EMPTY = t -> {
    };
    private static final ColumnQueryParserAsync PARSER = ColumnQueryParserAsync.getParser();

    protected abstract ColumnEntityConverter getConverter();

    protected abstract ColumnFamilyManagerAsync getManager();

    protected abstract ClassRepresentations getClassRepresentations();

    protected abstract Converters getConverters();

    private ColumnObserverParser columnQueryParser;


    private ColumnObserverParser getObserver() {
        if (Objects.isNull(columnQueryParser)) {
            columnQueryParser = new ColumnMapperObserver(getClassRepresentations());
        }
        return columnQueryParser;
    }


    @Override
    public <T> void insert(T entity) {
        insert(entity, EMPTY);
    }

    @Override
    public <T> void insert(T entity, Duration ttl) {
        insert(entity, ttl, EMPTY);
    }

    @Override
    public <T> void insert(T entity, Consumer<T> callBack) {
        requireNonNull(entity, "entity is required");
        requireNonNull(callBack, "callBack is required");
        Consumer<ColumnEntity> dianaCallBack = c -> callBack.accept((T) getConverter().toEntity(entity.getClass(), c));
        getManager().insert(getConverter().toColumn(entity), dianaCallBack);
    }

    @Override
    public <T> void insert(T entity, Duration ttl, Consumer<T> callback) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl is required");
        requireNonNull(callback, "callBack is required");
        Consumer<ColumnEntity> dianaCallBack = c -> callback.accept((T) getConverter().toEntity(entity.getClass(), c));
        getManager().insert(getConverter().toColumn(entity), ttl, dianaCallBack);
    }

    @Override
    public <T> void update(T entity) {
        requireNonNull(entity, "entity is required");
        update(entity, EMPTY);
    }

    @Override
    public <T> void update(T entity, Consumer<T> callback) {
        requireNonNull(entity, "entity is required");
        requireNonNull(callback, "callBack is required");
        Consumer<ColumnEntity> dianaCallBack = c -> callback.accept((T) getConverter().toEntity(entity.getClass(), c));
        getManager().update(getConverter().toColumn(entity), dianaCallBack);
    }

    @Override
    public void delete(ColumnDeleteQuery query) {
        requireNonNull(query, "query is required");
        getManager().delete(query);
    }

    @Override
    public void delete(ColumnDeleteQuery query, Consumer<Void> callback) {
        requireNonNull(query, "query is required");
        requireNonNull(callback, "callback is required");
        getManager().delete(query, callback);
    }

    @Override
    public <T> void select(ColumnQuery query, Consumer<List<T>> callback) {
        requireNonNull(query, "query is required");
        requireNonNull(callback, "callBack is required");

        Consumer<List<ColumnEntity>> dianaCallBack = d -> callback.accept(
                d.stream()
                        .map(getConverter()::toEntity)
                        .map(o -> (T) o)
                        .collect(toList()));
        getManager().select(query, dianaCallBack);
    }

    @Override
    public <T, ID> void find(Class<T> entityClass, ID id, Consumer<Optional<T>> callback) {

        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");
        requireNonNull(callback, "callBack is required");

        ClassRepresentation classRepresentation = getClassRepresentations().get(entityClass);
        FieldRepresentation idField = classRepresentation.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classRepresentation, idField.getFieldName(), getConverters());

        ColumnQuery query = ColumnQueryBuilder.select().from(classRepresentation.getName())
                .where(idField.getName()).eq(value).build();

        singleResult(query, callback);
    }

    @Override
    public <T, ID> void delete(Class<T> entityClass, ID id, Consumer<Void> callback) {

        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");
        requireNonNull(callback, "callBack is required");

        ColumnDeleteQuery query = getDeleteQuery(entityClass, id);

        delete(query, callback);
    }


    @Override
    public <T, ID> void delete(Class<T> entityClass, ID id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");

        ColumnDeleteQuery query = getDeleteQuery(entityClass, id);

        delete(query);
    }

    @Override
    public <T> void query(String query, Consumer<List<T>> callback) {
        requireNonNull(query, "query is required");
        requireNonNull(callback, "callback is required");
        Consumer<List<ColumnEntity>> mapper = columnEntities -> {
            callback.accept(columnEntities.stream().map(c -> (T) getConverter().toEntity(c))
                    .collect(toList()));
        };
        PARSER.query(query, getManager(), mapper, getObserver());
    }

    @Override
    public <T> void singleResult(String query, Consumer<Optional<T>> callback) {
        requireNonNull(query, "query is required");
        requireNonNull(callback, "callBack is required");
        Consumer<List<ColumnEntity>> mapper = columnEntities -> {
            List<T> entities = columnEntities.stream().map(c -> (T) getConverter().toEntity(c)).collect(toList());
            if (entities.isEmpty()) {
                callback.accept(Optional.empty());
            }
            if (entities.size() == 1) {
                callback.accept(Optional.ofNullable(getConverter().toEntity(columnEntities.get(0))));
            }
            throw new UnsupportedOperationException("This query does not return a unique result: " + query);
        };
        PARSER.query(query, getManager(), mapper, getObserver());
    }

    @Override
    public PreparedStatementAsync prepare(String query) {
        requireNonNull(query, "query is required");
        return new ColumnPreparedStatementAsync(PARSER.prepare(query, getManager(), getObserver()), getConverter());
    }

    private <T, ID> ColumnDeleteQuery getDeleteQuery(Class<T> entityClass, ID id) {
        ClassRepresentation classRepresentation = getClassRepresentations().get(entityClass);
        FieldRepresentation idField = classRepresentation.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classRepresentation, idField.getFieldName(), getConverters());
        return ColumnQueryBuilder.delete().from(classRepresentation.getName())
                .where(idField.getName()).eq(value).build();
    }
}

