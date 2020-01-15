package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;

public class KafkaListener extends PostgresListener {

    public static final String QUERY__SHOW_TABLES_AND_STREAMS = "SHOW_TABLES_AND_STREAMS";

    public KafkaListener(Writer writer) {
        super(writer);
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(QUERY__SHOW_TABLES_AND_STREAMS);
        property.put(ctx, sb.toString());
    }

    @Override
    protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
        return "current_time(" + (ctx.comma_sep_expr() != null ? ctx.comma_sep_expr().getText() : "") + ")";
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
					.append("'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"'")
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
}
