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

import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.diana.api.column.ColumnObserverParser;

import java.util.Optional;

final class ColumnMapperObserver implements ColumnObserverParser {


    private final ClassRepresentations representations;

    ColumnMapperObserver(ClassRepresentations representations) {
        this.representations = representations;
    }


    @Override
    public String fireEntity(String entity) {
        Optional<ClassMapping> classRepresentation = getClassRepresentation(entity);
        return classRepresentation.map(ClassMapping::getName).orElse(entity);
    }

    @Override
    public String fireField(String entity, String field) {
        Optional<ClassMapping> classRepresentation = getClassRepresentation(entity);
        return classRepresentation.map(c -> c.getColumnField(field)).orElse(field);
    }

    private Optional<ClassMapping> getClassRepresentation(String entity) {
        Optional<ClassMapping> bySimpleName = representations.findBySimpleName(entity);
        if (bySimpleName.isPresent()) {
            return bySimpleName;
        }
        return representations.findByClassName(entity);
    }

}
