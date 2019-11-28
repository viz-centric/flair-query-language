package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;

import java.io.Writer;

public class RedshiftListener extends PostgresListener {
	public RedshiftListener(Writer writer) {
		super(writer);
	}

	@Override
	protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
		if ("NOW()".equals(expression)) {
			expression = "GETDATE()";
		}
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
	protected String getHourOrDaysFromLetter(String letter) {
		return SqlTimeConverter.toSingular(letter);
	}
}
