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
package org.jnosql.artemis.reflection;

import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.model.Actor;
import org.jnosql.artemis.model.Animal;
import org.jnosql.artemis.model.Director;
import org.jnosql.artemis.model.Machine;
import org.jnosql.artemis.model.Person;
import org.jnosql.artemis.model.User;
import org.jnosql.artemis.model.Worker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.jnosql.artemis.reflection.FieldType.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(CDIExtension.class)
public class ClassConverterTest {

    @Inject
    private ClassConverter classConverter;


    @Test
    public void shouldCreateClassMapping() {
        ClassMapping classMapping = classConverter.create(Person.class);

        assertEquals("Person", classMapping.getName());
        assertEquals(Person.class, classMapping.getClassInstance());
        assertEquals(4, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones"));

    }

    @Test
    public void shouldCreateClassMapping2() {
        ClassMapping classMapping = classConverter.create(Actor.class);

        assertEquals("Actor", classMapping.getName());
        assertEquals(Actor.class, classMapping.getClassInstance());
        assertEquals(6, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones", "movieCharacter", "movieRating"));

    }

    @Test
    public void shouldCreateClassMappingWithEmbeddedClass() {
        ClassMapping classMapping = classConverter.create(Director.class);
        assertEquals("Director", classMapping.getName());
        assertEquals(Director.class, classMapping.getClassInstance());
        assertEquals(5, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones", "movie"));

    }

    @Test
    public void shouldReturnFalseWhenThereIsNotKey() {
        ClassMapping classMapping = classConverter.create(Worker.class);
        boolean allMatch = classMapping.getFields().stream().noneMatch(FieldMapping::isId);
        assertTrue(allMatch);
    }


    @Test
    public void shouldReturnTrueWhenThereIsKey() {
        ClassMapping classMapping = classConverter.create(User.class);
        List<FieldMapping> fields = classMapping.getFields();

        Predicate<FieldMapping> hasKeyAnnotation = FieldMapping::isId;
        assertTrue(fields.stream().anyMatch(hasKeyAnnotation));
        FieldMapping fieldMapping = fields.stream().filter(hasKeyAnnotation).findFirst().get();
        assertEquals("_id", fieldMapping.getName());
        assertEquals(DEFAULT, fieldMapping.getType());

    }

    @Test
    public void shouldReturnErrorWhenThereIsNotConstructor() {
        Assertions.assertThrows(ConstructorException.class, () -> classConverter.create(Animal.class));
    }

    @Test
    public void shouldReturnWhenIsDefaultConstructor() {
        ClassMapping classMapping = classConverter.create(Machine.class);
        List<FieldMapping> fields = classMapping.getFields();
        assertEquals(1, fields.size());
    }

}