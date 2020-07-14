package com.flair.bi.compiler.oracle;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParser.ExprContext;
import com.flair.bi.grammar.FQLParser.Function_nameContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Writer;
import java.util.Optional;

public class OracleListener extends SQLListener {
    public OracleListener(Writer writer) {
        super(writer);

		CAST_MAP.put("timestamp",
				(field1) -> new StringBuilder()
						.append("to_timestamp(")
						.append(field1.getFieldName())
						.append(",")
						.append("'YYYY-MM-DD HH24:MI:SS.FF6'")
						.append(")"));
		CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
		CAST_MAP.put("date", CAST_MAP.get("timestamp"));

		CAST_MAP.put("flair_string",
				(field) -> new StringBuilder()
						.append("CAST(")
						.append(field.getFieldName())
						.append(" as varchar(256))")
		);

		CAST_MAP.put("nvarchar2",
				(field) -> new StringBuilder()
						.append("CAST(")
						.append(field.getFieldName())
						.append(" as ")
						.append(field.getDataType())
						.append("(256)")
						.append(")")
		);
		CAST_MAP.put("varchar2", CAST_MAP.get("nvarchar2"));
		CAST_MAP.put("varchar", CAST_MAP.get("nvarchar2"));
		CAST_MAP.put("char", CAST_MAP.get("flair_string"));

    }

	@Override
	protected StringBuilder onRawQuery(FQLParser.Table_or_subqueryContext ctx, FQLParser.Raw_queryContext x) {
		StringBuilder str = new StringBuilder();
		str.append("(")
				.append(x.getText(), 2, x.getText().length() - 2)
				.append(")")
				.append(" ");

		Optional.ofNullable(property.get(ctx.table_alias()))
				.ifPresent(str::append);
		return str;
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
		if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
				&& ("now".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
			str.append(onFlairNowFunction(ctx.func_call_expr()));
		} else if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
				&& ("distinct_count".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
			str.append("count(distinct ")
					.append(ctx.func_call_expr().getChild(2).getChild(0).getText())
					.append(")");
        } else if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
				&& ("datefmt".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
			str.append("to_char(")
					.append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(", ")
					.append(ctx.func_call_expr().getChild(2).getChild(2).getText())
					.append(")");
		} else if(Optional.ofNullable(ctx.func_call_expr()).isPresent() && ("year".equalsIgnoreCase(ctx.func_call_expr().start.getText())||
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
		else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
				&& "DATE_TIME".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
			str.append("to_char(")
					.append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(", ")
					.append("'DD-MON-YYYY HH24:MI'")
					.append(")");
		}
		else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
				&& "TIME".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
			str.append("to_char(")
					.append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(", ")
					.append("'HH24:MI'")
					.append(")");
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

		Optional.ofNullable(ctx.expr_in_brackets())
				.map(item -> property.get(item.expr()))
				.ifPresent(expr_in_brackets ->
						str.append("(")
								.append(expr_in_brackets)
								.append(")")
				);

		if (str.length() == 0) {
			str.append(ctx.getText());
		}

        property.put(ctx, str.toString());
	}

    @Override
	public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT table_name FROM dba_tables");

		if (ctx.describe_stmt_like() != null) {
			sb.append(" WHERE upper(table_name) LIKE upper(")
					.append(ctx.describe_stmt_like().expr().getText())
					.append(")");
		}

		if (ctx.describe_stmt_limit() != null) {
			sb.append(" FETCH NEXT ")
					.append(ctx.describe_stmt_limit().expr().getText())
					.append(" ROWS ONLY");
        }

		property.put(ctx, sb.toString());
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

	@Override
	protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
		String curTime = "sysdate";

		if (ctx.comma_sep_expr() != null) {
			String strExpr;

			FQLParser.ExprContext expr = ctx.comma_sep_expr().expr(0);
			if (expr != null) {
				strExpr = property.get(expr) != null ? property.get(expr) : expr.getText();

				FQLParser.ExprContext expr2 = ctx.comma_sep_expr().expr(1);
				if (expr2 != null) {
					curTime = property.get(expr2) != null ? property.get(expr2) : expr2.getText();
				}

				return onDateTruncate(curTime, strExpr);
			}
		}
		return curTime;
	}

	@Override
	protected String getHourOrDaysFromLetter(String letter, String number) {
		return SqlTimeConverter.toSingular(letter) + "(" + number.length() + ")";
	}

	@Override
	protected String onDateTruncate(String finalFieldName, String timeUnit) {
    	// MI - minute, DD - day
		String oracleTimeUnit;
		switch (timeUnit) {
			case "'day'":
				oracleTimeUnit = "'DD'";
				break;
			case "'second'":
			case "'minute'":
			default:
				oracleTimeUnit = "'MI'";
		}
		return "TRUNC(" + finalFieldName + ", " + oracleTimeUnit + ")";
	}
}
