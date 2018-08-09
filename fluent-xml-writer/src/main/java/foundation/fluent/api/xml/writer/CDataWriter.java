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

package foundation.fluent.api.xml.writer;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

public class CDataWriter extends FilterWriter {

    public static final IntUnaryOperator INVALID_CHARACTER_MAPPING = c -> c < 9 ? ' ' : c;

    private final IntPredicate invalid = c -> c < 32;
    private final IntUnaryOperator operator;

    /**
     * Create a new filtered writer.
     *
     * @param out a Writer object to provide the underlying stream.
     * @param operator Operator to transform invalid characters.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public CDataWriter(Writer out, IntUnaryOperator operator) {
        super(out);
        this.operator = operator;
    }

    public CDataWriter(Writer out) {
        this(out, INVALID_CHARACTER_MAPPING);
    }

    @Override
    public void write(int c) throws IOException {
        super.write(operator.applyAsInt(c));
    }

    private void writeCbuf(char[] cbuf, int off, int len) throws IOException {
        int end = off + len;
        for(int i = off; i < end; i++) {
            if(invalid.test(cbuf[i])) {
                cbuf[i] = (char) operator.applyAsInt(cbuf[i]);
            }
        }
        out.write(cbuf, off, len);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writeCbuf(Arrays.copyOf(cbuf, cbuf.length), off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        writeCbuf(str.toCharArray(), off, len);
    }

}
