package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class AthenaFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new AthenaListener(writer);
    }
}
