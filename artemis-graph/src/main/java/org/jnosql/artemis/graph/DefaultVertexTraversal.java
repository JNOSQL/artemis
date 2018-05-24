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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.Entity;
import org.jnosql.diana.api.NonUniqueResultException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation of {@link VertexTraversal}
 */
class DefaultVertexTraversal extends AbstractVertexTraversal implements VertexTraversal {


    private static final Predicate<String> IS_EMPTY = String::isEmpty;
    private static final Predicate<String> NOT_EMPTY = IS_EMPTY.negate();

    DefaultVertexTraversal(Supplier<GraphTraversal<?, ?>> supplier,
                           Function<GraphTraversal<?, ?>, GraphTraversal<Vertex, Vertex>> flow,
                           GraphConverter converter) {
        super(supplier, flow, converter);
    }


    @Override
    public VertexTraversal has(String propertyKey, Object value) {
        requireNonNull(propertyKey, "propertyKey is required");
        requireNonNull(value, "value is required");

        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.has(propertyKey, value)), converter);
    }

    @Override
    public VertexTraversal has(String propertyKey) {
        requireNonNull(propertyKey, "propertyKey is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.has(propertyKey)), converter);
    }

    @Override
    public VertexTraversal has(String propertyKey, P<?> predicate) {
        requireNonNull(propertyKey, "propertyKey is required");
        requireNonNull(predicate, "predicate is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.has(propertyKey, predicate)), converter);
    }

    @Override
    public VertexTraversal has(T accessor, Object value) {
        requireNonNull(accessor, "accessor is required");
        requireNonNull(value, "value is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.has(accessor, value)), converter);
    }

    @Override
    public VertexTraversal has(T accessor, P<?> predicate) {
        requireNonNull(accessor, "accessor is required");
        requireNonNull(predicate, "predicate is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.has(accessor, predicate)), converter);
    }

    @Override
    public VertexTraversal out(String... labels) {
        if (Stream.of(labels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one label element cannot be null");
        }
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.out(labels)), converter);
    }

    @Override
    public <T> VertexTraversal filter(Predicate<T> predicate) {
        requireNonNull(predicate, "predicate is required");

        Predicate<Traverser<Vertex>> p = v -> predicate.test(converter.toEntity(v.get()));
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.filter(p)), converter);
    }

    @Override
    public EdgeTraversal outE(String... edgeLabels) {
        if (Stream.of(edgeLabels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one edgeLabels element cannot be null");
        }
        return new DefaultEdgeTraversal(supplier, flow.andThen(g -> g.outE(edgeLabels)), converter);
    }

    @Override
    public VertexTraversal in(String... labels) {
        if (Stream.of(labels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one label element cannot be null");
        }
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.in(labels)), converter);
    }

    @Override
    public EdgeTraversal inE(String... edgeLabels) {
        if (Stream.of(edgeLabels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one edgeLabels element cannot be null");
        }

        return new DefaultEdgeTraversal(supplier, flow.andThen(g -> g.inE(edgeLabels)), converter);
    }

    @Override
    public VertexTraversal both(String... labels) {
        if (Stream.of(labels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one label element cannot be null");
        }
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.both(labels)), converter);
    }

    @Override
    public EdgeTraversal bothE(String... edgeLabels) {
        if (Stream.of(edgeLabels).anyMatch(Objects::isNull)) {
            throw new NullPointerException("The no one edgeLabels element cannot be null");
        }
        return new DefaultEdgeTraversal(supplier, flow.andThen(g -> g.bothE(edgeLabels)), converter);
    }

    @Override
    public VertexRepeatTraversal repeat() {
        return new DefaultVertexRepeatTraversal(supplier, flow, converter);
    }

    @Override
    public VertexTraversal limit(long limit) {
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.limit(limit)), converter);
    }

    @Override
    public VertexTraversal range(long start, long end) {
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.range(start, end)), converter);
    }


    @Override
    public VertexTraversal hasLabel(String label) {
        Objects.requireNonNull(label, "label is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.hasLabel(label)), converter);
    }

    @Override
    public <T> VertexTraversal hasLabel(Class<T> entityClass) {
        requireNonNull(entityClass, "entityClass is required");
        Entity entity = entityClass.getAnnotation(Entity.class);
        String label = Optional.ofNullable(entity).map(Entity::value)
                .filter(NOT_EMPTY)
                .orElse(entityClass.getSimpleName());
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.hasLabel(label)), converter);
    }

    @Override
    public <T> VertexTraversal hasLabel(P<String> predicate) {
        requireNonNull(predicate, "predicate is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.hasLabel(predicate)), converter);
    }


    @Override
    public VertexTraversal hasNot(String propertyKey) {
        requireNonNull(propertyKey, "propertyKey is required");
        return new DefaultVertexTraversal(supplier, flow.andThen(g -> g.hasNot(propertyKey)), converter);
    }

    @Override
    public <T> Optional<T> next() {
        Optional<Vertex> vertex = flow.apply(supplier.get()).tryNext();
        return vertex.map(converter::toEntity);
    }

    @Override
    public <T> Stream<T> stream() {
        return flow.apply(supplier.get()).toList().stream()
                .map(converter::toEntity);
    }

    @Override
    public <T> Optional<T> getSingleResult() {
        List<T> result = getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        } else if (result.size() == 1) {
            return Optional.of(result.get(0));
        }
        throw new NonUniqueResultException("The Vertex traversal query returns more than one result");
    }

    @Override
    public <T> List<T> getResultList() {
        Stream<T> stream = stream();
        return stream.collect(Collectors.toList());
    }

    @Override
    public <T> Stream<T> next(int limit) {
        return flow.apply(supplier.get())
                .next(limit).stream()
                .map(converter::toEntity);
    }

    @Override
    public ValueMapTraversal valueMap(String... propertyKeys) {
        return new DefaultValueMapTraversal(supplier, flow.andThen(g -> g.valueMap(propertyKeys)));
    }


    @Override
    public long count() {
        return flow.apply(supplier.get()).count().tryNext().orElse(0L);
    }

    @Override
    public VertexTraversalOrder orderBy(String property) {
        requireNonNull(property, "property is required");
        return new DefaultVertexTraversalOrder(supplier, flow, converter, property);
    }
}
