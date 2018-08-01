/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2018, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.fluent.api.xml;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public final class Requirement {

    private Requirement() {}

    public static Object[] requirement(Consumer<DocumentWriter> actual, String expected) {
        return new Object[] {new DocumentWriterConsumer(actual, " should generate text"), expected};
    }

    public static Object[] negativeRequirement(Consumer<DocumentWriter> actual, String expected) {
        return new Object[] {new DocumentWriterConsumer(actual, " should throw"), expected};
    }

    private static class DocumentWriterConsumer implements Consumer<DocumentWriter> {

        private final Consumer<DocumentWriter> actual;
        private final String expectation;

        private DocumentWriterConsumer(Consumer<DocumentWriter> actual, String expectation) {
            this.actual = actual;
            this.expectation = expectation;
        }

        @Override
        public void accept(DocumentWriter documentWriter) {
            actual.accept(documentWriter);
        }

        @Override
        public String toString() {
            ToStringInvocationHandler handler = new ToStringInvocationHandler("w");
            actual.accept((DocumentWriter) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DocumentWriter.class}, handler));
            return handler.toString() + ' ' + expectation;
        }

        private class ToStringInvocationHandler implements InvocationHandler {

            private final String content;
            private final List<ToStringInvocationHandler> invocations = new ArrayList<>();

            private ToStringInvocationHandler(String content) {
                this.content = content;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getDeclaringClass().equals(Object.class)) {
                    return method.invoke(this);
                }
                String params = isNull(args) ? "" : stream(args).map(arg -> arg instanceof String ? "\"" + arg + "\"" : valueOf(arg)).collect(joining(", "));
                ToStringInvocationHandler handler = new ToStringInvocationHandler(method.getName() + '(' + params + ')');
                invocations.add(handler);
                if(method.getReturnType().equals(void.class)) {
                    return null;
                }
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{method.getReturnType()}, handler);
            }

            @Override
            public String toString() {
                switch (invocations.size()) {
                    case 0:
                        return content;
                    case 1:
                        return content + '.' + invocations.get(0);
                    default:
                        return content + invocations.stream().map(Objects::toString).collect(joining(";\nvar.", ";\nvar.", ";"));
                }

            }

        }

    }

}
