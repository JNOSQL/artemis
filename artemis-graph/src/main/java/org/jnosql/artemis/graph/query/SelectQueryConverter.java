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
import org.jnosql.aphrodite.antlr.method.SelectMethodFactory;
import org.jnosql.artemis.reflection.ClassRepresentation;
import org.jnosql.query.Condition;
import org.jnosql.query.SelectQuery;
import org.jnosql.query.Sort;
import org.jnosql.query.Where;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.incr;

final class SelectQueryConverter extends AbstractQueryConvert implements Function<GraphQueryMethod, List<Vertex>> {


    @Override
    public List<Vertex> apply(GraphQueryMethod graphQuery) {

        SelectMethodFactory selectMethodFactory = SelectMethodFactory.get();
        SelectQuery query = selectMethodFactory.apply(graphQuery.getMethod(), graphQuery.getEntityName());
        ClassRepresentation representation = graphQuery.getRepresentation();

        GraphTraversal<Vertex, Vertex> traversal = graphQuery.getTraversal();
        if (query.getWhere().isPresent()) {
            Where where = query.getWhere().get();

            Condition condition = where.getCondition();
            traversal.filter(getPredicate(graphQuery, condition, representation));
        }

        if (query.getSkip() > 0) {
            traversal.skip(query.getSkip());
        }

        if (query.getLimit() > 0) {
            return traversal.next((int) query.getLimit());
        }
        query.getOrderBy().forEach(getSort(traversal, representation));
        traversal.hasLabel(representation.getName());
        return traversal.toList();
    }

    private Consumer<Sort> getSort(GraphTraversal<Vertex, Vertex> traversal, ClassRepresentation representation) {
        return o -> {
            if (Sort.SortType.ASC.equals(o.getType())) {
                traversal.order().by(representation.getColumnField(o.getName()), incr);
            } else {
                traversal.order().by(representation.getColumnField(o.getName()), decr);
            }
        };
    }

}
