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
package org.jnosql.artemis;

import javax.enterprise.inject.spi.ProcessProducer;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class util for check some data during CDI Extensions for Artemis
 */
public final class Databases {

    private static final Logger LOGGER = Logger.getLogger(Databases.class.getName());

    private Databases() {}

    /**
     * This method get the class from ProcessProduce, check if the object is a valid object for the database type
     * add it for a list passed for CDI's lifecycle.
     *
     * @param processProducer the {@link ProcessProducer} of CDI Exntesion
     * @param type type data which extension is scanning
     * @param databases list of objects which will be used by Artemis CDI Extension
     *
     * @see DatabaseType
     *
     */
    public static void addDatabase(final ProcessProducer processProducer, final DatabaseType type, final List<Database> databases) {
        LOGGER.info(String.format("Found the type %s to databases %s", type, databases));
        Set<Annotation> annotations = processProducer.getAnnotatedMember().getAnnotations();
        Optional<Database> databaseOptional = annotations.stream().filter(a -> a instanceof Database)
                .map(Database.class::cast).findFirst();
        databaseOptional.ifPresent(database -> {
            if (!type.equals(database.value())) {
                String simpleName = processProducer.getAnnotatedMember().getDeclaringType().getJavaClass().getSimpleName();
                throw new IllegalStateException(String.format("The %s is producing a wrong manager for %s type", simpleName, type));
            }
            databases.add(database);
        });
    }
}
