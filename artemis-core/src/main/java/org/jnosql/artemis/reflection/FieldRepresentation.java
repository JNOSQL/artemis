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


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Optional;

import org.jnosql.artemis.AttributeConverter;
import org.jnosql.diana.api.Value;

/**
 * This class represents the information from {@link Field}.
 * The strategy is do cache in all fields in a class to either read and writer faster from Field
 */
public interface FieldRepresentation extends Serializable {

    /**
     * Return the type of the field
     *
     * @return the {@link FieldType}
     */
    FieldType getType();

    /**
     * The {@link Field}
     *
     * @return the field
     */
    Field getNativeField();

    /**
     * Returns the {@link GetterAcessor}
     * @return the {@link GetterAcessor} instance
     */
    GetterAcessor getGetterAcessor();

    /**
     * Returns the {@link SetterAcessor}
     * @return the {@link SetterAcessor} instance
     */
    SetterAcessor getSetterAcessor();

    /**
     * Returns the name of the field that can be eiher the field name
     * or {@link org.jnosql.artemis.Column#value()}
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the Java Fields name.
     * {@link Field#getName()}
     *
     * @return The Java Field name {@link Field#getName()}
     */
    String getFieldName();


    /**
     * Returns the object from the field type
     *
     * @param value the value {@link Value}
     * @return the instance from the field type
     */
    Object getValue(Value value);

    /**
     * Returns true is the field is annotated with {@link org.jnosql.artemis.Id}
     *
     * @return true is annotated with {@link org.jnosql.artemis.Id}
     */
    boolean isId();

    /**
     * Returns the converter class
     *
     * @param <T> the Converter
     * @return the converter if present
     */
    <T extends AttributeConverter> Optional<Class<? extends AttributeConverter>> getConverter();

    /**
     * Creates the FieldRepresentationBuilder
     *
     * @return a new Builder instance
     */
    static FieldRepresentationBuilder builder() {
        return new FieldRepresentationBuilder();
    }

}
