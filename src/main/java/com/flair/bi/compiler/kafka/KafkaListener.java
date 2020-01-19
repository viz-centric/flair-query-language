package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

public class KafkaListener extends PostgresListener {

	private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final String QUERY__SHOW_TABLES_AND_STREAMS = "SHOW_TABLES_AND_STREAMS";

	private final Clock clock;

	public KafkaListener(Writer writer, Clock clock) {
        super(writer);
		this.clock = clock;
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
	protected String onFlairCastFunction(FQLParser.Func_call_exprContext func_call_expr) {
		StringBuilder str = new StringBuilder();
		String dataType = func_call_expr.getChild(2).getChild(0).getText();
		String fieldName = func_call_expr.getChild(2).getChild(2).getText();
		if (Arrays.asList("timestamp", "date", "datetime").contains(dataType.toLowerCase())) {
			str.append("STRINGTOTIMESTAMP(")
					.append(fieldName)
					.append(",")
					.append("'yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''")
					.append(")");
		} else {
			str.append("CAST(")
					.append(fieldName)
					.append(" as VARCHAR)");
		}
		return str.toString();
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
}
