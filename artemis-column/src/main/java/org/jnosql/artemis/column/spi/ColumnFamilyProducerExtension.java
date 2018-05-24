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
package org.jnosql.artemis.column.spi;


import org.jnosql.artemis.DatabaseMetadata;
import org.jnosql.artemis.Databases;
import org.jnosql.artemis.Repository;
import org.jnosql.artemis.RepositoryAsync;
import org.jnosql.artemis.column.query.RepositoryAsyncColumnBean;
import org.jnosql.artemis.column.query.RepositoryColumnBean;
import org.jnosql.diana.api.column.ColumnFamilyManager;
import org.jnosql.diana.api.column.ColumnFamilyManagerAsync;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducer;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.jnosql.artemis.DatabaseType.COLUMN;

/**
 * Extension to start up the ColumnTemplate, ColumnTemplateAsync, Repository and RepositoryAsync
 * from the {@link org.jnosql.artemis.Database} qualifier
 */
public class ColumnFamilyProducerExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(ColumnFamilyProducerExtension.class.getName());

    private final Set<DatabaseMetadata> databases = new HashSet<>();

    private final Set<DatabaseMetadata> databasesAsync = new HashSet<>();

    private final Collection<Class<?>> crudTypes = new HashSet<>();

    private final Collection<Class<?>> crudAsyncTypes = new HashSet<>();


    <T extends Repository> void onProcessAnnotatedType(@Observes final ProcessAnnotatedType<T> repo) {
        Class<T> javaClass = repo.getAnnotatedType().getJavaClass();
        if (Repository.class.equals(javaClass)) {
            return;
        }
        if (Stream.of(javaClass.getInterfaces()).anyMatch(Repository.class::equals)
                && Modifier.isInterface(javaClass.getModifiers())) {
            LOGGER.info("Adding a new Repository as discovered on Column: " + javaClass);
            crudTypes.add(javaClass);
        }
    }

    <T extends RepositoryAsync> void onProcessAnnotatedTypeAsync(@Observes final ProcessAnnotatedType<T> repo) {
        Class<T> javaClass = repo.getAnnotatedType().getJavaClass();
        if (RepositoryAsync.class.equals(javaClass)) {
            return;
        }
        if (Stream.of(javaClass.getInterfaces()).anyMatch(RepositoryAsync.class::equals)
                && Modifier.isInterface(javaClass.getModifiers())) {
            LOGGER.info("Adding a new RepositoryAsync as discovered on Column: " + javaClass);
            crudAsyncTypes.add(javaClass);
        }
    }

    <T, X extends ColumnFamilyManager> void processProducer(@Observes final ProcessProducer<T, X> pp) {
        Databases.addDatabase(pp, COLUMN, databases);
    }

    <T, X extends ColumnFamilyManagerAsync> void processProducerAsync(@Observes final ProcessProducer<T, X> pp) {
        Databases.addDatabase(pp, COLUMN, databasesAsync);
    }

    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        LOGGER.info(String.format("Starting to process on columns: %d databases crud %d and crudAsync %d",
                databases.size(), crudTypes.size(), crudAsyncTypes.size()));
        databases.forEach(type -> {
            final ColumnTemplateBean bean = new ColumnTemplateBean(beanManager, type.getProvider());
            afterBeanDiscovery.addBean(bean);
        });

        databasesAsync.forEach(type -> {
            final ColumnTemplateAsyncBean bean = new ColumnTemplateAsyncBean(beanManager, type.getProvider());
            afterBeanDiscovery.addBean(bean);
        });

        crudTypes.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_COLUMN)) {
                afterBeanDiscovery.addBean(new RepositoryColumnBean(type, beanManager, ""));
            }
            databases.forEach(database -> afterBeanDiscovery
                    .addBean(new RepositoryColumnBean(type, beanManager, database.getProvider())));
        });

        crudAsyncTypes.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_COLUMN)) {
                afterBeanDiscovery.addBean(new RepositoryAsyncColumnBean(type, beanManager, ""));
            }

            databasesAsync.forEach(database -> afterBeanDiscovery
                    .addBean(new RepositoryAsyncColumnBean(type, beanManager, database.getProvider())));
        });


    }

}
