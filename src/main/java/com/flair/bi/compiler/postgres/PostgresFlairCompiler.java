package com.flair.bi.compiler.postgres;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class PostgresFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new PostgresListener(writer);
    }
}
