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

import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.IdNotFoundException;
import org.jnosql.artemis.model.User;
import org.jnosql.artemis.model.Worker;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.key.KeyValueEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(CDIExtension.class)
public class DefaultKeyValueEntityConverterTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Test
    public void shouldReturnNPEWhenEntityIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toKeyValue(null));
    }

    @Test
    public void shouldReturnErrorWhenThereIsNotKeyAnnotation() {
        Assertions.assertThrows(IdNotFoundException.class, () -> converter.toKeyValue(new Worker()));
    }

    @Test
    public void shouldReturnErrorWhenTheKeyIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            User user = new User(null, "name", 24);
            converter.toKeyValue(user);
        });
    }

    @Test
    public void shouldConvertToKeyValue() {
        User user = new User("nickname", "name", 24);
        KeyValueEntity<String> keyValueEntity = converter.toKeyValue(user);
        assertEquals("nickname", keyValueEntity.getKey());
        assertEquals(user, keyValueEntity.getValue().get());
    }

    @Test
    public void shouldReturnNPEWhenKeyValueIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toEntity(User.class, (KeyValueEntity<?>) null));
    }

    @Test
    public void shouldReturnNPEWhenClassIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> converter.toEntity(null, KeyValueEntity.of("user", new User("nickname", "name", 21))));
    }

    @Test
    public void shouldReturnErrorWhenTheKeyIsMissing() {
        Assertions.assertThrows(IdNotFoundException.class, () -> converter.toEntity(Worker.class, KeyValueEntity.of("worker", new Worker())));
    }

    @Test
    public void shouldConvertToEntity() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("user", expectedUser));
        assertEquals(expectedUser, user);
    }

    @Test
    public void shouldConvertAndFeedTheKeyValue() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("nickname", new User(null, "name", 21)));
        assertEquals(expectedUser, user);
    }

    @Test
    public void shouldConvertAndFeedTheKeyValueIfKeyAndFieldAreDifferent() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class,
                KeyValueEntity.of("nickname", new User("newName", "name", 21)));
        assertEquals(expectedUser, user);
    }

    @Test
    public void shouldConvertValueToEntity() {
        User expectedUser = new User("nickname", "name", 21);
        User user = converter.toEntity(User.class, Value.of(expectedUser));
        assertEquals(expectedUser, user);
    }


}