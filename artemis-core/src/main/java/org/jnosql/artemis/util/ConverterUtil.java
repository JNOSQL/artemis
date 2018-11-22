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
package org.jnosql.artemis.util;

import org.jnosql.artemis.AttributeConverter;
import org.jnosql.artemis.Converters;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.FieldMapping;
import org.jnosql.diana.api.Value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConverterUtil {


    private ConverterUtil() {

    }

    /**
     * Converts the value to database format
     *
     * @param value          the value
     * @param representation the class representation
     * @param name           the java fieldName
     * @param converters     the collection of converter
     * @return the value converted
     */
    public static Object getValue(Object value, ClassRepresentation representation, String name, Converters converters) {
        Optional<FieldMapping> fieldOptional = representation.getFieldRepresentation(name);
        if (fieldOptional.isPresent()) {
            FieldMapping field = fieldOptional.get();
            return getValue(value, converters, field);
        }
        return value;
    }

    /**
     * Converts the value from the field with {@link FieldMapping} to database format
     *
     * @param value      the value to be converted
     * @param converters the converter
     * @param field      the field
     * @return tje value converted
     */
    public static Object getValue(Object value, Converters converters, FieldMapping field) {
        Field nativeField = field.getNativeField();
        if (!nativeField.getType().equals(value.getClass())) {
            return field.getConverter()
                    .map(converters::get)
                    .map(useConverter(value))
                    .orElseGet(() -> Value.of(value).get(nativeField.getType()));
        }

        return field.getConverter()
                .map(converters::get)
                .map(useConverter(value))
                .orElse(value);
    }

    private static Function<AttributeConverter, Object> useConverter(Object value) {
        return a -> {
            if (isNative(value).test(a)) {
                return value;
            }
            return a.convertToDatabaseColumn(value);
        };
    }

    private static Predicate<AttributeConverter> isNative(Object value) {
        return a -> getGenericInterface(a).getActualTypeArguments()[1].equals(value.getClass());
    }


    private static ParameterizedType getGenericInterface(AttributeConverter a) {
        for (Type genericInterface : a.getClass().getGenericInterfaces()) {
            if (ParameterizedType.class.isAssignableFrom(genericInterface.getClass()) &&
                    ParameterizedType.class.cast(genericInterface).getRawType().equals(AttributeConverter.class)) {
                return (ParameterizedType) genericInterface;
            }
        }
        throw new IllegalArgumentException("It does not found AttributeConverter implementation to this converter");
    }
}
