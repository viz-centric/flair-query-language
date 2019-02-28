package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;

public class KafkaListener extends PostgresListener {

	public static final String QUERY__SHOW_TABLES_AND_STREAMS = "SHOW_TABLES_AND_STREAMS";

	public KafkaListener(Writer writer) {
		super(writer);
	}

	@Override
	public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append(QUERY__SHOW_TABLES_AND_STREAMS);
		property.put(ctx, sb.toString());
	}

}
