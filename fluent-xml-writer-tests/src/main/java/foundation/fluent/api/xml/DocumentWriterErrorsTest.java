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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Writer;
import java.util.function.Consumer;

import static foundation.fluent.api.xml.DocumentWriterConfig.config;
import static foundation.fluent.api.xml.DocumentWriterFactory.document;
import static foundation.fluent.api.xml.Requirement.negativeRequirement;
import static org.testng.Assert.assertEquals;

public class DocumentWriterErrorsTest {

    @DataProvider
    public Object[][] data() {
        return new Object[][] {
                negativeRequirement(
                        w -> w.version(1.0).encoding("UTF-8").close(),
                        "No root element created."
                ),

                negativeRequirement(
                        w -> w.tag("root").end().end().close(),
                        "No open element to close."
                ),

                negativeRequirement(
                        w -> w.text("sfv").close(),
                        "Cannot write text out of the root element."
                ),

                negativeRequirement(
                        w -> w.cdata("sfv").close(),
                        "Cannot write CDATA out of the root element."
                ),

                negativeRequirement(
                        w -> {
                            w.version(1.0).encoding("UTF-8");
                            w.tag("root").end();
                            w.tag("root").close();
                        },
                        "Trying to output second root."
                ),

                negativeRequirement(
                        w -> {
                            w.tag("root").end();
                            w.version(1.0);
                        },
                        "XML spec must be first in the document."
                )
        };
    }

    @Test(dataProvider = "data")
    public void testThat(Consumer<DocumentWriter> actual, String expected) {
        try {
            actual.accept(document(new Writer() {
                @Override public void write(char[] cbuf, int off, int len) { }
                @Override public void flush() { }
                @Override public void close() { }
            }, config().singleQuoteValue()));
            throw new AssertionError("IllegalStateException with message '" + expected + "' expected, but no exception thrown.");
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage(), expected);
        }
    }

}
