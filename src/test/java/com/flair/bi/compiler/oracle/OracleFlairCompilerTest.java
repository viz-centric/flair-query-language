package com.flair.bi.compiler.oracle;

import com.flair.bi.compiler.AbstractSqlCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;
import org.junit.Test;

public class OracleFlairCompilerTest extends AbstractSqlCompilerUnitTest<OracleFlairCompiler> {

	@Override
	protected OracleFlairCompiler configureCompiler() {
		return new OracleFlairCompiler();
	}

	@Test
	public void showTables() throws CompilationException {
		stmtTest("show tables", "SELECT table_name FROM dba_tables");
	}

	@Test
	public void showTablesLike() throws CompilationException {
		stmtTest("show tables like '%pera%'",
				"SELECT table_name FROM dba_tables WHERE upper(table_name) LIKE upper('%pera%')");
	}

	@Test
	public void showTablesLimit() throws CompilationException {
		stmtTest("show tables limit 4", "SELECT table_name FROM dba_tables FETCH NEXT 4 ROWS ONLY");
	}

	@Test
	public void selectWithLimit() throws CompilationException {
		stmtTest("SELECT * FROM ORDERS LIMIT 1", "SELECT * FROM ORDERS FETCH NEXT 1 ROWS ONLY");
	}

	@Test
	public void selectWithLimitAndOffset() throws CompilationException {
		stmtTest("SELECT * FROM ORDERS LIMIT 1 OFFSET 5", "SELECT * FROM ORDERS OFFSET 5 ROWS FETCH NEXT 1 ROWS ONLY");
	}

	@Test
	public void showTablesLikeLimit() throws CompilationException {
		stmtTest("show tables like '%pera%' limit 4",
				"SELECT table_name FROM dba_tables WHERE upper(table_name) LIKE upper('%pera%') FETCH NEXT 4 ROWS ONLY");
	}

	@Test
	public void parseDateFunction() throws CompilationException {
		stmtTest("select datefmt(custom_field, 'yyyy-MM-dd') from my_table where a = 1",
				"select to_char(custom_field, 'yyyy-MM-dd') from my_table where a = 1");
	}

	@Test
	public void parseAggregationFunction() throws CompilationException {
		stmtTest("select column1, count(column2) from my_table where a = 1",
				"select column1, count(column2) from my_table where a = 1");
	}

	@Test
	public void parseDistinctCountFunction() throws CompilationException {
		stmtTest("select column1, distinct_count(column2) from my_table where a = 1",
				"select column1, count(distinct column2) from my_table where a = 1");
	}

	@Test
	public void parseNowFunction() throws CompilationException {
		stmtTest("select column1, now() from my_table where a = 1",
				"select column1, sysdate from my_table where a = 1");
	}

	@Test
	public void parseLimitAndOffset() throws CompilationException {
		stmtTest("select column1 from my_table where a = 1 limit 10 offset 53",
				"select column1 from my_table where a = 1 OFFSET 53 ROWS FETCH NEXT 10 ROWS ONLY");
	}

	@Test
	public void parseFlairTypeCastLike() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(__FLAIR_CAST(bigint, product_id)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(CAST(product_id as CHAR)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC");
	}

	@Test
	public void parseFlairIntervalOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN sysdate AND (sysdate - interval '4' hour) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY");
	}

	@Test
	public void parseFlairIntervalAndCastOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(__FLAIR_CAST(timestamp,'2019-11-03T22:00:00.000Z'), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN sysdate AND (to_timestamp('2019-11-03T22:00:00.000Z','YYYY-MM-DD\"T\"HH24:MI:SS.ff3\"Z\"') - interval '4' hour) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY");
	}
}
