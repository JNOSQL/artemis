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
package org.jnosql.artemis.graph.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.aphrodite.antlr.method.DeleteMethodFactory;
import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.query.Condition;
import org.jnosql.query.DeleteQuery;
import org.jnosql.query.Where;

import java.util.List;
import java.util.function.Function;

final class DeleteQueryConverter extends AbstractQueryConvert implements Function<GraphQueryMethod, List<Vertex>> {

    @Override
    public List<Vertex> apply(GraphQueryMethod graphQuery) {
        DeleteMethodFactory factory = DeleteMethodFactory.get();
        DeleteQuery deleteQuery = factory.apply(graphQuery.getMethod(), graphQuery.getEntityName());
        ClassMapping representation = graphQuery.getRepresentation();
        GraphTraversal<Vertex, Vertex> traversal = graphQuery.getTraversal();
        if (deleteQuery.getWhere().isPresent()) {
            Where where = deleteQuery.getWhere().get();

            Condition condition = where.getCondition();
            traversal.filter(getPredicate(graphQuery, condition, representation));
        }

        traversal.hasLabel(representation.getName());
        return traversal.toList();
    }
}
