package com.flair.bi.compiler.cockroachdb;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class CockroachdbFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new CockroachdbListener(writer);
    }
}
