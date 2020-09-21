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

	@Override
	public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') ");

		StringBuilder sbLike = new StringBuilder();
		if (ctx.describe_stmt_like() != null) {
			sbLike.append("AND UPPER(TABLE_NAME) LIKE UPPER(")
					.append(ctx.describe_stmt_like().expr().getText())
					.append(") ");
		}

		sb.append(sbLike)
				.append("UNION ALL SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog') ")
				.append(sbLike);

		if (ctx.describe_stmt_limit() != null) {
			sb.append("LIMIT ")
					.append(ctx.describe_stmt_limit().expr().getText());
		}

		property.put(ctx, sb.toString().trim());
	}
}
