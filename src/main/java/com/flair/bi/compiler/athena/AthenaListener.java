package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.mysql.MySQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
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

}
