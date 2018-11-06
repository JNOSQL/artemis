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


import java.lang.reflect.Field;
import java.util.Objects;

public final class EmbeddedFieldRepresentation extends AbstractFieldRepresentation {

    private final String entityName;

    public EmbeddedFieldRepresentation(FieldType type, Field field, String name, String entityName,
                                       FieldReader reader, FieldWriter writer) {
        super(type, field, name, null, reader, writer);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmbeddedFieldRepresentation that = (EmbeddedFieldRepresentation) o;
        return type == that.type &&
                Objects.equals(field, that.field) &&
                Objects.equals(entityName, that.entityName) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, field, name, entityName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmbeddedFieldRepresentation{");
        sb.append("entityName='").append(entityName).append('\'');
        sb.append(", type=").append(type);
        sb.append(", field=").append(field);
        sb.append(", name='").append(name).append('\'');
        sb.append(", fieldName='").append(fieldName).append('\'');
        sb.append(", converter=").append(converter);
        sb.append('}');
        return sb.toString();
    }
}
