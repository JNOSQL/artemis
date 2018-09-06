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

import org.jnosql.aphrodite.antlr.method.DeleteMethodFactory;
import org.jnosql.aphrodite.antlr.method.SelectMethodFactory;
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.Param;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.diana.api.column.ColumnDeleteQuery;
import org.jnosql.diana.api.column.ColumnObserverParser;
import org.jnosql.diana.api.column.ColumnQuery;
import org.jnosql.diana.api.column.query.ColumnDeleteQueryParams;
import org.jnosql.diana.api.column.query.ColumnQueryParams;
import org.jnosql.diana.api.column.query.DeleteQueryConverter;
import org.jnosql.diana.api.column.query.SelectQueryConverter;
import org.jnosql.query.DeleteQuery;
import org.jnosql.query.Params;
import org.jnosql.query.SelectQuery;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

abstract class BaseColumnRepository {

    protected abstract Converters getConverters();

    protected abstract ClassRepresentation getClassRepresentation();

    private ColumnObserverParser columnObserverParser;

    private ParamsBinder paramsBinder;


    protected ColumnQuery getQuery(Method method, Object[] args) {
        SelectMethodFactory selectMethodFactory = SelectMethodFactory.get();
        SelectQuery selectQuery = selectMethodFactory.apply(method, getClassRepresentation().getName());
        SelectQueryConverter converter = SelectQueryConverter.get();
        ColumnQueryParams queryParams = converter.apply(selectQuery, getParser());
        ColumnQuery query = queryParams.getQuery();
        Params params = queryParams.getParams();
        ParamsBinder paramsBinder = getParamsBinder();
        paramsBinder.bind(params, args, method);
        return query;
    }

    protected ColumnDeleteQuery getDeleteQuery(Method method, Object[] args) {
        DeleteMethodFactory deleteMethodFactory = DeleteMethodFactory.get();
        DeleteQuery deleteQuery = deleteMethodFactory.apply(method, getClassRepresentation().getName());
        DeleteQueryConverter converter = DeleteQueryConverter.get();
        ColumnDeleteQueryParams queryParams = converter.apply(deleteQuery, getParser());
        ColumnDeleteQuery query = queryParams.getQuery();
        Params params = queryParams.getParams();
        ParamsBinder paramsBinder = getParamsBinder();
        paramsBinder.bind(params, args, method);
        return query;
    }


    protected ColumnObserverParser getParser() {
        if (columnObserverParser == null) {
            this.columnObserverParser = new RepositoryColumnObserverParser(getClassRepresentation());
        }
        return columnObserverParser;
    }

    protected ParamsBinder getParamsBinder() {
        if (Objects.isNull(paramsBinder)) {
            this.paramsBinder = new ParamsBinder(getClassRepresentation(), getConverters());
        }
        return paramsBinder;
    }

    protected Map<String, Object> getParams(Method method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Param param = parameter.getAnnotation(Param.class);
            if (Objects.nonNull(param)) {
                params.put(param.value(), args[index]);
            }
        }
        return params;
    }
}
