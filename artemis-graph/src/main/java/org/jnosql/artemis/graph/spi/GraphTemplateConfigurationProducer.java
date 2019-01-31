/*
 *  Copyright (c) 2019 Otávio Santana and others
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
package org.jnosql.artemis.graph.spi;

import org.jnosql.artemis.ConfigurationUnit;
import org.jnosql.artemis.graph.GraphTemplate;
import org.jnosql.artemis.graph.GraphTemplateProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * It creates a {@link org.jnosql.artemis.graph.GraphTemplate} from a ConfigurationUnit annotation.
 */
@ApplicationScoped
class GraphTemplateConfigurationProducer {

    @Inject
    private GraphConfigurationProducer configurationProducer;

    @Inject
    private GraphTemplateProducer producer;


    @ConfigurationUnit
    @Produces
    public GraphTemplate getGraph(InjectionPoint injectionPoint) {
        return producer.get(configurationProducer.getGraph(injectionPoint));
    }
}
