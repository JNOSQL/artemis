package org.jnosql.artemis.document.query;


import org.jnosql.diana.api.document.DocumentDeleteQuery;

/**
 * The last step to the build of {@link DocumentDeleteQuery}.
 * It either can return a new {@link DocumentDeleteQuery} instance or execute a query with
 * {@link org.jnosql.diana.api.document.DocumentCollectionManager} and {@link DocumentCollectionManagerAsync}
 */
public interface DocumentMapperDeleteQueryBuild {
    /**
     * Creates a new instance of {@link DocumentDeleteQuery}
     *
     * @return a new {@link DocumentDeleteQuery} instance
     */
    DocumentDeleteQuery build();
}
