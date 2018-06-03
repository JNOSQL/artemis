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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jnosql.artemis.EntityNotFoundException;
import org.jnosql.artemis.IdNotFoundException;
import org.jnosql.artemis.PreparedStatement;
import org.jnosql.artemis.graph.model.Animal;
import org.jnosql.artemis.graph.model.Book;
import org.jnosql.artemis.graph.model.Person;
import org.jnosql.artemis.graph.model.WrongEntity;
import org.jnosql.diana.api.NonUniqueResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jnosql.artemis.graph.model.Person.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractGraphTemplateTest {

    protected abstract Graph getGraph();

    protected abstract GraphTemplate getGraphTemplate();

    @AfterEach
    public void after() {
        getGraph().traversal().V().toList().forEach(Vertex::remove);
        getGraph().traversal().E().toList().forEach(Edge::remove);
    }


    @Test
    public void shouldReturnErrorWhenThereIsNotId() {
        assertThrows(IdNotFoundException.class, () -> {
            WrongEntity entity = new WrongEntity("lion");
            getGraphTemplate().insert(entity);
        });
    }

    @Test
    public void shouldReturnErrorWhenEntityIsNull() {
        assertThrows(NullPointerException.class, () -> getGraphTemplate().insert(null));
    }


    @Test
    public void shouldInsertAnEntity() {
        Person person = builder().withAge()
                .withName("Otavio").build();
        Person updated = getGraphTemplate().insert(person);

        assertNotNull(updated.getId());

        getGraphTemplate().delete(updated.getId());
    }


    @Test
    public void shouldGetErrorWhenIdIsNullWhenUpdate() {
        assertThrows(NullPointerException.class, () -> {
            Person person = builder().withAge()
                    .withName("Otavio").build();
            getGraphTemplate().update(person);
        });
    }

    @Test
    public void shouldGetErrorWhenEntityIsNotSavedYet() {
        assertThrows(EntityNotFoundException.class, () -> {
            Person person = builder().withAge()
                    .withId(10L)
                    .withName("Otavio").build();

            getGraphTemplate().update(person);
        });
    }

    @Test
    public void shouldUpdate() {
        Person person = builder().withAge()
                .withName("Otavio").build();
        Person updated = getGraphTemplate().insert(person);
        Person newPerson = builder()
                .withAge()
                .withId(updated.getId())
                .withName("Otavio Updated").build();

        Person update = getGraphTemplate().update(newPerson);

        assertEquals(newPerson, update);

        getGraphTemplate().delete(update.getId());
    }


    @Test
    public void shouldReturnErrorInFindWhenIdIsNull() {
        assertThrows(NullPointerException.class, () -> getGraphTemplate().find(null));
    }

    @Test
    public void shouldFindAnEntity() {
        Person person = builder().withAge()
                .withName("Otavio").build();
        Person updated = getGraphTemplate().insert(person);
        Optional<Person> personFound = getGraphTemplate().find(updated.getId());

        assertTrue(personFound.isPresent());
        assertEquals(updated, personFound.get());

        getGraphTemplate().delete(updated.getId());
    }

    @Test
    public void shouldNotFindAnEntity() {
        Optional<Person> personFound = getGraphTemplate().find(0L);
        assertFalse(personFound.isPresent());
    }

    @Test
    public void shouldDeleteAnEntity() {

        Person person = getGraphTemplate().insert(builder().withAge()
                .withName("Otavio").build());

        assertTrue(getGraphTemplate().find(person.getId()).isPresent());
        getGraphTemplate().delete(person.getId());
        assertFalse(getGraphTemplate().find(person.getId()).isPresent());
    }

    @Test
    public void shouldReturnErrorWhenGetEdgesIdHasNullId() {
        assertThrows(NullPointerException.class, () -> getGraphTemplate().getEdgesById(null, Direction.BOTH));
    }

    @Test
    public void shouldReturnErrorWhenGetEdgesIdHasNullDirection() {
        assertThrows(NullPointerException.class, () -> getGraphTemplate().getEdgesById(10, null));
    }

    @Test
    public void shouldReturnEmptyWhenVertexDoesNotExist() {
        Collection<EdgeEntity> edges = getGraphTemplate().getEdgesById(10, Direction.BOTH);
        assertTrue(edges.isEmpty());
    }

    @Test
    public void shouldReturnEdgesById() {
        Person otavio = getGraphTemplate().insert(builder().withAge()
                .withName("Otavio").build());

        Animal dog = getGraphTemplate().insert(new Animal("dog"));
        Book cleanCode = getGraphTemplate().insert(Book.builder().withName("Clean code").build());

        EdgeEntity likes = getGraphTemplate().edge(otavio, "likes", dog);
        EdgeEntity reads = getGraphTemplate().edge(otavio, "reads", cleanCode);

        Collection<EdgeEntity> edgesById = getGraphTemplate().getEdgesById(otavio.getId(), Direction.BOTH);
        Collection<EdgeEntity> edgesById1 = getGraphTemplate().getEdgesById(otavio.getId(), Direction.BOTH, "reads");
        Collection<EdgeEntity> edgesById2 = getGraphTemplate().getEdgesById(otavio.getId(), Direction.BOTH, () -> "likes");
        Collection<EdgeEntity> edgesById3 = getGraphTemplate().getEdgesById(otavio.getId(), Direction.OUT);
        Collection<EdgeEntity> edgesById4 = getGraphTemplate().getEdgesById(cleanCode.getId(), Direction.IN);

        assertEquals(edgesById, edgesById3);
        assertThat(edgesById, containsInAnyOrder(likes, reads));
        assertThat(edgesById1, containsInAnyOrder(reads));
        assertThat(edgesById2, containsInAnyOrder(likes));
        assertThat(edgesById4, containsInAnyOrder(reads));

    }

    @Test
    public void shouldReturnErrorWhenGetEdgesHasNullId() {
        assertThrows(NullPointerException.class, () -> getGraphTemplate().getEdges(null, Direction.BOTH));
    }

    @Test
    public void shouldReturnErrorWhenGetEdgesHasNullId2() {
        assertThrows(NullPointerException.class, () -> {
            Person otavio = builder().withAge().withName("Otavio").build();
            getGraphTemplate().getEdges(otavio, Direction.BOTH);
        });
    }

    @Test
    public void shouldReturnErrorWhenGetEdgesHasNullDirection() {
        assertThrows(NullPointerException.class, () -> {
            Person otavio = getGraphTemplate().insert(builder().withAge()
                    .withName("Otavio").build());
            getGraphTemplate().getEdges(otavio, null);
        });
    }

    @Test
    public void shouldReturnEmptyWhenEntityDoesNotExist() {
        Person otavio = builder().withAge().withName("Otavio").withId(10L).build();
        Collection<EdgeEntity> edges = getGraphTemplate().getEdges(otavio, Direction.BOTH);
        assertTrue(edges.isEmpty());
    }


    @Test
    public void shouldReturnEdges() {
        Person otavio = getGraphTemplate().insert(builder().withAge()
                .withName("Otavio").build());

        Animal dog = getGraphTemplate().insert(new Animal("dog"));
        Book cleanCode = getGraphTemplate().insert(Book.builder().withName("Clean code").build());

        EdgeEntity likes = getGraphTemplate().edge(otavio, "likes", dog);
        EdgeEntity reads = getGraphTemplate().edge(otavio, "reads", cleanCode);

        Collection<EdgeEntity> edgesById = getGraphTemplate().getEdges(otavio, Direction.BOTH);
        Collection<EdgeEntity> edgesById1 = getGraphTemplate().getEdges(otavio, Direction.BOTH, "reads");
        Collection<EdgeEntity> edgesById2 = getGraphTemplate().getEdges(otavio, Direction.BOTH, () -> "likes");
        Collection<EdgeEntity> edgesById3 = getGraphTemplate().getEdges(otavio, Direction.OUT);
        Collection<EdgeEntity> edgesById4 = getGraphTemplate().getEdges(cleanCode, Direction.IN);

        assertEquals(edgesById, edgesById3);
        assertThat(edgesById, containsInAnyOrder(likes, reads));
        assertThat(edgesById1, containsInAnyOrder(reads));
        assertThat(edgesById2, containsInAnyOrder(likes));
        assertThat(edgesById4, containsInAnyOrder(reads));

    }

    @Test
    public void shouldGetTransaction() {
        Transaction transaction = getGraphTemplate().getTransaction();
        assertNotNull(transaction);
    }

    @Test
    public void shouldExecuteQuery() {
        Person person = builder().withAge()
                .withName("Otavio").build();
        getGraphTemplate().insert(person);
        List<Person> people = getGraphTemplate().query("g.V().hasLabel('Person')");
        MatcherAssert.assertThat(people.stream().map(Person::getName).collect(toList()), Matchers.contains("Otavio"));
    }

    @Test
    public void shouldReturnEmpty() {
        Optional<Person> person = getGraphTemplate().singleResult("g.V().hasLabel('Person')");
        assertFalse(person.isPresent());
    }

    @Test
    public void shouldReturnOneElement() {
        Person otavio = builder().withAge()
                .withName("Otavio").build();
        getGraphTemplate().insert(otavio);
        Optional<Person> person = getGraphTemplate().singleResult("g.V().hasLabel('Person')");
        assertTrue(person.isPresent());
    }

    @Test
    public void shouldReturnErrorWhenHasMoneThanOneElement() {

        getGraphTemplate().insert(builder().withAge().withName("Otavio").build());
        getGraphTemplate().insert(builder().withAge().withName("Poliana").build());
        assertThrows(NonUniqueResultException.class, () -> getGraphTemplate().singleResult("g.V().hasLabel('Person')"));
    }

    @Test
    public void shouldExecutePrepareStatement() {
        getGraphTemplate().insert(builder().withAge().withName("Otavio").build());
        PreparedStatement prepare = getGraphTemplate().prepare("g.V().hasLabel(param)");
        prepare.bind("param", "Person");
        List<Person> people = prepare.getResultList();
        MatcherAssert.assertThat(people.stream().map(Person::getName).collect(toList()), Matchers.contains("Otavio"));
    }

    @Test
    public void shouldExecutePrepareStatementSingleton() {
        getGraphTemplate().insert(builder().withAge().withName("Otavio").build());
        PreparedStatement prepare = getGraphTemplate().prepare("g.V().hasLabel(param)");
        prepare.bind("param", "Person");
        Optional<Person> otavio = prepare.getSingleResult();
        assertTrue(otavio.isPresent());
    }

    @Test
    public void shouldExecutePrepareStatementSingletonEmpty() {
        PreparedStatement prepare = getGraphTemplate().prepare("g.V().hasLabel(param)");
        prepare.bind("param", "Person");
        Optional<Person> otavio = prepare.getSingleResult();
        assertFalse(otavio.isPresent());
    }

    @Test
    public void shouldExecutePrepareStatementWithErrorWhenThereIsMoreThanOneResult() {
        getGraphTemplate().insert(builder().withAge().withName("Otavio").build());
        getGraphTemplate().insert(builder().withAge().withName("Poliana").build());
        PreparedStatement prepare = getGraphTemplate().prepare("g.V().hasLabel(param)");
        prepare.bind("param", "Person");
        assertThrows(NonUniqueResultException.class, prepare::getSingleResult);
    }
}
