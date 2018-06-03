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
package org.jnosql.artemis.document;


import org.jnosql.artemis.Converters;
import org.jnosql.artemis.IdNotFoundException;
import org.jnosql.artemis.PreparedStatement;
import org.jnosql.artemis.document.util.ConverterUtil;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.reflection.FieldRepresentation;
import org.jnosql.diana.api.NonUniqueResultException;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentObserverParser;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.DocumentQueryParser;
import org.jnosql.diana.api.document.query.DocumentQueryBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * This class provides a skeletal implementation of the {@link DocumentTemplate} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractDocumentTemplate implements DocumentTemplate {


    private static final DocumentQueryParser PARSER = DocumentQueryParser.getParser();

    protected abstract DocumentEntityConverter getConverter();

    protected abstract DocumentCollectionManager getManager();

    protected abstract DocumentWorkflow getWorkflow();

    protected abstract DocumentEventPersistManager getPersistManager();

    protected abstract ClassRepresentations getClassRepresentations();

    protected abstract Converters getConverters();

    private final UnaryOperator<DocumentEntity> insert = e -> getManager().insert(e);

    private final UnaryOperator<DocumentEntity> update = e -> getManager().update(e);

    private DocumentObserverParser columnQueryParser;


    private DocumentObserverParser getObserver() {
        if (Objects.isNull(columnQueryParser)) {
            columnQueryParser = new DocumentMapperObserver(getClassRepresentations());
        }
        return columnQueryParser;
    }


    @Override
    public <T> T insert(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getWorkflow().flow(entity, insert);
    }


    @Override
    public <T> T insert(T entity, Duration ttl) {
        Objects.requireNonNull(entity, "entity is required");
        Objects.requireNonNull(ttl, "ttl is required");
        return getWorkflow().flow(entity, e -> getManager().insert(e, ttl));
    }


    @Override
    public <T> T update(T entity) {
        Objects.requireNonNull(entity, "entity is required");
        return getWorkflow().flow(entity, update);
    }


    @Override
    public void delete(DocumentDeleteQuery query) {
        Objects.requireNonNull(query, "query is required");
        getPersistManager().firePreDeleteQuery(query);
        getManager().delete(query);
    }

    @Override
    public <T> List<T> select(DocumentQuery query) {
        Objects.requireNonNull(query, "query is required");
        getPersistManager().firePreQuery(query);
        List<DocumentEntity> entities = getManager().select(query);
        Function<DocumentEntity, T> function = e -> getConverter().toEntity(e);
        return entities.stream().map(function).collect(Collectors.toList());
    }

    @Override
    public <T, ID> Optional<T> find(Class<T> entityClass, ID id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");
        ClassRepresentation classRepresentation = getClassRepresentations().get(entityClass);
        FieldRepresentation idField = classRepresentation.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classRepresentation, idField.getFieldName(), getConverters());
        DocumentQuery query = DocumentQueryBuilder.select().from(classRepresentation.getName())
                .where(idField.getName()).eq(value).build();

        return singleResult(query);
    }

    @Override
    public <T, ID> void delete(Class<T> entityClass, ID id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");

        ClassRepresentation classRepresentation = getClassRepresentations().get(entityClass);
        FieldRepresentation idField = classRepresentation.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classRepresentation, idField.getFieldName(), getConverters());
        DocumentDeleteQuery query = DocumentQueryBuilder.delete().from(classRepresentation.getName())
                .where(idField.getName()).eq(value).build();

        delete(query);
    }

    @Override
    public <T> List<T> query(String query) {
        requireNonNull(query, "query is required");
        return PARSER.query(query, getManager(), getObserver()).stream().map(c -> (T) getConverter().toEntity(c))
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
        return new DocumentPreparedStatement(PARSER.prepare(query, getManager(), getObserver()), getConverter());
    }


    @Override
    public long count(String documentCollection) {
        return getManager().count(documentCollection);
    }

    public <T> long count(Class<T> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is required");
        ClassRepresentation classRepresentation = getClassRepresentations().get(entityClass);
        return getManager().count(classRepresentation.getName());
    }

}
