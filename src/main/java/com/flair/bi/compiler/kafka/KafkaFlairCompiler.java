package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;

public class KafkaFlairCompiler extends AbstractFlairCompiler {
    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new KafkaListener(writer);
    }
}
