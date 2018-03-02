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

import org.hamcrest.Matchers;
import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.model.Actor;
import org.jnosql.artemis.model.Address;
import org.jnosql.artemis.model.AppointmentBook;
import org.jnosql.artemis.model.Contact;
import org.jnosql.artemis.model.ContactType;
import org.jnosql.artemis.model.Director;
import org.jnosql.artemis.model.Job;
import org.jnosql.artemis.model.Money;
import org.jnosql.artemis.model.Movie;
import org.jnosql.artemis.model.Person;
import org.jnosql.artemis.model.Worker;
import org.jnosql.artemis.model.Zipcode;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(CDIExtension.class)
public class DefaultColumnEntityConverterTest {


    @Inject
    private DefaultColumnEntityConverter converter;

    private Column[] columns;

    private Actor actor = Actor.actorBuilder().withAge()
            .withId()
            .withName()
            .withPhones(asList("234", "2342"))
            .withMovieCharacter(Collections.singletonMap("JavaZone", "Jedi"))
            .withMovierRating(Collections.singletonMap("JavaZone", 10))
            .build();

    @BeforeEach
    public void init() {

        columns = new Column[]{Column.of("_id", 12L),
                Column.of("age", 10), Column.of("name", "Otavio"),
                Column.of("phones", asList("234", "2342"))
                , Column.of("movieCharacter", Collections.singletonMap("JavaZone", "Jedi"))
                , Column.of("movieRating", Collections.singletonMap("JavaZone", 10))};
    }

    @Test
    public void shouldConvertEntityFromColumnEntity() {

        Person person = Person.builder().withAge()
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).build();

