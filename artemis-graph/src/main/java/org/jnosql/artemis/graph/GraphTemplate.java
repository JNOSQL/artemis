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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.jnosql.artemis.PreparedStatement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This interface that represents the common operation between an entity
 * and {@link org.apache.tinkerpop.gremlin.structure.Vertex}
 */
public interface GraphTemplate {

    /**
     * Inserts entity
     *
     * @param entity entity to be saved
     * @param <T>    the instance type
     * @return the entity saved
     * @throws NullPointerException                   when document is null
     * @throws org.jnosql.artemis.IdNotFoundException when entity has not {@link org.jnosql.artemis.Id}
     */
    <T> T insert(T entity);

    /**
     * Updates entity
     *
     * @param entity entity to be updated
     * @param <T>    the instance type
     * @return the entity saved
     * @throws NullPointerException                   when document is null
     * @throws org.jnosql.artemis.IdNotFoundException when an entity is null
     */
    <T> T update(T entity);


    /**
     * Deletes a {@link org.apache.tinkerpop.gremlin.structure.Vertex}
     *
     * @param id  the id to be used in the query {@link org.apache.tinkerpop.gremlin.structure.T#id}
     * @param <T> the id type
     * @throws NullPointerException when id is null
     */
    <T> void delete(T id);

    /**
     * Deletes a {@link org.apache.tinkerpop.gremlin.structure.Edge}
     *
     * @param id  the id to be used in the query {@link org.apache.tinkerpop.gremlin.structure.T#id}
     * @param <T> the id type
     * @throws NullPointerException when either label and id are null
     */
    <T> void deleteEdge(T id);


    /**
     * Find an entity given {@link org.apache.tinkerpop.gremlin.structure.T#label} and
     * {@link org.apache.tinkerpop.gremlin.structure.T#id}
     *
     * @param id   the id to be used in the query {@link org.apache.tinkerpop.gremlin.structure.T#id}
     * @param <T>  the entity type
     * @param <ID> the id type
     * @return the entity found otherwise {@link Optional#empty()}
     * @throws NullPointerException when id is null
     */
    <T, ID> Optional<T> find(ID id);

    /**
     * Either find or create an Edge between this two entities.
     * {@link org.apache.tinkerpop.gremlin.structure.Edge}
     * <pre>entityOUT ---label---&#62; entityIN.</pre>
     *
     * @param incoming the incoming entity
     * @param label    the Edge label
     * @param outgoing the outgoing entity
     * @param <IN>     the incoming type
     * @param <OUT>    the outgoing type
     * @return the {@link EdgeEntity} of these two entities
     * @throws NullPointerException                       Either when any elements are null or the entity is null
     * @throws org.jnosql.artemis.IdNotFoundException     when {@link org.jnosql.artemis.Id} annotation is missing in the entities
     * @throws org.jnosql.artemis.EntityNotFoundException when neither outgoing or incoming is found
     */
    <OUT, IN> EdgeEntity edge(OUT outgoing, String label, IN incoming);

    /**
     * Either find or create an Edge between this two entities.
     * {@link org.apache.tinkerpop.gremlin.structure.Edge}
     * <pre>entityOUT ---label---&#62; entityIN.</pre>
     *
     * @param incoming the incoming entity
     * @param label    the Edge label
     * @param outgoing the outgoing entity
     * @param <IN>     the incoming type
     * @param <OUT>    the outgoing type
     * @return the {@link EdgeEntity} of these two entities
     * @throws NullPointerException                       Either when any elements are null or the entity is null
     * @throws org.jnosql.artemis.IdNotFoundException     when {@link org.jnosql.artemis.Id} annotation is missing in the entities
     * @throws org.jnosql.artemis.EntityNotFoundException when neither outgoing or incoming is found
     */
    default <OUT, IN> EdgeEntity edge(OUT outgoing, Supplier<String> label, IN incoming) {
        Objects.requireNonNull(label, "supplier is required");
        return edge(outgoing, label.get(), incoming);
    }


