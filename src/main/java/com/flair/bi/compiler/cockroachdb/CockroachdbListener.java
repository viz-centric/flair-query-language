package com.flair.bi.compiler.cockroachdb;

import com.flair.bi.compiler.postgres.PostgresListener;

import java.io.Writer;

public class CockroachdbListener extends PostgresListener {

	public CockroachdbListener(Writer writer) {
		super(writer);
	}

}
