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

import java.net.URI;

/**
 * Writer of any element within the XML hierarchy, except the root element.
 */
public interface ElementWriter extends ContentWriter {

    /**
     * Write XML namespace of the current tag.
     * @param uri Namespace URI.
     * @return Writer of other tag attributes.
     */
    ElementWriter xmlns(String uri);

    /**
     * Write XML namespace of the current tag.
     * @param prefix Namespace prefix.
     * @param uri Namespace URI.
     * @return Writer of other tag attributes.
     */
    ElementWriter xmlns(String prefix, URI uri);

    /**
     * Write XML namespace of the current tag.
     * @param prefix Namespace prefix.
     * @param uri Namespace URI.
     * @return Writer of other tag attributes.
     */
    default ElementWriter xmlns(String prefix, String uri) {
        return xmlns(prefix, URI.create(uri));
    }

    /**
     * Write tag attribute.
     * @param name Attribute name.
     * @param value Attribute value.
     * @return Writer of other tag attributes.
     */
    ElementWriter attribute(String name, String value);

    /**
     * Flush the content using underlying writer.
     * @return this
     */
    ElementWriter flush();

}
