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
		String curTime = "GETDATE()";

		if (ctx.comma_sep_expr() != null) {
			String strExpr;

			FQLParser.ExprContext expr = ctx.comma_sep_expr().expr(0);
			if (expr != null) {
				strExpr = property.get(expr) != null ? property.get(expr) : expr.getText();

				FQLParser.ExprContext expr2 = ctx.comma_sep_expr().expr(1);
				if (expr2 != null) {
					curTime = property.get(expr2) != null ? property.get(expr2) : expr2.getText();
				}

				return onDateTruncate(curTime, strExpr);
			}
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
