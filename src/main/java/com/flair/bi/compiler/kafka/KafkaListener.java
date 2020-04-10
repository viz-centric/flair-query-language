package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KafkaListener extends PostgresListener {

	private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final String QUERY__SHOW_TABLES_AND_STREAMS = "SHOW_TABLES_AND_STREAMS";

	private final Clock clock;

	public KafkaListener(Writer writer, Clock clock) {
        super(writer);
		this.clock = clock;

		CAST_MAP.put("timestamp",
				(field1) -> new StringBuilder()
						.append("STRINGTOTIMESTAMP(")
						.append(field1.getFieldName())
						.append(",")
						.append("'yyyy-MM-dd HH:mm:ss'")
						.append(")"));
		CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
		CAST_MAP.put("date", CAST_MAP.get("timestamp"));

		CAST_MAP.put("flair_string",
				(field) -> new StringBuilder()
						.append("CAST(")
						.append(field.getFieldName())
						.append(" as VARCHAR)")
		);
	}

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(QUERY__SHOW_TABLES_AND_STREAMS);
        property.put(ctx, sb.toString());
    }

    @Override
    protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
		String formatted = LocalDateTime.now(clock).format(ISO_DATE_TIME);
        return "STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss.SSS''Z''')";
    }

    @Override
	protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
		long millis = SqlTimeConverter.toMillis(hourOrDays, number);
		return "(" +
				expression +
				" " +
				operator +
				" " + millis +
				")";
	}

	@Override
	public void exitFunc_call_expr(FQLParser.Func_call_exprContext ctx) {
		Optional<String> funcName = getFunctionName(ctx);
		if (funcName.isPresent()) {
			if ("UPPER".equalsIgnoreCase(funcName.get())) {
				property.put(ctx, "UCASE(" + (ctx.comma_sep_expr() != null ? property.get(ctx.comma_sep_expr()) : "") + ")");
				return;
			}
		}
		super.exitFunc_call_expr(ctx);
	}

	@Override
	public void exitOrder_expr(FQLParser.Order_exprContext ctx) {
		property.put(ctx, "");
	}

	@Override
	public void exitLimit_expr(FQLParser.Limit_exprContext ctx) {
		StringBuilder str = new StringBuilder();

		str.append(ctx.K_LIMIT().getText())
				.append(" ")
				.append(property.get(ctx.expr(0)));

		property.put(ctx, str.toString());
	}

	@Override
	public void exitSelect_core(FQLParser.Select_coreContext ctx) {
		StringBuilder str = new StringBuilder();

		if (ctx.K_SELECT() != null) {
			List<String> colList = ctx.result_column().stream()
					.map(i -> {
						if (i.column_alias() != null) {
							return i.column_alias().getText();
						}
						if (i.expr() != null) {
							return i.expr().getText();
						}
						return i.getText();
					})
					.collect(Collectors.toList());

			parseResults.put(ParseResult.SELECT_COLUMNS, colList);

			str.append(ctx.K_SELECT().getText())
					.append(" ")
					.append(ctx.K_ALL() == null ? "" : ctx.K_ALL().getText() + " ")
					.append(ctx.result_column().stream()
							.map(property::get).collect(Collectors.joining(", ")));

			if (ctx.K_FROM() != null) {
				str.append(" ")
						.append(ctx.K_FROM().getText())
						.append(" ");

				if (ctx.table_or_subquery().isEmpty()) {
					str.append(property.get(ctx.join_clause()));
				} else {
					str.append(ctx.table_or_subquery().stream()
							.map(property::get).collect(Collectors.joining(", ")));
				}

			}

			if (ctx.K_WHERE() != null) {
				str.append(" ")
						.append(ctx.K_WHERE().getText())
						.append(" ")
						.append(property.get(ctx.expr(0)));
			}


			if (ctx.K_GROUP() != null) {
				str.append(" ")
						.append(ctx.K_GROUP().getText()).append(" ")
						.append(ctx.K_BY().getText())
						.append(" ")
						.append(property.get(ctx.comma_sep_expr()));

				if (ctx.K_HAVING() != null) {
					str.append(" ")
							.append(ctx.K_HAVING().getText())
							.append(" ")
							.append(ctx.expr().size() == 2 ? property.get(ctx.expr(1)) : property.get(ctx.expr(0)));
				}

			}

		} else {
			str.append(property.get(ctx.values_expr()));
		}

		property.put(ctx, str.toString());
	}
}
