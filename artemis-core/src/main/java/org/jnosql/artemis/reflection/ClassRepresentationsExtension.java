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


import org.jnosql.artemis.Embeddable;
import org.jnosql.artemis.Entity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jnosql.artemis.reflection.ClassOperationFactory.INSTANCE;

/**
 * This class is a CDI extension to load all class that has {@link Entity} annotation.
 * This extension will load all Classes and put in a map.
 * Where the key is {@link Class#getName()} and the value is {@link ClassMapping}
 */
@ApplicationScoped
public class ClassRepresentationsExtension implements Extension {

    private final Map<String, ClassMapping> representations = new ConcurrentHashMap<>();

    private final Map<Class<?>, ClassMapping> classes = new ConcurrentHashMap<>();

    private final ClassConverter classConverter;

    {
        classConverter = new ClassConverter(INSTANCE.getReflections());
    }

    /**
     * Event observer
     *
     * @param target the target
     * @param <T>    the type
     */
    public <T> void initializePropertyLoading(@Observes final ProcessAnnotatedType<T> target) {

        AnnotatedType<T> annotatedType = target.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Entity.class)) {
            Class<T> javaClass = target.getAnnotatedType().getJavaClass();
            ClassMapping classMapping = classConverter.create(javaClass);
            representations.put(classMapping.getName(), classMapping);
            classes.put(javaClass, classMapping);
        } else if (isSubElement(annotatedType)) {
            Class<T> javaClass = target.getAnnotatedType().getJavaClass();
            ClassMapping classMapping = classConverter.create(javaClass);
            classes.put(javaClass, classMapping);
        }

    }

    private <T> boolean isSubElement(AnnotatedType<T> annotatedType) {
        return annotatedType.isAnnotationPresent(Embeddable.class);
    }


    /**
     * Returns the representations loaded in CDI startup
     *
     * @return the class loaded
     */
    public Map<String, ClassMapping> getRepresentations() {
        return representations;
    }

    /**
     * Returns all class found in the process grouped by Java class
     *
     * @return the map instance
     */
    public Map<Class<?>, ClassMapping> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClassRepresentationsExtension{");
        sb.append("classConverter=").append(classConverter);
        sb.append(", representations-size=").append(representations.size());
        sb.append(", classes=").append(classes);
        sb.append('}');
        return sb.toString();
    }
}
