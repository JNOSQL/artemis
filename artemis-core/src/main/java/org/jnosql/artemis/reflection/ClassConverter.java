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

import org.jnosql.artemis.Convert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
class ClassConverter {


    private Reflections reflections;

    private FieldWriterFactory writerFactory;

    private FieldReaderFactory readerFactory;

    private InstanceSupplierFactory instanceSupplierFactory;


    @Inject
    ClassConverter(Reflections reflections, ClassOperation classOperation) {
        this.reflections = reflections;
        this.readerFactory = classOperation.getFieldReaderFactory();
        this.writerFactory = classOperation.getFieldWriterFactory();
        this.instanceSupplierFactory = classOperation.getInstanceSupplierFactory();
    }

    ClassConverter() {
    }

    public ClassRepresentation create(Class<?> entityClass) {


        String entityName = reflections.getEntityName(entityClass);

        List<FieldRepresentation> fields = reflections.getFields(entityClass)
                .stream().map(this::to).collect(toList());

        List<String> fieldsName = fields.stream().map(FieldRepresentation::getName).collect(toList());

        Map<String, NativeMapping> nativeFieldGroupByJavaField =
                getNativeFieldGroupByJavaField(fields, "", "");

        Map<String, FieldRepresentation> fieldsGroupedByName = fields.stream()
                .collect(collectingAndThen(toMap(FieldRepresentation::getName,
                        Function.identity()), Collections::unmodifiableMap));

        InstanceSupplier instanceSupplier = instanceSupplierFactory.apply(reflections.makeAccessible(entityClass));

        return DefaultClassRepresentation.builder().withName(entityName)
                .withClassInstance(entityClass)
                .withFields(fields)
                .withFieldsName(fieldsName)
                .withInstanceSupplier(instanceSupplier)
                .withJavaFieldGroupedByColumn(nativeFieldGroupByJavaField)
                .withFieldsGroupedByName(fieldsGroupedByName)
                .build();
    }

    private Map<String, NativeMapping> getNativeFieldGroupByJavaField(List<FieldRepresentation> fields,
                                                                      String javaField, String nativeField) {

        Map<String, NativeMapping> nativeFieldGrouopByJavaField = new HashMap<>();

        for (FieldRepresentation field : fields) {
            appendValue(nativeFieldGrouopByJavaField, field, javaField, nativeField);
        }

        return nativeFieldGrouopByJavaField;
    }

    private void appendValue(Map<String, NativeMapping> nativeFieldGroupByJavaField, FieldRepresentation field,
                             String javaField, String nativeField) {


        switch (field.getType()) {
            case SUBENTITY:
                appendFields(nativeFieldGroupByJavaField, field, javaField, appendPreparePrefix(nativeField, field.getName()));
                return;
            case EMBEDDED:
                appendFields(nativeFieldGroupByJavaField, field, javaField, nativeField);
                return;
            case COLLECTION:
                if (GenericFieldRepresentation.class.cast(field).isEmbeddable()) {
                    Class<?> entityClass = GenericFieldRepresentation.class.cast(field).getElementType();
                    String nativeFieldAppended = appendPreparePrefix(nativeField, field.getName());
                    appendFields(nativeFieldGroupByJavaField, field, javaField, nativeFieldAppended, entityClass);
                    return;
                }
                appendDefaultField(nativeFieldGroupByJavaField, field, javaField, nativeField);
                return;
            default:
                appendDefaultField(nativeFieldGroupByJavaField, field, javaField, nativeField);
        }

    }

    private void appendDefaultField(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                                    FieldRepresentation field, String javaField, String nativeField) {

        nativeFieldGroupByJavaField.put(javaField.concat(field.getFieldName()),
                NativeMapping.of(nativeField.concat(field.getName()), field));
    }

    private void appendFields(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                              FieldRepresentation field,
                              String javaField, String nativeField) {

        Class<?> entityClass = field.getNativeField().getType();
        appendFields(nativeFieldGroupByJavaField, field, javaField, nativeField, entityClass);
    }

    private void appendFields(Map<String, NativeMapping> nativeFieldGroupByJavaField,
                              FieldRepresentation field, String javaField, String nativeField,
                              Class<?> entityClass) {

        Map<String, NativeMapping> entityMap = getNativeFieldGroupByJavaField(
                reflections.getFields(entityClass)
                        .stream().map(this::to).collect(toList()),
                appendPreparePrefix(javaField, field.getFieldName()), nativeField);

        String nativeElement = entityMap.values().stream().map(NativeMapping::getNativeField)
                .collect(Collectors.joining(","));

        nativeFieldGroupByJavaField.put(appendPrefix(javaField, field.getFieldName()), NativeMapping.of(nativeElement, field));
        nativeFieldGroupByJavaField.putAll(entityMap);
    }

    private String appendPreparePrefix(String prefix, String field) {
        return appendPrefix(prefix, field).concat(".");
    }

    private String appendPrefix(String prefix, String field) {
        if (prefix.isEmpty()) {
            return field;
        } else {
            return prefix.concat(field);
        }
    }


    private FieldRepresentation to(Field field) {
        FieldType fieldType = FieldType.of(field);
        reflections.makeAccessible(field);
        Convert convert = field.getAnnotation(Convert.class);
        boolean id = reflections.isIdField(field);
        String columnName = id ? reflections.getIdName(field) : reflections.getColumnName(field);

        FieldRepresentationBuilder builder = FieldRepresentation.builder().withName(columnName)
                .withField(field).withType(fieldType).withId(id)
                .withReader(readerFactory.apply(field))
                .withWriter(writerFactory.apply(field));

        if (nonNull(convert)) {
            builder.withConverter(convert.value());
        }
        switch (fieldType) {
            case COLLECTION:
            case MAP:
                builder.withTypeSupplier(field::getGenericType);
                return builder.buildGeneric();
            case EMBEDDED:
                return builder.withEntityName(reflections.getEntityName(field.getType())).buildEmedded();
            default:
                return builder.buildDefault();


        }
    }

}
