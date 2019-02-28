package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.postgres.PostgresListener;

import java.io.Writer;

public class RedshiftListener extends PostgresListener {
	public RedshiftListener(Writer writer) {
		super(writer);
	}
}
