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
import java.util.HashMap;
import java.util.Map;

public class EscapingWriter extends FilterWriter {

    private final Map<Integer, String> entities;

    /**
     * Create a new filtered writer.
     *
     * @param out a Writer object to provide the underlying stream.
     * @param entities
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public EscapingWriter(Writer out, Map<Integer, String> entities) {
        super(out);
        this.entities = entities;
    }

    public EscapingWriter(Writer out) {
        this(out, entities());
    }

    @Override
    public void write(int c) throws IOException {
        /*
        if(entities.containsKey(c)) {
            out.write(entities.get(c));
        } else {
            out.write(c);
        }*/
        switch (c) {
            case '<': out.write("&lt;"); break;
            case '>': out.write("&gt;"); break;
            case '&': out.write("&amp;"); break;
            case '"': out.write("&quot;"); break;
            case '\'': out.write("&apos;"); break;
            default: out.write(c);
        }
    }

    private int escape(char[] cbuf, int off, int end, String m) throws IOException {
        if(end > off) out.write(cbuf, off, end - off);
        out.write(m);
        return end + 1;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        int end = off + len;
        int s = off;
        for(int i = off; i < end; i++) {
            //int c = cbuf[i];

            switch (cbuf[i]) {
                case '<': s = escape(cbuf, s, i, "&lt;"); break;
                case '>': s = escape(cbuf, s, i, "&gt;"); break;
                case '&': s = escape(cbuf, s, i, "&amp;"); break;
                case '"': s = escape(cbuf, s, i, "&quot;"); break;
                case '\'': s = escape(cbuf, s, i, "&apos;"); break;
            }
            /*
            if(entities.containsKey(c)) {
                if(i > s) {
                    out.write(cbuf, s, i - s);
                }
                out.write(entities.get(c));
                s = i + 1;
            }*/
        }
        if(end > s) {
            out.write(cbuf, s, end - s);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }

    private static Map<Integer, String> entities() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put((int)'&', "&amp;");
        map.put((int)'<', "&lt;");
        map.put((int)'>', "&gt;");
        map.put((int)'"', "&quot;");
        map.put((int)'\'', "&apos;");
        return map;
    }
}
