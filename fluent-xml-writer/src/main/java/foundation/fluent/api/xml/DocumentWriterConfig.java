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

import java.util.Arrays;

public final class DocumentWriterConfig {

    public final char attrQuot;
    public final String prettyPrint;
    public final String indent;
    public final String attributeIndent;

    private DocumentWriterConfig(char attrQuot, String prettyPrint, String indent, String attributeIndent) {
        this.attrQuot = attrQuot;
        this.prettyPrint = prettyPrint;
        this.indent = indent;
        this.attributeIndent = attributeIndent;
    }

    public static DocumentWriterConfig config() {
        return new DocumentWriterConfig('"', "", "", " ");
    }

    public DocumentWriterConfig quot(char attrQuot) {
        return new DocumentWriterConfig(attrQuot, prettyPrint, indent, attributeIndent);
    }

    public DocumentWriterConfig indentSpaces(int level) {
        char[] indent = new char[level];
        Arrays.fill(indent, ' ');
        return new DocumentWriterConfig(attrQuot, "\n", new String(indent), attributeIndent);
    }

    public DocumentWriterConfig indentTabs(int level) {
        char[] indent = new char[level];
        Arrays.fill(indent, '\t');
        return new DocumentWriterConfig(attrQuot, "\n", new String(indent), attributeIndent);
    }

    public DocumentWriterConfig indentAttribute(String attributeIndent) {
        return new DocumentWriterConfig(attrQuot, prettyPrint, indent, attributeIndent);
    }

}
