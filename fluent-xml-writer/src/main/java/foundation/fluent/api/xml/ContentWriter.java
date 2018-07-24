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

import java.io.Writer;
import java.util.function.Consumer;

/**
 * Writer of the XML tag content.
 */
public interface ContentWriter {

    /**
     * Write XML processing instruction.
     * @param name Processing instruction name (e.g. xml-stylesheet).
     * @param content Content of the processing instruction tag.
     * @return Writer to continue writing additional content after the processing instruction.
     */
    ContentWriter instruction(String name, String content);

    /**
     * Start writing opening tag with provided tag name.
     * @param name Tag name.
     * @return Writer of element content including it's attributes.
     */
    ElementWriter tag(String name);

    /**
     * Start writing opening tag with provided XML namespace prefix and tag name.
     * @param nsPrefix XML namespace prefix.
     * @param name Tag name.
     * @return Writer of element content including it's attributes.
     */
    ElementWriter tag(String nsPrefix, String name);

    /**
     * Write text content of the currently opened tag.
     * @param content Text content to be written.
     * @return Writer to continue writing additional content after the processing instruction.
     */
    ContentWriter text(String content);

    /**
     * Write CDATA content of the currently opened tag.
     * @param content CDATA content to be written.
     * @return Writer to continue writing additional content after the processing instruction.
     */
    ContentWriter cdata(String content);

    ContentWriter fragment(Consumer<Writer> consumer);

    /**
     * End currently opened tag.
     * @return Parent content writer.
     */
    ContentWriter end();

    /**
     * Close the XML document.
     *  1. Close all opened tags
     *  2. Close underlying output stream.
     */
    void close();

}