        ColumnEntity entity = converter.toColumn(person);
        assertEquals("Person", entity.getName());
        assertEquals(4, entity.size());
        assertThat(entity.getColumns(), containsInAnyOrder(Column.of("_id", 12L),
                Column.of("age", 10), Column.of("name", "Otavio"), Column.of("phones", Arrays.asList("234", "2342"))));

    }

    @Test
    public void shouldConvertColumnEntityFromEntity() {

        ColumnEntity entity = converter.toColumn(actor);
        assertEquals("Actor", entity.getName());
        assertEquals(6, entity.size());

        assertThat(entity.getColumns(), containsInAnyOrder(columns));
    }

    @Test
    public void shouldConvertColumnEntityToEntity() {
        ColumnEntity entity = ColumnEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(Actor.class, entity);
        assertNotNull(actor);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    public void shouldConvertColumnEntityToEntity2() {
        ColumnEntity entity = ColumnEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);

        Actor actor = converter.toEntity(entity);
        assertNotNull(actor);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    public void shouldConvertColumnEntityToExistEntity() {
        ColumnEntity entity = ColumnEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();
        Actor result = converter.toEntity(actor, entity);

        assertTrue(actor == result);
        assertEquals(10, actor.getAge());
        assertEquals(12L, actor.getId());
        assertEquals(asList("234", "2342"), actor.getPhones());
        assertEquals(Collections.singletonMap("JavaZone", "Jedi"), actor.getMovieCharacter());
        assertEquals(Collections.singletonMap("JavaZone", 10), actor.getMovieRating());
    }

    @Test
    public void shouldReturnErrorWhenToEntityIsNull() {
        ColumnEntity entity = ColumnEntity.of("Actor");
        Stream.of(columns).forEach(entity::add);
        Actor actor = Actor.actorBuilder().build();

        assertThrows(NullPointerException.class, () -> {
           converter.toEntity(null, entity);
        });

        assertThrows(NullPointerException.class, () -> {
            converter.toEntity(actor, null);
        });
    }


    @Test
    public void shouldConvertEntityToColumnEntity2() {

        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDiretor().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        ColumnEntity entity = converter.toColumn(director);
        assertEquals(5, entity.size());

        assertEquals(getValue(entity.find("name")), director.getName());
        assertEquals(getValue(entity.find("age")), director.getAge());
        assertEquals(getValue(entity.find("_id")), director.getId());
        assertEquals(getValue(entity.find("phones")), director.getPhones());


        Column subColumn = entity.find("movie").get();
        List<Column> columns = subColumn.get(new TypeReference<List<Column>>() {
        });

        assertEquals(3, columns.size());
        assertEquals("movie", subColumn.getName());
        assertEquals(movie.getTitle(), columns.stream().filter(c -> "title".equals(c.getName())).findFirst().get().get());
        assertEquals(movie.getYear(), columns.stream().filter(c -> "year".equals(c.getName())).findFirst().get().get());
        assertEquals(movie.getActors(), columns.stream().filter(c -> "actors".equals(c.getName())).findFirst().get().get());


    }

    @Test
    public void shouldConvertToEmbeddedClassWhenHasSubColumn() {
        Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
        Director director = Director.builderDiretor().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        ColumnEntity entity = converter.toColumn(director);
        Director director1 = converter.toEntity(entity);

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

    @Test
    public void shouldConvertToEmbeddedClassWhenHasSubColumn2() {
        Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
        Director director = Director.builderDiretor().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        ColumnEntity entity = converter.toColumn(director);
        entity.remove("movie");
        entity.add(Column.of("title", "Matrix"));
        entity.add(Column.of("year", 2012));
        entity.add(Column.of("actors", singleton("Actor")));
        Director director1 = converter.toEntity(entity);

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

    @Test
    public void shouldConvertToEmbeddedClassWhenHasSubColumn3() {
        Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
        Director director = Director.builderDiretor().withAge(12)
                .withId(12)
                .withName("Otavio")
                .withPhones(asList("234", "2342")).withMovie(movie).build();

        ColumnEntity entity = converter.toColumn(director);
        entity.remove("movie");
        Map<String, Object> map = new HashMap<>();
        map.put("title", "Matrix");
        map.put("year", 2012);
        map.put("actors", singleton("Actor"));

        entity.add(Column.of("movie", map));
        Director director1 = converter.toEntity(entity);

        assertEquals(movie, director1.getMovie());
        assertEquals(director.getName(), director1.getName());
        assertEquals(director.getAge(), director1.getAge());
        assertEquals(director.getId(), director1.getId());
    }

    @Test
    public void shouldConvertToColumnWhenHaConverter() {
        Worker worker = new Worker();
        Job job = new Job();
        job.setCity("Sao Paulo");
        job.setDescription("Java Developer");
        worker.setName("Bob");
        worker.setSalary(new Money("BRL", BigDecimal.TEN));
        worker.setJob(job);
        ColumnEntity entity = converter.toColumn(worker);
        assertEquals("Worker", entity.getName());
        assertEquals("Bob", entity.find("name").get().get());
        Column subColumn = entity.find("job").get();
        List<Column> columns = subColumn.get(new TypeReference<List<Column>>() {
        });
        assertThat(columns, Matchers.containsInAnyOrder(Column.of("city", "Sao Paulo"), Column.of("description", "Java Developer")));
        assertEquals("BRL 10", entity.find("money").get().get());
    }

    @Test
    public void shouldConvertToEntityWhenHasConverter() {
        Worker worker = new Worker();
        Job job = new Job();
        job.setCity("Sao Paulo");
        job.setDescription("Java Developer");
        worker.setName("Bob");
        worker.setSalary(new Money("BRL", BigDecimal.TEN));
        worker.setJob(job);
        ColumnEntity entity = converter.toColumn(worker);
        Worker worker1 = converter.toEntity(entity);
        assertEquals(worker.getSalary(), worker1.getSalary());
        assertEquals(job.getCity(), worker1.getJob().getCity());
        assertEquals(job.getDescription(), worker1.getJob().getDescription());
    }


    @Test
    public void shouldConvertoListEmbeddable() {
        AppointmentBook appointmentBook = new AppointmentBook("ids");
        appointmentBook.add(Contact.builder().withType(ContactType.EMAIL).withName("Ada").withInformation("ada@lovelace.com").build());
        appointmentBook.add(Contact.builder().withType(ContactType.MOBILE).withName("Ada").withInformation("11 1231231 123").build());
        appointmentBook.add(Contact.builder().withType(ContactType.PHONE).withName("Ada").withInformation("12 123 1231 123123").build());

        ColumnEntity entity = converter.toColumn(appointmentBook);
        Column contacts = entity.find("contacts").get();
        assertEquals("ids", appointmentBook.getId());
        List<List<Column>> columns = (List<List<Column>>) contacts.get();

        assertEquals(3L, columns.stream().flatMap(Collection::stream)
                .filter(c -> c.getName().equals("name"))
                .count());
    }

    @Test
    public void shouldConvertFromListEmbeddable() {
        ColumnEntity entity = ColumnEntity.of("AppointmentBook");
        entity.add(Column.of("_id", "ids"));
        List<List<Column>> columns = new ArrayList<>();

        columns.add(asList(Column.of("name", "Ada"), Column.of("type", ContactType.EMAIL),
                Column.of("information", "ada@lovelace.com")));

        columns.add(asList(Column.of("name", "Ada"), Column.of("type", ContactType.MOBILE),
                Column.of("information", "11 1231231 123")));

        columns.add(asList(Column.of("name", "Ada"), Column.of("type", ContactType.PHONE),
                Column.of("information", "phone")));

        entity.add(Column.of("contacts", columns));

        AppointmentBook appointmentBook = converter.toEntity(entity);

        List<Contact> contacts = appointmentBook.getContacts();
        assertEquals("ids", appointmentBook.getId());
        assertEquals("Ada", contacts.stream().map(Contact::getName).distinct().findFirst().get());

    }


    @Test
    public void shouldConvertSubEntity() {
        Zipcode zipcode = new Zipcode();
        zipcode.setZip("12321");
        zipcode.setPlusFour("1234");

        Address address = new Address();
        address.setCity("Salvador");
        address.setState("Bahia");
        address.setStreet("Rua Engenheiro Jose Anasoh");
        address.setZipcode(zipcode);

        ColumnEntity columnEntity = converter.toColumn(address);
        List<Column> columns = columnEntity.getColumns();
        assertEquals("Address", columnEntity.getName());
        assertEquals(5, columns.size());

        assertEquals("Rua Engenheiro Jose Anasoh", getValue(columnEntity.find("street")));
        assertEquals("Salvador", getValue(columnEntity.find("city")));
        assertEquals("Bahia", getValue(columnEntity.find("state")));
        assertEquals("12321", getValue(columnEntity.find("zip")));
        assertEquals("1234", getValue(columnEntity.find("plusFour")));
    }

    @Test
    public void shouldConvertColumnInSubEntity() {

        ColumnEntity entity = ColumnEntity.of("Address");

        entity.add(Column.of("street", "Rua Engenheiro Jose Anasoh"));
        entity.add(Column.of("city", "Salvador"));
        entity.add(Column.of("state", "Bahia"));
        entity.add(Column.of("zip", "12321"));
        entity.add(Column.of("plusFour", "1234"));

        Address address = converter.toEntity(entity);

        assertEquals("Rua Engenheiro Jose Anasoh", address.getStreet());
        assertEquals("Salvador", address.getCity());
        assertEquals("Bahia", address.getState());
        assertEquals("12321", address.getZipcode().getZip());
        assertEquals("1234",  address.getZipcode().getPlusFour());

    }
    private Object getValue(Optional<Column> column) {
        return column.map(Column::getValue).map(Value::get).orElse(null);
    }

}