package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Optional;

public class RedshiftListener extends PostgresListener {
	public RedshiftListener(Writer writer) {
		super(writer);
	}

	@Override
	protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
		String curTime = "GETDATE()";
		Optional<String> expr = Optional.ofNullable(ctx.comma_sep_expr())
				.map(comma -> comma.expr(0))
				.map(first -> first.getText());
		if (expr.isPresent()) {
			return onDateTruncate(curTime, expr.get());
		}
		return curTime;
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
