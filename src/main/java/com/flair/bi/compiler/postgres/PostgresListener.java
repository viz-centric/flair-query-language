package com.flair.bi.compiler.postgres;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParser.ExprContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Writer;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostgresListener extends SQLListener {
    public PostgresListener(Writer writer) {
        super(writer);

        CAST_MAP.put("timestamp",
                (field1) -> new StringBuilder()
                        .append("to_timestamp(")
                        .append(field1.getFieldName())
                        .append(",")
                        .append("'YYYY-MM-DD HH24:MI:SS'")
                        .append(")"));
        CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
        CAST_MAP.put("date", CAST_MAP.get("timestamp"));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitFunction_name(FQLParser.Function_nameContext ctx) {
    	
        if (ctx.getText().equals("rand")) {
            property.put(ctx, "random");
        }
        else if(ctx.getText().equals("year")) {
        	property.put(ctx, "date_part");
        }		
        else {
            property.put(ctx, ctx.getText());
        }
    }

	@Override
	public void exitExpr(ExprContext ctx) {
		  StringBuilder str = new StringBuilder();
		  
		 //literal
        Optional.ofNullable(ctx.literal())
                .map(property::get)
                .ifPresent(str::append);

       
        //BIND_PARAMETER
        Optional.ofNullable(ctx.BIND_PARAMETER())
                .ifPresent(str::append);
        
        //unary operator
        Optional.ofNullable(ctx.unary_operator())
                .map(property::get)
                .ifPresent(x -> str
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(0))));
        
        //binary operator
        Optional.ofNullable(ctx.binary_operator())
                .map(property::get)
                .ifPresent(x -> str
                        .append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(1))));
                        

        //func_call_expr
        if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && ("distinct_count".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            str.append("count(distinct ")
                    .append(ctx.func_call_expr().getChild(2).getChild(0).getText())
                    .append(")");
        } else if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && ("datefmt".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            str.append("to_char(")
                    .append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append("::timestamp, ")
                    .append(ctx.func_call_expr().getChild(2).getChild(2).getText())
                    .append(")");
        } else if (Optional.ofNullable(ctx.func_call_expr()).isPresent() && ("year".equalsIgnoreCase(ctx.func_call_expr().start.getText()) ||
                "week".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "month".equalsIgnoreCase(ctx.func_call_expr().start.getText()) ||
                "quarter".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "DAY".equalsIgnoreCase(ctx.func_call_expr().start.getText()) ||
                "HOUR".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {

            str.append("date_part(")
                    .append("'" + ctx.func_call_expr().start.getText() + "',");
            if (ctx.func_call_expr().getChild(2).getText().contains(",")) {
                str.append("TO_DATE(")
                        .append(ctx.func_call_expr().getChild(2).getText())
                        .append(")");
            } else {
                str.append(ctx.func_call_expr().getChild(2).getText());
            }
            str.append("::timestamp)");
        } else if (Optional.ofNullable(ctx.func_call_expr()).isPresent() && ("YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText())
                || "YEARWEEK".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            str.append("to_char(");
            if (ctx.func_call_expr().getChild(2).getText().contains(",")) {
                str.append("TO_DATE( ")
                        .append(ctx.func_call_expr().getChild(2).getText() + ")");

            } else {
                str.append(ctx.func_call_expr().getChild(2).getText());
            }

            if ("YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
                str.append(",'YYYY-MM')");
            } else if ("YEARWEEK".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
                str.append(",'YYYY-WW')");
            } else if ("YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
                str.append(",'YYYY-Q')");
            }

        } else {
            Optional.ofNullable(ctx.func_call_expr())
                    .map(property::get)
                    .ifPresent(str::append);
        }
        
      
       
//        expr K_NOT? K_BETWEEN expr K_AND expr
        Optional.ofNullable(ctx.K_BETWEEN())
                .map(TerminalNode::getText)
                .ifPresent(x -> str.append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                        .append(ctx.K_BETWEEN().getText()).append(" ")
                        .append(property.get(ctx.expr(1)))
                        .append(" ").append(ctx.K_AND().getText()).append(" ")
                        .append(property.get(ctx.expr(2))));
        //expr K_NOT? K_IN ( '(' ( select_stmt
        //                          | expr ( ',' expr )*
        //                          )?
        //                      ')'
        //        | ( database_name '.' )? table_name )
        Optional.ofNullable(ctx.K_IN())
                .map(terminalNode -> {
                    return terminalNode.getText();
                })
                .ifPresent(x -> {
                    str.append(property.get(ctx.expr(0)))
                            .append(" ")
                            .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                            .append(x).append(" ");

                    String tableName = property.get(ctx.table_name());

                    if (tableName == null) {
                        str.append("(");
                        Optional.ofNullable(ctx.select_stmt())
                                .map(property::get)
                                .ifPresent(str::append);

                        Optional.ofNullable(ctx.comma_sep_expr())
                                .map(property::get)
                                .ifPresent(str::append);

                        str.append(")");

                    } else {
                        Optional.ofNullable(ctx.database_name())
                                .map(property::get)
                                .ifPresent(y ->
                                        str.append(y)
                                                .append(".")
                                );

                        str.append(tableName);
                    }

                });

//        ( K_NOT )? K_EXISTS )? '(' select_stmt ')'
        if (ctx.getText().contains("(") && ctx.select_stmt() != null) {
            Optional.ofNullable(ctx.select_stmt())
                    .map(property::get)
                    .ifPresent(x -> {
                        Optional.ofNullable(ctx.K_NOT())
                                .map(TerminalNode::getText)
                                .ifPresent(y -> str.append(y).append(" "));

                        Optional.ofNullable(ctx.K_EXISTS())
                                .map(TerminalNode::getText)
                                .ifPresent(y -> str.append(y).append(" "));

                        str.append("(")
                                .append(x)
                                .append(")");
                    });
        }

        //expr K_NOT? str_match_expr expr ( K_ESCAPE expr )?
        Optional.ofNullable(ctx.str_match_expr())
                .map(property::get)
                .ifPresent(x -> str.append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(1)))
                        .append(" ")
                        .append(ctx.K_ESCAPE() == null ? "" :
                                ctx.K_ESCAPE().getText() + " " + property.get(ctx.expr(2))));


        Optional.ofNullable(ctx.db_tbl_col_expr())
                .map(property::get)
                .ifPresent(str::append);

        Optional.ofNullable(ctx.expr_in_brackets())
                .map(item -> property.get(item.expr()))
                .ifPresent(expr_in_brackets -> {
                    str.append("(")
                        .append(expr_in_brackets)
                        .append(")");
                });

        if (str.length() == 0) {
            str.append(ctx.getText());
        }

        property.put(ctx, str.toString());
	}

    @Override
    public void exitComma_sep_expr(FQLParser.Comma_sep_exprContext ctx) {
        String collect = ctx.expr().stream().map(node -> {
            String s = property.get(node);
            return s;
        })
                .collect(Collectors.joining(","));
        property.put(ctx, collect);
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tablename FROM pg_catalog.pg_tables");

        if (ctx.describe_stmt_like() != null) {
            sb.append(" WHERE tablename LIKE ")
                    .append(ctx.describe_stmt_like().expr().getText());
        }

        if (ctx.describe_stmt_limit() != null) {
            sb.append(" LIMIT ")
                    .append(ctx.describe_stmt_limit().expr().getText());
        }

        property.put(ctx, sb.toString());
    }

}
