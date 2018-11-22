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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jnosql.artemis.reflection.ClassMapping;
import org.jnosql.query.Condition;
import org.jnosql.query.ConditionValue;
import org.jnosql.query.Operator;

abstract class AbstractQueryConvert {


    protected GraphTraversal<Vertex, Vertex> getPredicate(GraphQueryMethod graphQuery, Condition condition,
                                                        ClassMapping mapping) {
        Operator operator = condition.getOperator();
        String name = condition.getName();
        String nativeName = mapping.getColumnField(name);
        switch (operator) {
            case EQUALS:
                return __.has(nativeName, P.eq(graphQuery.getValue(name)));
            case GREATER_THAN:
                return __.has(nativeName, P.gt(graphQuery.getValue(name)));
            case GREATER_EQUALS_THAN:
                return __.has(nativeName, P.gte(graphQuery.getValue(name)));
            case LESSER_THAN:
                return __.has(nativeName, P.lt(graphQuery.getValue(name)));
            case LESSER_EQUALS_THAN:
                return __.has(nativeName, P.lte(graphQuery.getValue(name)));
            case BETWEEN:
                return __.has(nativeName, P.between(graphQuery.getValue(name), graphQuery.getValue(name)));
            case IN:
                return __.has(nativeName, P.within(graphQuery.getInValue(name)));
            case NOT:
                Condition notCondition = ConditionValue.class.cast(condition.getValue()).get().get(0);
                return __.not(getPredicate(graphQuery, notCondition, mapping));
            case AND:
                return ConditionValue.class.cast(condition.getValue()).get().stream()
                        .map(c -> getPredicate(graphQuery, c, mapping)).reduce(GraphTraversal::and)
                        .orElseThrow(() -> new UnsupportedOperationException("There is an inconsistency at the AND operator"));
            case OR:
                return ConditionValue.class.cast(condition.getValue()).get().stream()
                        .map(c -> getPredicate(graphQuery, c, mapping)).reduce(GraphTraversal::or)
                        .orElseThrow(() -> new UnsupportedOperationException("There is an inconsistency at the OR operator"));
            default:
                throw new UnsupportedOperationException("There is not support to the type " + operator + " in graph");


        }
    }
}
