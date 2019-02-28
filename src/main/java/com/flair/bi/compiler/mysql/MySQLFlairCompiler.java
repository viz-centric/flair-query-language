package com.flair.bi.compiler.mysql;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class MySQLFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new MySQLListener(writer);
    }
}
