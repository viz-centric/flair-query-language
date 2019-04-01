package com.flair.bi.compiler.oracle;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParser.ExprContext;
import com.flair.bi.grammar.FQLParser.Function_nameContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Writer;
import java.util.Optional;

public class OracleListener extends SQLListener {
    public OracleListener(Writer writer) {
        super(writer);
    }

	@Override
	public void exitFunction_name(Function_nameContext ctx) {
		 if (ctx.getText().equals("rand")) {
	            property.put(ctx, "dbms_random.value");
		 }
		 else {
	            property.put(ctx, ctx.getText());
	     }
	}

	@Override
	public void exitLimit_expr(FQLParser.Limit_exprContext ctx) {
		StringBuilder sb = new StringBuilder();

		if (ctx.expr().size() > 1) {
			sb.append("OFFSET ")
				.append(ctx.expr(1).getText())
				.append(" ROWS ");
		}
		sb.append("FETCH NEXT ")
				.append(ctx.expr(0).getText())
				.append(" ROWS ONLY");

		property.put(ctx, sb.toString());
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
        if(Optional.ofNullable(ctx.func_call_expr()).isPresent() && ("year".equalsIgnoreCase(ctx.func_call_expr().start.getText())||
        		"month".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "DAY".equalsIgnoreCase(ctx.func_call_expr().start.getText()) ||
        		"HOUR".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
        	str.append("EXTRACT(")       	
        	.append(ctx.func_call_expr().start.getText())
        	.append(" FROM ");
        	if(ctx.func_call_expr().getChild(2).getText().contains("-") || ctx.func_call_expr().getChild(2).getText().contains("/")) {
        		str.append("TO_DATE( ")
        		.append(ctx.func_call_expr().getChild(2).getText()+")");
        		
        	}
        	else {
        	str.append(ctx.func_call_expr().getChild(2).getText());
        	}
        	str.append(")");
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent() && ("week".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "QUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())
        		|| "YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText()) || "YEARWEEK".equalsIgnoreCase(ctx.func_call_expr().start.getText())
        		|| "YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) 
        {
        	str.append("to_char(");
        	if(ctx.func_call_expr().getChild(2).getText().contains("-") || ctx.func_call_expr().getChild(2).getText().contains("/")) {
        		str.append("TO_DATE( ")
        		.append(ctx.func_call_expr().getChild(2).getText()+")");
        		
        	}else {
        	str.append(ctx.func_call_expr().getChild(2).getText());
        	}
        	
        	if("week".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        		str.append(",'WW')");
        	}
        	else if("QUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        		str.append(",'Q')");
        	}
        	else if("YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        		str.append(",'YYYY-MM')");
        	}
        	else if("YEARWEEK".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        		str.append(",'YYYY-WW')");
        	}
        	else if("YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        		str.append(",'YYYY-Q')");
        	}
        	
        	
        	
        }
        else {
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
                .map(TerminalNode::getText)
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

        property.put(ctx, str.toString());
	}

	@Override
	public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT table_name FROM all_tables");

		if (ctx.describe_stmt_like() != null) {
			sb.append(" WHERE table_name LIKE ")
					.append(ctx.describe_stmt_like().expr().getText());
		}

		if (ctx.describe_stmt_limit() != null) {
			sb.append(" FETCH NEXT ")
					.append(ctx.describe_stmt_limit().expr().getText())
					.append(" ROWS ONLY");
        }

		property.put(ctx, sb.toString());
	}
}
