package com.flair.bi.compiler.bigquery;

import com.flair.bi.compiler.postgres.PostgresListener;

import java.io.Writer;

public class BigqueryListener extends PostgresListener {

    public BigqueryListener(Writer writer) {
        super(writer);
    }
}
