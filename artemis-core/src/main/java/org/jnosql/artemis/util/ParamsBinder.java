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

import org.jnosql.artemis.Converters;
import org.jnosql.artemis.DynamicQueryException;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.artemis.reflection.FieldMapping;
import org.jnosql.query.Params;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A Binder class, that will apply values in the {@link Params} instance to a dynamic query.
 */
public class ParamsBinder {


    private final ClassRepresentation representation;

    private final Converters converters;


    /**
     * Creates a ParamsBinder instance
     * @param representation the representation of the used class
     * @param converters the converts
     * @throws NullPointerException when there is null parameter
     */
    public ParamsBinder(ClassRepresentation representation, Converters converters) {
        this.representation = Objects.requireNonNull(representation, "representation is required");
        this.converters = Objects.requireNonNull(converters, "converters is required");
    }

    /**
     * Fill up the Params with the args
     * @param params the params
     * @param args the args
     * @param method the method
     * @throws NullPointerException when there is null parameter
     */
    public void bind(Params params, Object[] args, Method method) {

        Objects.requireNonNull(params, "params is required");
        Objects.requireNonNull(args, "args is required");
        Objects.requireNonNull(method, "method is required");

        List<String> names = params.getNames();
        if (names.size() > args.length) {
            throw new DynamicQueryException("The number of parameters in a query is bigger than the number of " +
                    "parameters in the method: " + method);
        }
        for (int index = 0; index < names.size(); index++) {
            String name = names.get(index);
            String fieldName = name.substring(0, name.lastIndexOf("_"));
            Optional<FieldMapping> field = this.representation.getFields().stream()
                    .filter(f -> f.getName().equals(fieldName)).findFirst();

            Object value;
            if (field.isPresent()) {
                value = ConverterUtil.getValue(args[index], converters, field.get());
            } else {
                value = args[index];
            }
            params.bind(name, value);
        }
    }
}
