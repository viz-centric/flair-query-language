package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;

public class RedshiftListener extends PostgresListener {
	public RedshiftListener(Writer writer) {
		super(writer);
	}

	@Override
	protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
		return "GETDATE(" + (ctx.comma_sep_expr() != null ? ctx.comma_sep_expr().getText() : "") + ")";
	}

	@Override
	protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
		return "DATEADD(" +
				hourOrDays +
				", " +
				operator +
				number +
				", " +
				expression +
				")";
	}

	@Override
	protected String getHourOrDaysFromLetter(String letter, String number) {
		return SqlTimeConverter.toSingular(letter);
	}

	@Override
	protected String onDateTruncate(String finalFieldName, String timeUnit) {
		// second, day
		return "date_trunc(" + timeUnit + ", " + finalFieldName + ")";
	}
}
