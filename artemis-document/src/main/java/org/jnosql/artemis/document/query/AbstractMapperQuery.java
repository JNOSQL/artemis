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
package org.jnosql.artemis.document.query;

import org.jnosql.artemis.Converters;
import org.jnosql.artemis.document.util.ConverterUtil;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCondition;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

abstract class AbstractMapperQuery {


    protected final String documentCollection;

    protected boolean negate;

    protected DocumentCondition condition;

    protected boolean and;

    protected String name;

    protected final ClassRepresentation representation;

    protected final Converters converters;

    protected long start;

    protected long limit;


    AbstractMapperQuery(ClassRepresentation representation, Converters converters) {
        this.representation = representation;
        this.converters = converters;
        this.documentCollection = representation.getName();
    }

    protected void appendCondition(DocumentCondition newCondition) {
        if (negate) {
            newCondition = newCondition.negate();
        }
        if (nonNull(condition)) {
            if (and) {
                this.condition = condition.and(newCondition);
            } else {
                this.condition = condition.or(newCondition);
            }
        } else {
            this.condition = newCondition;
        }
        this.negate = false;
        this.name = null;
    }

    protected <T> void betweenImpl(T valueA, T valueB) {
        requireNonNull(valueA, "valueA is required");
        requireNonNull(valueB, "valueB is required");
        DocumentCondition newCondition = DocumentCondition
                .between(Document.of(representation.getColumnField(name), asList(getValue(valueA), getValue(valueB))));
        appendCondition(newCondition);
    }


    protected <T> void inImpl(Iterable<T> values) {

        requireNonNull(values, "values is required");
        List<Object> convertedValues = StreamSupport.stream(values.spliterator(), false)
                .map(this::getValue).collect(toList());
        DocumentCondition newCondition = DocumentCondition
                .in(Document.of(representation.getColumnField(name), convertedValues));
        appendCondition(newCondition);
    }

    protected <T> void eqImpl(T value) {
        requireNonNull(value, "value is required");

        DocumentCondition newCondition = DocumentCondition
                .eq(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected void likeImpl(String value) {
        requireNonNull(value, "value is required");
        DocumentCondition newCondition = DocumentCondition
                .like(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void gteImpl(T value) {
        requireNonNull(value, "value is required");
        DocumentCondition newCondition = DocumentCondition
                .gte(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void gtImpl(T value) {
        requireNonNull(value, "value is required");
        DocumentCondition newCondition = DocumentCondition
                .gt(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void ltImpl(T value) {
        requireNonNull(value, "value is required");
        DocumentCondition newCondition = DocumentCondition
                .lt(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }

    protected <T> void lteImpl(T value) {
        requireNonNull(value, "value is required");
        DocumentCondition newCondition = DocumentCondition
                .lte(Document.of(representation.getColumnField(name), getValue(value)));
        appendCondition(newCondition);
    }


    protected Object getValue(Object value) {
        return ConverterUtil.getValue(value, representation, name, converters);
    }
}
