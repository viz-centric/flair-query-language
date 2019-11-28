package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.postgres.PostgresListener;

import java.io.Writer;
import java.util.Objects;

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
		if (Objects.equals(letter, "hours")) {
			return "hour";
		} else if (Objects.equals(letter, "days")) {
			return "day";
		} else if (Objects.equals(letter, "months")) {
			return "month";
		}
		return letter;
	}
}
