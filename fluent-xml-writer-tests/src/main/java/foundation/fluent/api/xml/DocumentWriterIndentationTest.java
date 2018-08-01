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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.Consumer;

import static foundation.fluent.api.xml.DocumentWriterConfig.config;
import static foundation.fluent.api.xml.DocumentWriterFactory.document;
import static foundation.fluent.api.xml.Requirement.requirement;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class DocumentWriterIndentationTest {

    @DataProvider
    public Object[][] data() {
        return new Object[][] {
                requirement(
                        w -> w.tag("element").end(),
                        "<element/>"
                ),

                requirement(
                        w -> w.version(1.0).tag("element").end(),
                        "<?xml version='1.0'?>\n<element/>"
                ),

                requirement(
                        w -> w.version(1.0).comment("Top level comment").tag("element").end(),
                        "<?xml version='1.0'?>\n<!-- Top level comment -->\n<element/>"
                ),

                requirement(
                        w -> w.version(1.0).encoding("UTF-8").tag("element").comment("Comment inside element").close(),
                        "<?xml version='1.0' encoding='UTF-8'?>\n<element>\n    <!-- Comment inside element -->\n</element>"
                ),

                requirement(
                        w -> w.version(1.0).encoding("UTF-8")
                                .tag("element").attribute("a", "b").xmlns("http://my/uri")
                                .text("aha<")
                                .cdata("&uuu")
                                .cdata(" f")
                                .end(),
                        "<?xml version='1.0' encoding='UTF-8'?>\n<element a='b' xmlns='http://my/uri'>\n    aha&lt;\n    <![CDATA[&uuu f]]>\n</element>"
                ),

                requirement(
                        w -> {
                            ContentWriter tag = w.tag("tag");
                            tag.tag("one").attribute("a", "u").text("1");
                            tag.tag("two").attribute("b", "v").text("2");
                            tag.close();
                        },
                        "<tag>\n    <one a='u'>\n        1\n    </one>\n    <two b='v'>\n        2\n    </two>\n</tag>"
                ),

                requirement(
                        w -> w.version(1.0).instruction("xml-stylesheet", "href='style.css' type='text/css'").tag("root").instruction("php", "phpinfo()").close(),
                        "<?xml version='1.0'?>\n<?xml-stylesheet href='style.css' type='text/css'?>\n<root>\n    <?php phpinfo()?>\n</root>"
                )
        };
    }

    @Test(dataProvider = "data")
    public void testThat(Consumer<DocumentWriter> actual, String expected) throws ParserConfigurationException, IOException, SAXException {
        StringWriter writer = new StringWriter();
        actual.accept(document(writer, config().quot('\'').indentSpaces(4)));
        assertEquals(writer.toString(), expected);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(writer.toString().getBytes()));
        System.out.println(document);
    }

}
