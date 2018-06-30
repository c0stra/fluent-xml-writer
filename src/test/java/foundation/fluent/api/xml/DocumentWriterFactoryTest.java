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

import org.testng.annotations.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static foundation.fluent.api.xml.DocumentWriterFactory.*;
import static org.testng.Assert.assertEquals;

public class DocumentWriterFactoryTest {

    @Test
    public void testTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).open("element").close();
        assertEquals(writer.toString(), "<element/>");
    }

    @Test
    public void testVersionAndTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).version(1.0).open("element").close();
        assertEquals(writer.toString(), "<?xml version='1.0'?><element/>");
    }

    @Test
    public void testEncodingAndTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).encoding("UTF-8").open("element").close();
        assertEquals(writer.toString(), "<?xml encoding='UTF-8'?><element/>");
    }

    @Test
    public void testVersionEncodingAndTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).version(1.0).encoding("UTF-8").open("element").finish();
        assertEquals(writer.toString(), "<?xml version='1.0' encoding='UTF-8'?><element/>");
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "No root element created.")
    public void testVersionEncodingAndNoTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).version(1.0).encoding("UTF-8").finish();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Trying to output second root.")
    public void testVersionEncodingAndTwoTopLevelElement() {
        StringWriter writer = new StringWriter();
        document(writer).version(1.0).encoding("UTF-8").open("root").close().open("root").finish();
    }


    @Test
    public void testContent() {
        StringWriter writer = new StringWriter();
        document(writer)
                .version(1.0).encoding("UTF-8")
                .open("element").attribute("a", "b").xmlns("http://my/uri")
                    .text("aha")
                    .cdata("uuu")
                    .cdata(" f")
                .close();
        assertEquals(writer.toString(), "<?xml version='1.0' encoding='UTF-8'?><element a='b' xmlns='http://my/uri'>aha<!CDATA[uuu f]></element>");
    }

    @Test
    public void testLevel() {
        StringWriter writer = new StringWriter();
        ElementWriter<DocumentWriter> tag = document(writer).open("tag");
        tag.open("one").attribute("a", "u").text("1");
        tag.open("two").attribute("b", "v").text("2");
        tag.finish();
        assertEquals(writer.toString(), "<tag><one a='u'>1</one><two b='v'>2</two></tag>");
    }

    @Test(invocationCount = 5, enabled = false)
    public void testEscaping() throws IOException {
        //StringWriter writer = new StringWriter();
        Writer writer = new FileWriter("output.xml");
        ContentWriter<DocumentWriter> cdata = document(writer)
                .version(1.0).encoding("UTF-8")
                .open("element").attribute("a", "b&").xmlns("http://my/uri")
                .text("a\u0001ha< >")
                .cdata("uuu")
                .cdata(" f");
        for(int i = 0; i < 1000000; i++) {
            cdata                .open("another").attribute("counter", String.valueOf(Math.random())).attribute("ouha", "h & a")
                    .text("fgdsfg ' & > <     ")
                    .cdata("fdsf \u0010 fdsff \u0002")
                    .open("dalsi").attribute("ua", "au")
                    .close()
                    .close();

        }
                cdata.close().finish();

    }


    @Test(invocationCount = 5, enabled = false)
    public void testWriter() throws IOException, XMLStreamException {
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(new FileWriter("output2.xml"));
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("element");
        writer.writeAttribute("a", "b&");
        writer.writeNamespace("ns", "http://my/uri");
        writer.writeCharacters("aha < >");
        writer.writeCData("uuu");
        writer.writeCData(" f");

        for(int i = 0; i < 1000000; i++) {
            writer.writeStartElement("another");
            writer.writeAttribute("counter", String.valueOf(Math.random()));
            writer.writeAttribute("ouha", "h & a");
            writer.writeCharacters("fgdsfg ' & > <     ");
            writer.writeCharacters("fdsf \u0010 fdsff \u0002");
            writer.writeStartElement("dalsi");
            writer.writeAttribute("ua", "au");
            writer.writeEndElement();
            writer.writeEndElement();
        }

        writer.close();
    }

}
