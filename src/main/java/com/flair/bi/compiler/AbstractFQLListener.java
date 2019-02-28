package com.flair.bi.compiler;

import com.flair.bi.grammar.FQLParserBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.io.Writer;

public abstract class AbstractFQLListener extends FQLParserBaseListener {

    protected final Writer writer;

    protected ParseTreeProperty<String> property = new ParseTreeProperty<>();

    public AbstractFQLListener(Writer writer) {
        this.writer = writer;
    }
}
