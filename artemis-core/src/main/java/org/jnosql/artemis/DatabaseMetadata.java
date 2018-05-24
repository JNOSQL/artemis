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

import java.util.Objects;

/**
 * This class is metadata to {@link Database}
 */
public final class DatabaseMetadata {

    private final DatabaseType type;

    private final String provider;

    private DatabaseMetadata(DatabaseType type, String provider) {
        this.type = type;
        this.provider = provider;
    }

    public DatabaseType getType() {
        return type;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DatabaseMetadata)) {
            return false;
        }
        DatabaseMetadata that = (DatabaseMetadata) o;
        return type == that.type &&
                Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, provider);
    }

    @Override
    public String toString() {
        return type + (provider == null ? "" : '@' + provider);
    }


    /**
     * creates a {@link DatabaseMetadata} instance
     *
     * @param database the database annotation
     * @return a {@link DatabaseMetadata} instance
     * @throws NullPointerException when database is null
     */
    public static DatabaseMetadata of(Database database) {
        Objects.requireNonNull(database, "database is required");
        return new DatabaseMetadata(database.value(), database.provider());
    }
}
