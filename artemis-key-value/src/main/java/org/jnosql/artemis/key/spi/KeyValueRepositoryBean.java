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
package org.jnosql.artemis.key.spi;


import org.jnosql.artemis.DatabaseQualifier;
import org.jnosql.artemis.DatabaseType;
import org.jnosql.artemis.key.KeyValueTemplate;
import org.jnosql.artemis.key.KeyValueTemplateProducer;
import org.jnosql.diana.api.key.BucketManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class KeyValueRepositoryBean implements Bean<KeyValueTemplate>, PassivationCapable {

    private final BeanManager beanManager;

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param beanManager the beanManager
     * @param provider    the provider name, that must be a
     */
    public KeyValueRepositoryBean(BeanManager beanManager, String provider) {
        this.beanManager = beanManager;
        this.types = Collections.singleton(KeyValueTemplate.class);
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofKeyValue(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return KeyValueTemplate.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public KeyValueTemplate create(CreationalContext<KeyValueTemplate> creationalContext) {

        KeyValueTemplateProducer producer = getInstance(KeyValueTemplateProducer.class);
        BucketManager manager = getManager();
        return producer.get(manager);
    }

    private BucketManager getManager() {
        Bean<BucketManager> bean = (Bean<BucketManager>) beanManager.getBeans(BucketManager.class,
                DatabaseQualifier.ofKeyValue(provider) ).iterator().next();
        CreationalContext<BucketManager> ctx = beanManager.createCreationalContext(bean);
        return (BucketManager) beanManager.getReference(bean, BucketManager.class, ctx);
    }


    private <T> T getInstance(Class<T> clazz) {
        Bean<T> bean = (Bean<T>) beanManager.getBeans(clazz).iterator().next();
        CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, clazz, ctx);
    }

    @Override
    public void destroy(KeyValueTemplate instance, CreationalContext<KeyValueTemplate> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return KeyValueTemplate.class.getName() + DatabaseType.COLUMN + "-" + provider;
    }

}
