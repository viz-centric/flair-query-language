package com.flair.bi.compiler.oracle;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class OracleFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new OracleListener(writer);
    }
}
