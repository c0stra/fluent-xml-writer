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

/**
 * Configuration of the document writer.
 *
 * It allows one to configure the writer using fluent API, so you specify the config simply like:
 *
 * config().singleQuoteValue().indentSpaces(4)
 *
 * The config is immutable, so every method creates new config instance with immutable parameters.
 */
public final class DocumentWriterConfig {

    public final String attrQuot;
    public final String prettyPrint;
    public final String indent;
    public final String attributeIndent;

    private DocumentWriterConfig(String attrQuot, String prettyPrint, String indent, String attributeIndent) {
        this.attrQuot = attrQuot;
        this.prettyPrint = prettyPrint;
        this.indent = indent;
        this.attributeIndent = attributeIndent;
    }

    /**
     * Public factory method to create new config.
     * @return New DocumentWriterConfig instance.
     */
    public static DocumentWriterConfig config() {
        return new DocumentWriterConfig("\"", "", "", " ");
    }

    /**
     * Quote attribute values using single quote (apostrophe): `'`
     * @return Config with previous values and quoting character changed to apostrophe.
     */
    public DocumentWriterConfig singleQuoteValue() {
        return new DocumentWriterConfig("'", prettyPrint, indent, attributeIndent);
    }

    /**
     * Quote attribute values using double quote: `"`
     * This is the default for new config.
     * @return Config with previous values and quoting character changed to double quote.
     */
    public DocumentWriterConfig doubleQuoteValue() {
        return new DocumentWriterConfig("\"", prettyPrint, indent, attributeIndent);
    }

    /**
     * Set indentation to specified number of spaces.
     * It enables pretty printing, so every tag starts at new line (default behavior is not to do so).
     * Every tag or other content will also get indented by parent prefix + specified number of spaces.
     * @param level Number of spaces to be used to indent.
     * @return Config with previous values and pretty printing enabled with specified indentation level.
     */
    public DocumentWriterConfig indentSpaces(int level) {
        char[] indent = new char[level];
        Arrays.fill(indent, ' ');
        return new DocumentWriterConfig(attrQuot, "\n", new String(indent), attributeIndent);
    }

    /**
     * Set indentation to specified number of tabs.
     * It enables pretty printing, so every tag starts at new line (default behavior is not to do so).
     * Every tag or other content will also get indented by parent prefix + specified number of tabs.
     * @param level Number of tabs to be used to indent.
     * @return Config with previous values and pretty printing enabled with specified indentation level.
     */
    public DocumentWriterConfig indentTabs(int level) {
        char[] indent = new char[level];
        Arrays.fill(indent, '\t');
        return new DocumentWriterConfig(attrQuot, "\n", new String(indent), attributeIndent);
    }

    /**
     * Set indentation to tabs.
     * It enables pretty printing, so every tag starts at new line (default behavior is not to do so).
     * Every tag or other content will also get indented by parent prefix + additional tab.
     * @return Config with previous values and pretty printing enabled with specified indentation.
     */
    public DocumentWriterConfig indentTabs() {
        return indentTabs(1);
    }

    /**
     * Set indentation for attributes.
     * It's not yet working, and may change similarly to content indentation (only set level).
     * @param level Level of additional attribute indentation.
     * @return Config with previous values and attribute indentation set.
     */
    public DocumentWriterConfig indentAttribute(int level) {
        return new DocumentWriterConfig(attrQuot, prettyPrint, indent, attributeIndent);
    }

}
