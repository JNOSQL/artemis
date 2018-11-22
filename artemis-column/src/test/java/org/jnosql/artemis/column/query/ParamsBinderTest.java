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
package org.jnosql.artemis.column.query;

import org.jnosql.aphrodite.antlr.method.SelectMethodFactory;
import org.jnosql.artemis.CDIExtension;
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.model.Person;
import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.artemis.util.ParamsBinder;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.column.Column;
import org.jnosql.diana.api.column.ColumnCondition;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.column.query.ColumnQueryParams;
import org.jnosql.diana.api.column.query.SelectQueryConverter;
import org.jnosql.query.Params;
import org.jnosql.query.SelectQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(CDIExtension.class)
class ParamsBinderTest {


    @Inject
    private ClassRepresentations representations;

    @Inject
    private Converters converters;

    private ParamsBinder paramsBinder;

    @Test
    public void shouldConvert() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAge")).findFirst().get();
        ClassMapping classMapping = representations.get(Person.class);
        RepositoryColumnObserverParser parser = new RepositoryColumnObserverParser(classMapping);
        paramsBinder = new ParamsBinder(classMapping, converters);

        SelectMethodFactory selectMethodFactory = SelectMethodFactory.get();
        SelectQuery selectQuery = selectMethodFactory.apply(method, classMapping.getName());
        SelectQueryConverter converter = SelectQueryConverter.get();
        ColumnQueryParams columnQueryParams = converter.apply(selectQuery, parser);
        Params params = columnQueryParams.getParams();
        Object[] args = {10};
        paramsBinder.bind(params, args, method);
        ColumnQuery query = columnQueryParams.getQuery();
        ColumnCondition columnCondition = query.getCondition().get();
        Value value = columnCondition.getColumn().getValue();
        assertEquals(10, value.get());

    }

    @Test
    public void shouldConvert2() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAgeAndName")).findFirst().get();
        ClassMapping classMapping = representations.get(Person.class);
        RepositoryColumnObserverParser parser = new RepositoryColumnObserverParser(classMapping);
        paramsBinder = new ParamsBinder(classMapping, converters);

        SelectMethodFactory selectMethodFactory = SelectMethodFactory.get();
        SelectQuery selectQuery = selectMethodFactory.apply(method, classMapping.getName());
        SelectQueryConverter converter = SelectQueryConverter.get();
        ColumnQueryParams queryParams = converter.apply(selectQuery, parser);
        Params params = queryParams.getParams();
        paramsBinder.bind(params, new Object[]{10L, "Ada"}, method);
        ColumnQuery query = queryParams.getQuery();
        ColumnCondition columnCondition = query.getCondition().get();
        List<ColumnCondition> conditions = columnCondition.getColumn().get(new TypeReference<List<ColumnCondition>>() {
        });
        List<Object> values = conditions.stream().map(ColumnCondition::getColumn)
                .map(Column::getValue)
                .map(Value::get).collect(Collectors.toList());
        assertEquals(10, values.get(0));
        assertEquals("Ada", values.get(1));

    }


    interface PersonRepository {

        List<Person> findByAge(Integer age);

        List<Person> findByAgeAndName(Long age, String name);
    }


}