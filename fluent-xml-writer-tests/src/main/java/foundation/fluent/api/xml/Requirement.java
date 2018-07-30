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
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Requirement {

    public static Object[] requirement(Consumer<DocumentWriter> actual, String expected) {
        return new Object[] {new Consumer<DocumentWriter>() {

            @Override
            public void accept(DocumentWriter documentWriter) {
                actual.accept(documentWriter);
            }

            @Override
            public String toString() {
                StringBuilder stringBuilder = new StringBuilder("w");
                actual.accept((DocumentWriter) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DocumentWriter.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if(method.getDeclaringClass().equals(Object.class)) {
                            return method.invoke(this);
                        }
                        stringBuilder.append('.').append(method.getName()).append('(');
                        if(args != null) {
                            stringBuilder.append(Arrays.stream(args).map(arg -> arg instanceof String ? "\"" + arg + "\"" : String.valueOf(arg)).collect(Collectors.joining(", ")));
                        }
                        stringBuilder.append(')');
                        if(method.getReturnType().equals(void.class)) {
                            return null;
                        }
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{method.getReturnType()}, this);
                    }
                }));
                return stringBuilder.toString();
            }
        }, expected};
    }

}
