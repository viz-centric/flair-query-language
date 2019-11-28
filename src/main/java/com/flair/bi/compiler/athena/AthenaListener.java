package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.mysql.MySQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;

public class AthenaListener extends MySQLListener {

    public AthenaListener(Writer writer) {
        super(writer);
    }

    @Override
    public void exitExpr(FQLParser.ExprContext ctx) {
        StringBuilder sb = new StringBuilder();

        Optional<FQLParser.Binary_operatorContext> optional = Optional
                .ofNullable(ctx.binary_operator())
                .filter(x -> x.K_LIKE() != null);
        if (optional.isPresent()) {
            sb
                    .append(property.get(ctx.expr(0)))
                    .append(" ")
                    .append(optional.get().getText())
                    .append(" ")
                    .append(property.get(ctx.expr(1)).replaceAll("%", "*"));
            property.put(ctx, sb.toString());
            return;
        }

        super.exitExpr(ctx);
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

    protected String onFlairFunction(FQLParser.Func_call_exprContext func_call_expr) {
        StringBuilder str = new StringBuilder();
        String dataType = func_call_expr.getChild(2).getChild(0).getText();
        if (Arrays.asList("timestamp", "date", "datetime").contains(dataType.toLowerCase())) {
            String fieldName = func_call_expr.getChild(2).getChild(2).getText();
            str.append("parse_datetime(")
                    .append(fieldName)
                    .append(",")
                    .append("'yyyy-MM-dd''T''HH:mm:ss.SSS''Z'")
                    .append(")");
        } else {
            str.append(func_call_expr.getText());
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
