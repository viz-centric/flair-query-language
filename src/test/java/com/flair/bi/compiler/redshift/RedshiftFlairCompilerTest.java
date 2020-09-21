package com.flair.bi.compiler.redshift;

import com.flair.bi.compiler.AbstractSqlCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;
import org.junit.Test;

public class RedshiftFlairCompilerTest extends AbstractSqlCompilerUnitTest<RedshiftFlairCompiler> {

	@Override
	protected RedshiftFlairCompiler configureCompiler() {
		return new RedshiftFlairCompiler();
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
		stmtTest("show tables",
				"SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') UNION ALL SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog')");
	}

	@Test
	public void showTablesLike() throws CompilationException {
		stmtTest("show tables like '%para%'",
				"SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%') UNION ALL SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%')");
	}

	@Test
	public void showTablesLimit() throws CompilationException {
		stmtTest("show tables limit 4",
				"SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') UNION ALL SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog') LIMIT 4");
	}

	@Test
	public void showTablesLikeLimit() throws CompilationException {
		stmtTest("show tables like '%para%' limit 5",
				"SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%') UNION ALL SELECT CONCAT(TABLE_SCHEMA, CONCAT('.', TABLE_NAME)) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%') LIMIT 5");
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
	public void parseFlairIntervalOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN GETDATE() AND DATEADD(hour, -4, GETDATE()) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairIntervalAndCastOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(__FLAIR_CAST(bigint, product_id), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN GETDATE() AND DATEADD(hour, -4, CAST(product_id as bigint)) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairTypeCastLike() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(__FLAIR_CAST(flair_string, product_id)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(CAST(product_id as TEXT)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereInLongExpression() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (__FLAIR_CAST(timestamp, '2019-11-03 22:00:00.000000'), 1231) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (to_timestamp('2019-11-03 22:00:00.000000','YYYY-MM-DD HH24:MI:SS.US'),1231) GROUP BY customer_city");
	}

	@Test
	public void parseWhereInOneCondition() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN ( __FLAIR_CAST(timestamp, 121) ) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (to_timestamp(121,'YYYY-MM-DD HH24:MI:SS.US')) GROUP BY customer_city");
	}

	@Test
	public void parseFlairCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = __FLAIR_CAST(bigint, product_id)",
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = CAST(product_id as bigint)");
	}

	@Test
	public void selectHavingWithInnerSelect() throws CompilationException {
		stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > (SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') AND NOW()) LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > ( SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN DATEADD(hour, -4, GETDATE()) AND GETDATE()) LIMIT 20");
	}

	@Test
	public void dateFormatDateTime() throws CompilationException {
		stmtTest("select date_time(order_date) from transactions where price = 500",
				"select to_char(order_date::timestamp, 'DD-Mon-YYYY HH24:MI') from transactions where price = 500");
	}

	@Test
	public void dateFormatTime() throws CompilationException {
		stmtTest("select time(order_date) from transactions where price = 500",
				"select to_char(order_date::timestamp, 'HH24:MI') from transactions where price = 500");
	}

	@Test
	public void flairTruncWithTimestamp() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, timestamp, 'second') from transactions where price = 500 and __FLAIR_TRUNC(udpated_on, timestamp, 'second') > 0",
				"select date_trunc('second', inserted_on) from transactions where price = 500 and date_trunc('second', udpated_on) > 0");
	}

	@Test
	public void flairFlairNow() throws CompilationException {
		stmtTest("select __FLAIR_NOW(), __FLAIR_NOW('day') from transactions where price = 500 and __FLAIR_NOW('day', CUSTOM_NOW()) > 0",
				"select GETDATE(), date_trunc('day', GETDATE()) from transactions where price = 500 and date_trunc('day', CUSTOM_NOW()) > 0");
	}

	@Test
	public void flairTruncWithVarchar() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, varchar, 'second') from transactions where price = 500 and __FLAIR_TRUNC(udpated_on, int, 'second') > 0",
				"select inserted_on from transactions where price = 500 and udpated_on > 0");
	}

	@Test
	public void flairRaw() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM __FLAIR_RAW([[select * from myql_tables mt left join mysql_schema ms on ms.id = mt.request_id where mt.col = 123 order by limit 11]]) as subquery WHERE column1 = 123",
				"SELECT updated_on as updated_on FROM (select * from myql_tables mt left join mysql_schema ms on ms.id = mt.request_id where mt.col = 123 order by limit 11) as subquery WHERE column1 = 123");
	}
}
