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
package org.jnosql.artemis.key;


import org.jnosql.diana.api.key.KeyValueEntity;

import java.util.Objects;

public interface KeyValueEntityPrePersist {

    /**
     * The {@link org.jnosql.diana.api.key.KeyValueEntity}  after be saved
     *
     * @return the {@link KeyValueEntity} instance
     */
    KeyValueEntity<?> getEntity();

    /**
     * Creates the {@link KeyValueEntityPrePersist} instance
     *
     * @param entity the entity
     * @return {@link KeyValueEntityPrePersist} instance
     * @throws NullPointerException when the entity is null
     */
    static <T> KeyValueEntityPrePersist of(KeyValueEntity<T> entity) {
        Objects.requireNonNull(entity, "Entity is required");
        return new DefaultKeyValueEntityPrePersist(entity);
    }
}
