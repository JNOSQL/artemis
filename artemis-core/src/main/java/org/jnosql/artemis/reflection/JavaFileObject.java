/*
 *  Copyright (c) 2018 Otávio Santana and others
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
package org.jnosql.artemis.reflection;


import java.net.URI;
import javax.tools.SimpleJavaFileObject;

final class JavaFileObject extends SimpleJavaFileObject {

    private final String javaSource;

    public JavaFileObject(String fullClassName, String javaSource) {
        super(URI.create("string:///" + fullClassName.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.javaSource = javaSource;
    }

    @Override
    public String getCharContent(boolean ignoreEncodingErrors) {
        return javaSource;
    }

}