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

import org.jnosql.artemis.MockitoExtension;
import org.jnosql.artemis.model.Person;
import org.jnosql.diana.api.column.ColumnEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DefaultColumnWorkflowTest {


    @InjectMocks
    private DefaultColumnWorkflow subject;

    @Mock
    private ColumnEventPersistManager columnEventPersistManager;

    @Mock
    private ColumnEntityConverter converter;

    @Mock
    private ColumnEntity columnEntity;

    @BeforeEach
    public void setUp() {
        when(converter.toColumn(any(Object.class)))
                .thenReturn(columnEntity);
        when(converter.toEntity(Mockito.eq(Person.class), any(ColumnEntity.class)))
                .thenReturn(Person.builder().build());
        when(converter.toEntity(Mockito.any(Person.class), any(ColumnEntity.class)))
                .thenReturn(Person.builder().build());

    }

    @Test
    public void shouldReturnErrorWhenEntityIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            UnaryOperator<ColumnEntity> action = t -> t;
            subject.flow(null, action);
        });
    }

    @Test
    public void shouldReturnErrorWhenActionIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> subject.flow("", null));
    }

    @Test
    public void shouldFollowWorkflow() {
        UnaryOperator<ColumnEntity> action = t -> t;
        subject.flow(Person.builder().withId(1L).withAge().withName("Ada").build(), action);

        verify(columnEventPersistManager).firePreColumn(any(ColumnEntity.class));
        verify(columnEventPersistManager).firePostColumn(any(ColumnEntity.class));
        verify(columnEventPersistManager).firePreEntity(any(Person.class));
        verify(columnEventPersistManager).firePostEntity(any(Person.class));

        verify(columnEventPersistManager).firePreColumnEntity(any(Person.class));
        verify(columnEventPersistManager).firePostColumnEntity(any(Person.class));
        verify(converter).toColumn(any(Object.class));
    }

}