package com.flair.bi.compiler.bigquery;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class BigqueryFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new BigqueryListener(writer);
    }
}
