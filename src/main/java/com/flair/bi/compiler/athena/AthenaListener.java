package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;

public class AthenaListener extends PostgresListener {

	public AthenaListener(Writer writer) {
		super(writer);
	}

	@Override
	public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("SHOW TABLES");

		if (ctx.describe_stmt_like() != null) {
			sb.append(" ").append(ctx.describe_stmt_like().expr().getText().replaceAll("%", "*"));
		}

		property.put(ctx, sb.toString());
	}

}
