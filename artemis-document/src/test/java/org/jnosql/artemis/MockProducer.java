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


import org.jnosql.artemis.document.DocumentTemplate;
import org.jnosql.artemis.document.DocumentTemplateAsync;
import org.jnosql.artemis.model.Person;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsync;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockProducer {

    @Produces
    public DocumentCollectionManager getDocumentCollectionManager() {
        DocumentEntity entity = DocumentEntity.of("Person");
        entity.add(Document.of("name", "Default"));
        entity.add(Document.of("age", 10));
        DocumentCollectionManager manager = mock(DocumentCollectionManager.class);
        when(manager.insert(Mockito.any(DocumentEntity.class))).thenReturn(entity);
        return manager;

    }

    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    public DocumentCollectionManager getDocumentCollectionManagerMock() {
        DocumentEntity entity = DocumentEntity.of("Person");
        entity.add(Document.of("name", "documentRepositoryMock"));
        entity.add(Document.of("age", 10));
        DocumentCollectionManager manager = mock(DocumentCollectionManager.class);
        when(manager.insert(Mockito.any(DocumentEntity.class))).thenReturn(entity);
        when(manager.singleResult(Mockito.any(DocumentQuery.class))).thenReturn(Optional.empty());
        return manager;

    }



    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    public DocumentTemplate getDocumentRepository() {
        DocumentTemplate documentTemplate = mock(DocumentTemplate.class);
        when(documentTemplate.insert(Mockito.any(Person.class))).thenReturn(Person.builder()
                .withName("documentRepositoryMock").build());

        when(documentTemplate.singleResult(any(DocumentQuery.class))).thenReturn(Optional.empty());
        when(documentTemplate.find(eq(Person.class), Mockito.any())).thenReturn(Optional.empty());
        return documentTemplate;
    }


    @Produces
    public DocumentCollectionManagerAsync getDocumentCollectionManagerAsync() {
        return Mockito.mock(DocumentCollectionManagerAsync.class);
    }


    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    public DocumentCollectionManagerAsync getDocumentCollectionManagerAsyncMock() {
        return Mockito.mock(DocumentCollectionManagerAsync.class);
    }

    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    public DocumentTemplateAsync getDocumentRepositoryAsync() {
        return mock(DocumentTemplateAsync.class);
    }


}
