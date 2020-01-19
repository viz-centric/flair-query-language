package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.AbstractFlairCompiler;
import com.flair.bi.grammar.FQLParserListener;

import java.io.Writer;
import java.time.Clock;

public class KafkaFlairCompiler extends AbstractFlairCompiler {

    private final Clock clock;

    public KafkaFlairCompiler() {
        this(Clock.systemDefaultZone());
    }

    public KafkaFlairCompiler(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected FQLParserListener getListener(Writer writer) {
        return new KafkaListener(writer, clock);
    }
}
