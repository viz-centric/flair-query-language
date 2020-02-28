package com.flair.bi.compiler.cockroachdb;

import com.flair.bi.compiler.postgres.PostgresListener;

import java.io.Writer;

public class CockroachdbListener extends PostgresListener {

	public CockroachdbListener(Writer writer) {
		super(writer);

		CAST_MAP.put("timestamp",
				(field1) -> new StringBuilder()
						.append("timestamptz ")
						.append(field1.getFieldName()));
		CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
		CAST_MAP.put("date", CAST_MAP.get("timestamp"));
	}

}
