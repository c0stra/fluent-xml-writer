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

package foundation.fluent.api.xml.impl;

import foundation.fluent.api.xml.*;
import foundation.fluent.api.xml.writer.CDataWriter;
import foundation.fluent.api.xml.writer.EscapingWriter;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.function.Supplier;

import static foundation.fluent.api.xml.impl.DocumentWriterImpl.DocumentState.*;
import static foundation.fluent.api.xml.impl.DocumentWriterImpl.ElementState.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class DocumentWriterImpl implements
        DocumentWriter.XmlSpecWriter,
        DocumentWriter.DoctypeWriter,
        Supplier<ContentWriter> {

    enum DocumentState {EMPTY, SPEC, DOCTYPE, PREFIX, OPEN, FINISHED}
    enum ElementState {OPENING, CONTENT, CDATA, CLOSED}

    private final DocumentWriterConfig config;
    private final PrintWriter writer;
    private final PrintWriter escapingWriter;
    private final PrintWriter cdataWriter;
    private ElementWriter child;
    private DocumentState state = EMPTY;

    public static DocumentWriter documentBuilder(Writer writer, DocumentWriterConfig config) {
        Writer cdataWriter = new CDataWriter(writer);
        return new DocumentWriterImpl(config, new PrintWriter(writer), new PrintWriter(cdataWriter), new PrintWriter(new EscapingWriter(cdataWriter)));
    }

    private DocumentWriterImpl(DocumentWriterConfig config, PrintWriter writer, PrintWriter cdataWriter, PrintWriter escapingWriter) {
        this.config = config;
        this.writer = writer;
        this.cdataWriter = cdataWriter;
        this.escapingWriter = escapingWriter;
    }

    private XmlSpecWriter set(String name, String value) {
        switch (state) {
            case EMPTY:
                writer.write("<?xml " + name + "=" + config.attrQuot + value + config.attrQuot);
                state = SPEC;
                break;
            case SPEC:
                writer.write(" " + name + "=" + config.attrQuot + value + config.attrQuot);
                break;
            default:
                throw new IllegalStateException("XML spec must be first in the document.");
        }
        return this;
    }

    private void toContent() {
        switch (state) {
            case SPEC: writer.write('?');
            case DOCTYPE: writer.write('>');
            case EMPTY: state = PREFIX;
            case PREFIX: return;
            case OPEN:
                child.end();
                state = FINISHED;
            default:
                // Nothing
        }
    }

    @Override
    public XmlSpecWriter version(String version) {
        return set("version", version);
    }

    @Override
    public DoctypeWriter doctype(String name) {
        switch (state) {
            case SPEC:
                writer.write("?><!DOCTYPE " + name);
                break;
            case PREFIX:
            case EMPTY:
                writer.write("<!DOCTYPE " + name);
                break;
            default:
                throw new IllegalStateException("DOCTYPE specification not allowed here.");
        }
        state = DOCTYPE;
        return this;
    }

    @Override
    public DoctypeWriter publicDtd(String uri, String dtd) {
        if(state != DOCTYPE) {
            throw new IllegalStateException("Not in DOCTYPE definition.");
        }
        writer.write(" PUBLIC " + config.attrQuot);
        escapingWriter.write(uri);
        writer.write(config.attrQuot + ' ' + config.attrQuot);
        escapingWriter.write(dtd);
        writer.write(config.attrQuot);
        return this;
    }

    @Override
    public DoctypeWriter systemDtd(String dtd) {
        if(state != DOCTYPE) {
            throw new IllegalStateException("Not in DOCTYPE definition.");
        }
        writer.write(" SYSTEM " + config.attrQuot);
        escapingWriter.write(dtd);
        writer.write(config.attrQuot);
        return this;
    }

    @Override
    public DocumentWriter flush() {
        writer.flush();
        return this;
    }

    @Override
    public XmlSpecWriter encoding(String encoding) {
        return set("encoding", encoding);
    }

    @Override
    public DocumentWriter instruction(String name, String content) {
        toContent();
        writer.write(config.prettyPrint + "<?" + name + " " + content + "?>");
        return this;
    }

    @Override
    public ElementWriter tag(String tag) {
        switch (state) {
            case EMPTY:
                writer.write('<' + tag);
                break;
            case SPEC:
                writer.write("?>" + config.prettyPrint + '<' + tag);
                break;
            case DOCTYPE:
                writer.write(">" + config.prettyPrint + '<' + tag);
                break;
            case PREFIX:
                writer.write(config.prettyPrint + '<' + tag);
                break;
            default:
                throw new IllegalStateException("Trying to output second root.");
        }
        state = OPEN;
        return child = new ElementWriterImpl(config.prettyPrint, tag, this);
    }

    @Override
    public ElementWriter tag(String nsPrefix, String tag) {
        return tag(nsPrefix + ':' + tag);
    }

    @Override
    public ContentWriter text(String content) {
        if(isNull(content)) {
            return this;
        }
        for(int i = 0; i < content.length(); i++) {
            if(!Character.isWhitespace(content.charAt(i))) {
                throw new IllegalStateException("Cannot write text out of the root element.");
            }
        }
        switch (state) {
            case SPEC:
                writer.write("?>" + content);
                state = PREFIX;
                break;
            case DOCTYPE:
                writer.write(">" + content);
                state = PREFIX;
                break;
            case OPEN:
                child.end();
                writer.write(content);
                state = FINISHED;
                break;
        }
        return this;
    }

    @Override
    public ContentWriter cdata(String content) {
        throw new IllegalStateException("Cannot write CDATA out of the root element.");
    }

    @Override
    public ContentWriter comment(String comment) {
        toContent();
        cdataWriter.write(config.prettyPrint + "<!-- " + comment + " -->");
        return this;
    }

    @Override
    public ContentWriter end() {
        throw new IllegalStateException("No open element to close.");
    }

    @Override
    public void close() {
        switch (state) {
            default: throw new IllegalStateException("No root element created.");
            case OPEN:
                child.end();
                state = FINISHED;
            case FINISHED: escapingWriter.close();
        }
    }

    @Override
    public ContentWriter get() {
        child = null;
        state = FINISHED;
        return this;
    }

    private final class ElementWriterImpl implements ElementWriter, Supplier<ContentWriter> {

        private final String tagPrefix;
        private final String prefix;
        private final String tag;
        private final Supplier<ContentWriter> parent;
        private ElementWriter child;
        private ElementState state = OPENING;

        private ElementWriterImpl(String prefix, String tag, Supplier<ContentWriter> parent) {
            this.tagPrefix = prefix;
            this.prefix = prefix + config.indent;
            this.tag = tag;
            this.parent = parent;
        }

        @Override public ElementWriter xmlns(String name) {
            return attribute("xmlns", name);
        }

        @Override public ElementWriter xmlns(String prefix, URI uri) {
            return attribute("xmlns:" + prefix, String.valueOf(uri));
        }

        @Override public ElementWriter attribute(String name, String value) {
            if(state == OPENING) {
                writer.write(config.attributeIndent + name + "=" + config.attrQuot);
                escapingWriter.write(value);
                writer.write(config.attrQuot);
                return this;
            }
            throw new IllegalStateException("Cannot write attribute " + name + "='" + value + "', when tag <" + tag + "> content started.");
        }

        @Override
        public ElementWriter flush() {
            writer.flush();
            return this;
        }

        @Override public ContentWriter instruction(String name, String content) {
            toContent();
            writer.write(prefix + "<?" + name + " " + content + "?>");
            return this;
        }

        @Override public ElementWriter tag(String tag) {
            toContent();
            writer.write(prefix + '<' + tag);
            return child = new ElementWriterImpl(prefix, tag, this);
        }

        @Override public ElementWriter tag(String nsPrefix, String tag) {
            return tag(nsPrefix + ':' + tag);
        }

        @Override public ContentWriter text(String content) {
            toContent();
            escapingWriter.write(prefix + content);
            return this;
        }

        @Override public ContentWriter cdata(String content) {
            switch (state) {
                case OPENING:
                    writer.write("/>" + prefix + "<![CDATA[");
                    state = CDATA;
                    break;
                case CONTENT:
                    closeChild();
                    writer.write(prefix + "<![CDATA[");
                    state = CDATA;
                    break;
                case CLOSED:
                    throw new IllegalStateException("Element " + tag + " already closed.");
            }
            cdataWriter.write(content);
            return this;
        }

        @Override
        public ContentWriter comment(String comment) {
            toContent();
            cdataWriter.write(prefix + "<!-- " + comment + " -->");
            return this;
        }

        @Override public ContentWriter end() {
            switch (state) {
                case OPENING:
                    writer.write("/>");
                    break;
                case CONTENT:
                    closeChild();
                    writer.write(tagPrefix + "</" + tag + '>');
                    break;
                case CDATA:
                    writer.write("]]>" + tagPrefix + "</" + tag + '>');
                    break;
                case CLOSED:
                    throw new IllegalStateException("Element " + tag + " already closed.");
            }
            state = CLOSED;
            return parent.get();
        }

        @Override public void close() {
            end().close();
        }

        private void closeChild() {
            if(nonNull(child)) child.end();
        }

        private void toContent() {
            switch (state) {
                case OPENING: writer.write(">"); break;
                case CDATA: writer.write("]]>"); break;
                case CONTENT: closeChild(); break;
                case CLOSED: throw new IllegalStateException("Element " + tag + " already closed.");
            }
            state = CONTENT;
        }

        @Override
        public ContentWriter get() {
            child = null;
            return this;
        }
    }

}