    /**
     * returns the edges of from a vertex id
     *
     * @param id        the id
     * @param direction the direction
     * @param labels    the edge labels
     * @param <ID>      the ID type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction, String... labels);

    /**
     * returns the edges of from a vertex id
     *
     * @param id        the id
     * @param direction the direction
     * @param labels    the edge labels
     * @param <ID>      the ID type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction, Supplier<String>... labels);

    /**
     * returns the edges of from a vertex id
     *
     * @param id        the id
     * @param direction the direction
     * @param <ID>      the ID type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <ID> Collection<EdgeEntity> getEdgesById(ID id, Direction direction);


    /**
     * returns the edges of from an entity
     *
     * @param entity    the entity
     * @param direction the direction
     * @param labels    the edge labels
     * @param <T>       the entity type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <T> Collection<EdgeEntity> getEdges(T entity, Direction direction, String... labels);

    /**
     * returns the edges of from an entity
     *
     * @param entity    the entity
     * @param direction the direction
     * @param labels    the edge labels
     * @param <T>       the entity type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <T> Collection<EdgeEntity> getEdges(T entity, Direction direction, Supplier<String>... labels);

    /**
     * returns the edges of from an entity
     *
     * @param entity    the entity
     * @param direction the direction
     * @param <T>       the entity type
     * @return the Edges
     * @throws NullPointerException where there is any parameter null
     */
    <T> Collection<EdgeEntity> getEdges(T entity, Direction direction);


    /**
     * Finds an {@link EdgeEntity} from the Edge Id
     *
     * @param edgeId the edge id
     * @param <E>    the edge id type
     * @return the {@link EdgeEntity} otherwise {@link Optional#empty()}
     * @throws NullPointerException when edgeId is null
     */
    <E> Optional<EdgeEntity> edge(E edgeId);


    /**
     * Gets a {@link VertexTraversal} to run a query in the graph
     *
     * @param vertexIds get ids
     * @return a {@link VertexTraversal} instance
     * @throws NullPointerException if any id element is null
     */
    VertexTraversal getTraversalVertex(Object... vertexIds);


    /**
     * Gets a {@link EdgeTraversal} to run a query in the graph
     *
     * @param edgeIds get ids
     * @return a {@link VertexTraversal} instance
     * @throws NullPointerException if any id element is null
     */
    EdgeTraversal getTraversalEdge(Object... edgeIds);


    /**
     * Gets the current transaction
     *
     * @return the current {@link Transaction}
     */
    Transaction getTransaction();


    /**
     * Executes a Gremlin gremlin then bring the result as a {@link List}
     *
     * @param gremlin the query gremlin
     * @param <T>     the entity type
     * @return the result as {@link List}
     * @throws NullPointerException when the gremlin is null
     */
    <T> List<T> query(String gremlin);

    /**
     * Executes a Gremlin query then bring the result as a unique result
     *
     * @param gremlin the gremlin query
     * @param <T>     the entity type
     * @return the result as {@link List}
     * @throws NullPointerException                          when the query is null
     * @throws org.jnosql.diana.api.NonUniqueResultException if returns more than one result
     */
    <T> Optional<T> singleResult(String gremlin);

    /**
     * Creates a {@link PreparedStatement} from the query
     *
     * @param gremlin the gremlin query
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException when the query is null
     */
    PreparedStatement prepare(String gremlin);

    /**
     * Returns the number of vertices from label
     *
     * @param label the label
     * @return the number of elements
     * @throws NullPointerException          when label is null
     * @throws UnsupportedOperationException when the database dot not have support
     */
    long count(String label);

    /**
     * Returns the number of vertices from label
     *
     * @param <T>         the entity type
     * @param entityClass the label
     * @return the number of elements
     * @throws NullPointerException          when label is null
     * @throws UnsupportedOperationException when the database dot not have support
     */
    <T> long count(Class<T> entityClass);
}
