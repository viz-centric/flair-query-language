package com.flair.bi.compiler.cockroachdb;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Arrays;

public class CockroachdbListener extends PostgresListener {

	public CockroachdbListener(Writer writer) {
		super(writer);
	}

	@Override
	protected String onFlairCastFunction(FQLParser.Func_call_exprContext func_call_expr) {
		StringBuilder str = new StringBuilder();
		String dataType = func_call_expr.getChild(2).getChild(0).getText();
		String fieldName = func_call_expr.getChild(2).getChild(2).getText();
		if (Arrays.asList("timestamp", "date", "datetime").contains(dataType.toLowerCase())) {
			str.append("timestamptz ")
					.append(fieldName);
		} else {
			str.append("CAST(")
					.append(fieldName)
					.append(" as TEXT)");
		}
		return str.toString();
	}
}
