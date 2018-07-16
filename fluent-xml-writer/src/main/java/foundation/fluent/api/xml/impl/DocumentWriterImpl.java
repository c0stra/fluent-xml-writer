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
import foundation.fluent.api.xml.writer.EscapingWriter;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static foundation.fluent.api.xml.impl.DocumentWriterImpl.DocumentState.*;
import static foundation.fluent.api.xml.impl.DocumentWriterImpl.ElementState.*;
import static java.util.Objects.nonNull;

public final class DocumentWriterImpl implements DocumentWriter, Supplier<DocumentWriter> {

    enum DocumentState {EMPTY, XML, PREFIX, OPEN, FINISHED}
    enum ElementState {OPENING, CONTENT, CDATA, CLOSED}

    private final PrintWriter writer;
    private final PrintWriter escapingWriter;
    private final PrintWriter cdataWriter;
    private ElementWriter<DocumentWriter> child;
    private DocumentState state = EMPTY;

    public static DocumentWriter documentBuilder(Writer writer) {
        Writer cdataWriter = new EscapingWriter(writer);
        return new DocumentWriterImpl(new PrintWriter(writer), new PrintWriter(cdataWriter), new PrintWriter(new EscapingWriter(cdataWriter)));
    }

    private DocumentWriterImpl(PrintWriter writer, PrintWriter cdataWriter, PrintWriter escapingWriter) {
        this.writer = writer;
        this.cdataWriter = cdataWriter;
        this.escapingWriter = escapingWriter;
    }

    private DocumentWriter set(String name, String value) {
        switch (state) {
            case EMPTY:
                writer.write("<?xml " + name + "='" + value + "'");
                state = XML;
                break;
            case XML:
                writer.write(" " + name + "='" + value + "'");
                break;
            default:
                throw new IllegalStateException("XML spec must be first in the document.");
        }
        return this;
    }

    private void toContent() {
        switch (state) {
            case XML: writer.write("?>");
            case EMPTY: state = PREFIX;
            case PREFIX: return;
            default: throw new IllegalStateException("Trying to output second root.");
        }
    }

    @Override
    public DocumentWriter version(String version) {
        return set("version", version);
    }

    @Override
    public DocumentWriter encoding(String encoding) {
        return set("encoding", encoding);
    }

    @Override
    public DocumentWriter instruction(String name, String content) {
        toContent();
        return this;
    }

    @Override
    public ElementWriter<DocumentWriter> open(String tag) {
        toContent();
        state = OPEN;
        return child = new ElementWriterImpl<>(tag, this);
    }

    @Override
    public ElementWriter<DocumentWriter> open(String nsPrefix, String tag) {
        return open(nsPrefix + ':' + tag);
    }

    @Override
    public void finish() {
        switch (state) {
            default:
                throw new IllegalStateException("No root element created.");
            case OPEN:
                child.close();
            case FINISHED:
                escapingWriter.close();
        }
    }

    @Override
    public DocumentWriter get() {
        child = null;
        state = FINISHED;
        return this;
    }

    private final class ElementWriterImpl<P extends WriterBase> implements ElementWriter<P>, Supplier<ContentWriter<P>> {

        private final String tag;
        private final Supplier<P> parent;
        private ElementWriter<ContentWriter<P>> child;
        private ElementState state = OPENING;

        private ElementWriterImpl(String tag, Supplier<P> parent) {
            this.tag = tag;
            this.parent = parent;
            writer.write("<" + tag);
        }

        @Override public ElementWriter<P> xmlns(String name) {
            return attribute("xmlns", name);
        }

        @Override public ElementWriter<P> xmlns(String prefix, URI uri) {
            return attribute("xmlns:" + prefix, String.valueOf(uri));
        }

        @Override public ElementWriter<P> attribute(String name, String value) {
            writer.write(" " + name + "='");
            escapingWriter.write(value);
            writer.write('\'');
            return this;
        }

        @Override public ContentWriter<P> instruction(String name, String content) {
            toContent();
            return this;
        }

        @Override public ElementWriter<ContentWriter<P>> open(String tag) {
            toContent();
            return child = new ElementWriterImpl<>(tag, this);
        }

        @Override public ElementWriter<ContentWriter<P>> open(String nsPrefix, String tag) {
            return open(nsPrefix + ':' + tag);
        }

        @Override public ContentWriter<P> text(String content) {
            toContent();
            escapingWriter.write(content);
            return this;
        }

        @Override public ContentWriter<P> cdata(String content) {
            switch (state) {
                case OPENING:
                    writer.write("/><!CDATA[");
                    state = CDATA;
                    break;
                case CONTENT:
                    closeChild();
                    writer.write("<!CDATA[");
                    state = CDATA;
                    break;
                case CLOSED:
                    throw new IllegalStateException("Element " + tag + " already closed.");
            }
            cdataWriter.write(content);
            return this;
        }

        @Override public ContentWriter<P> fragment(Consumer<Writer> consumer) {
            return this;
        }

        @Override public P close() {
            switch (state) {
                case OPENING:
                    writer.write("/>");
                    break;
                case CONTENT:
                    closeChild();
                    writer.write("</" + tag + '>');
                    break;
                case CDATA:
                    writer.write("]></" + tag + '>');
                    break;
                case CLOSED:
                    throw new IllegalStateException("Element " + tag + " already closed.");
            }
            state = CLOSED;
            return parent.get();
        }

        @Override public void finish() {
            close().finish();
        }

        private void closeChild() {
            if(nonNull(child)) child.close();
        }

        private void toContent() {
            switch (state) {
                case OPENING: writer.write(">"); break;
                case CDATA: writer.write("]>"); break;
                case CONTENT: closeChild(); break;
                case CLOSED: throw new IllegalStateException("Element " + tag + " already closed.");
            }
            state = CONTENT;
        }

        @Override
        public ContentWriter<P> get() {
            child = null;
            return this;
        }
    }

}