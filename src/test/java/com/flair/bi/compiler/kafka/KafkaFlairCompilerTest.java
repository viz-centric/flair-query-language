package com.flair.bi.compiler.kafka;

import org.junit.Test;

import com.flair.bi.compiler.AbstractCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;

public class KafkaFlairCompilerTest extends AbstractCompilerUnitTest<KafkaFlairCompiler> {

	@Override
	protected KafkaFlairCompiler configureCompiler() {
		return new KafkaFlairCompiler();
	}

	@Test
	public void testSimpleSelectStmt() throws CompilationException {
		stmtTest("Select data from transactions");
	}

	@Test
	public void testSelectStmtGroupBy() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions group by data");
	}

	@Test
	public void testSelectStmtGroupByOrderBy1() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions group by data order by price asc");
	}

	@Test
	public void testSelectStmtGroupByOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions group by data order by price desc");
	}

	@Test
	public void testSelectStmtOrderBy1() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions order by price asc");
	}

	@Test
	public void testSelectStmtOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions order by price desc");
	}

	@Test
	public void multipleTestCaseSimple() throws CompilationException {
		stmtTest("select * from transactions;select data from test");
	}

	@Test
	public void multipleTestCaseComplex() throws CompilationException {
		stmtTest(
				"select * from transactions group by data order by price asc;select data where price between 10 and 200");
	}

	@Test
	public void whereTestCase1() throws CompilationException {
		stmtTest("select * from transactions where data in ('a','b')");
	}

	@Test
	public void whereTestCase2() throws CompilationException {
		stmtTest("select * from transactions where price between 100 and 200");
	}

	@Test
	public void whereTestCase3() throws CompilationException {
		stmtTest("select * from transactions where price between 100 and 200 and data in ('1','2')");
	}

	@Test
	public void whereTestCase4() throws CompilationException {
		stmtTest("select * from transactions where price between 100 and 200 or data in ('1','2')");
	}

	@Test
	public void whereTestCase5() throws CompilationException {
		stmtTest("select * from transactions where price = 500");
	}

	@Test
	public void whereTestCase6() throws CompilationException {
		stmtTest("select * from transactions where price != 500");
	}

	@Test
	public void whereTestCase7() throws CompilationException {
		stmtTest("select * from transactions where data not in ('1','2')");
	}

	@Test
	public void whereTestCase8() throws CompilationException {
		stmtTest("select * from transactions where data like '%pera%'");
	}

	@Test
	public void randTest() throws CompilationException {
		stmtTest("select sum(price * rand()) as price from transactions where data like '%pera%'",
				"select sum(price * random()) as price from transactions where data like '%pera%'");

		stmtTest("select sum(price * random()) as price from transactions where data like '%pera%'",
				"select sum(price * random()) as price from transactions where data like '%pera%'");
	}

	@Test
	public void showTables() throws CompilationException {
		stmtTest("show tables", "SHOW_TABLES_AND_STREAMS");
	}

	@Test
	public void showTablesLike() throws CompilationException {
		stmtTest("show tables like '%pera%'", "SHOW_TABLES_AND_STREAMS");
	}

	@Test
	public void showTablesLimit() throws CompilationException {
		stmtTest("show tables limit 4", "SHOW_TABLES_AND_STREAMS");
	}

	@Test
	public void showTablesLikeLimit() throws CompilationException {
		stmtTest("show tables like '%pera%' limit 5", "SHOW_TABLES_AND_STREAMS");
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
	public void parseLimitAndOffset() throws CompilationException {
		stmtTest("select column1 from my_table where a = 1 limit 10 offset 53",
				"select column1 from my_table where a = 1 limit 10 offset 53");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0");
	}

}
