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

import org.jnosql.artemis.AttributeConverter;
import org.jnosql.artemis.reflection.FieldRepresentation;
import org.jnosql.artemis.reflection.GenericFieldRepresentation;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.Value;
import org.jnosql.diana.api.column.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.jnosql.artemis.reflection.FieldType.COLLECTION;
import static org.jnosql.artemis.reflection.FieldType.EMBEDDED;
import static org.jnosql.artemis.reflection.FieldType.SUBENTITY;

class ColumnFieldConverters {

    static class ColumnFieldConverterFactory {

        private final EmbeddedFieldConverter embeddedFieldConverter = new EmbeddedFieldConverter();
        private final DefaultConverter defaultConverter = new DefaultConverter();
        private final CollectionEmbeddableConverter embeddableConverter = new CollectionEmbeddableConverter();
        private final SubEntityConverter subEntityConverter = new SubEntityConverter();

        ColumnFieldConverter get(FieldRepresentation field) {
            if (EMBEDDED.equals(field.getType())) {
                return embeddedFieldConverter;
            } else if (SUBENTITY.equals(field.getType())) {
                return subEntityConverter;
            } else if (isCollectionEmbeddable(field)) {
                return embeddableConverter;
            } else {
                return defaultConverter;
            }
        }

        private boolean isCollectionEmbeddable(FieldRepresentation field) {
            return COLLECTION.equals(field.getType()) && GenericFieldRepresentation.class.cast(field).isEmbeddable();
        }
    }


    private static class EmbeddedFieldConverter implements ColumnFieldConverter {

        @Override
        public <T> void convert(T instance, List<Column> columns, Optional<Column> column, FieldRepresentation field,
                                AbstractColumnEntityConverter converter) {

            if (column.isPresent()) {
                Column subColumn = column.get();
                Object value = subColumn.get();
                if (Map.class.isInstance(value)) {
                    Map map = Map.class.cast(value);
                    List<Column> embeddedColumns = new ArrayList<>();

                    for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                        embeddedColumns.add(Column.of(entry.getKey().toString(), entry.getValue()));
                    }
                    converter.getReflections().setValue(instance, field.getNativeField(), converter.toEntity(field.getNativeField().getType(), embeddedColumns));
                } else {
                    converter.getReflections().setValue(instance, field.getNativeField(), converter.toEntity(field.getNativeField().getType(),
                            subColumn.get(new TypeReference<List<Column>>() {
                            })));
                }

            } else {
                converter.getReflections().setValue(instance, field.getNativeField(), converter.toEntity(field.getNativeField().getType(), columns));
            }
        }
    }

    private static class DefaultConverter implements ColumnFieldConverter {


        @Override
        public <T> void convert(T instance, List<Column> columns, Optional<Column> column,
                                FieldRepresentation field, AbstractColumnEntityConverter converter) {
            Value value = column.get().getValue();
            Optional<Class<? extends AttributeConverter>> optionalConverter = field.getConverter();
            if (optionalConverter.isPresent()) {

                AttributeConverter attributeConverter = converter.getConverters().get(optionalConverter.get());
                Object attributeConverted = attributeConverter.convertToEntityAttribute(value.get());
                converter.getReflections().setValue(instance, field.getNativeField(), field.getValue(Value.of(attributeConverted)));
            } else {
                converter.getReflections().setValue(instance, field.getNativeField(), field.getValue(value));
            }
        }
    }

    private static class SubEntityConverter implements ColumnFieldConverter {


        @Override
        public <T> void convert(T instance, List<Column> columns, Optional<Column> column,
                                FieldRepresentation field, AbstractColumnEntityConverter converter) {


            Field nativeField = field.getNativeField();
            Object subEntity = converter.toEntity(nativeField.getType(), columns);
            converter.getReflections().setValue(instance, nativeField, subEntity);

        }
    }

    private static class CollectionEmbeddableConverter implements ColumnFieldConverter {

        @Override
        public <T> void convert(T instance, List<Column> columns, Optional<Column> column, FieldRepresentation field,
                                AbstractColumnEntityConverter converter) {

            column.ifPresent(convertColumn(instance, field, converter));
        }

        private <T> Consumer<Column> convertColumn(T instance, FieldRepresentation field, AbstractColumnEntityConverter converter) {
            return column -> {
                GenericFieldRepresentation genericField = GenericFieldRepresentation.class.cast(field);
                Collection collection = genericField.getCollectionInstance();
                List<List<Column>> embeddable = (List<List<Column>>) column.get();
                for (List<Column> columnList : embeddable) {
                    Object element = converter.toEntity(genericField.getElementType(), columnList);
                    collection.add(element);
                }
                converter.getReflections().setValue(instance, field.getNativeField(), collection);
            };
        }
    }
}
