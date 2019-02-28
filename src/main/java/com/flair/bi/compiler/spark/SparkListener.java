package com.flair.bi.compiler.spark;

import com.flair.bi.compiler.SQLListener;

import java.io.Writer;

public class SparkListener extends SQLListener {
    public SparkListener(Writer writer) {
        super(writer);
    }
}
