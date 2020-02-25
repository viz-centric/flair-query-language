package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.mysql.MySQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Arrays;

public class AthenaListener extends MySQLListener {

    public AthenaListener(Writer writer) {
        super(writer);
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOW TABLES");

        if (ctx.describe_stmt_like() != null) {
            sb.append(" ").append(ctx.describe_stmt_like().expr().getText().replaceAll("%", "*"));
        }

        property.put(ctx, sb.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitLimit_expr(FQLParser.Limit_exprContext ctx) {
        StringBuilder str = new StringBuilder();

        str
                .append(ctx.K_LIMIT().getText())
                .append(" ")
                .append(property.get(ctx.expr(0)));

        property.put(ctx, str.toString());
    }

    @Override
    protected String onFlairCastFunction(FQLParser.Func_call_exprContext func_call_expr) {
        StringBuilder str = new StringBuilder();
        String dataType = func_call_expr.getChild(2).getChild(0).getText();
        String fieldName = func_call_expr.getChild(2).getChild(2).getText();
        if (Arrays.asList("timestamp", "date", "datetime").contains(dataType.toLowerCase())) {
            str.append("parse_datetime(")
                    .append(fieldName)
                    .append(",")
                    .append("'yyyy-MM-dd''T''HH:mm:ss.SSS''Z'")
                    .append(")");
        } else if ("flair_string".equalsIgnoreCase(dataType)) {
            str.append("CAST(")
                    .append(fieldName)
                    .append(" as VARCHAR)");
        } else {
            str.append("CAST(")
                    .append(fieldName)
                    .append(" as ")
                    .append(dataType)
                    .append(")");
        }
        return str.toString();
    }

    @Override
    protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
        return "(" +
                expression +
                " " +
                operator +
                " " + "interval '" + number + "' " + hourOrDays +
                ")";
    }

}
