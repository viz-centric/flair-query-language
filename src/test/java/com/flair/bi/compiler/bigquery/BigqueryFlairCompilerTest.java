package com.flair.bi.compiler.bigquery;

import com.flair.bi.compiler.AbstractCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;
import org.junit.Test;

/**
 * Unit tests for {@link BigqueryFlairCompiler}
 */
public class BigqueryFlairCompilerTest extends AbstractCompilerUnitTest<BigqueryFlairCompiler> {

	@Override
	protected BigqueryFlairCompiler configureCompiler() {
		return new BigqueryFlairCompiler();
	}

	@Test
	public void testSimpleSelectStmt() throws CompilationException {
		stmtTest("Select data from `bigquery-public-data.chicago_taxi_trips.transactions`");
	}

	@Test
	public void testSelectWithDatabaseStmt() throws CompilationException {
		stmtTest("Select data from `bigquery-public-data.chicago_taxi_trips.transactions`");
	}

	@Test
	public void testSelectStmtGroupBy() throws CompilationException {
		stmtTest("Select data, sum(price) as price from `bigquery-public-data.chicago_taxi_trips.transactions` group by data");
	}

	@Test
	public void testSelectStmtGroupByOrderBy1() throws CompilationException {
		stmtTest("Select data, sum(price) as price from `bigquery-public-data.chicago_taxi_trips.transactions` group by data order by price asc");
	}

	@Test
	public void testSelectStmtGroupByOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from `bigquery-public-data.chicago_taxi_trips.transactions` group by data order by price desc");
	}

	@Test
	public void testSelectStmtOrderBy1() throws CompilationException {
		stmtTest("Select data, sum(price) as price from `bigquery-public-data.chicago_taxi_trips.transactions` order by price asc");
	}

	@Test
	public void testSelectStmtOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from `bigquery-public-data.chicago_taxi_trips.transactions` order by price desc");
	}

	@Test
	public void multipleTestCaseSimple() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions`; select data from `bigquery-public-data.chicago_taxi_trips.transactions`");
	}

	@Test
	public void multipleTestCaseComplex() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` group by data order by price asc; select data where price between 10 and 200");
	}

	@Test
	public void whereTestCase1() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where data in ('a','b')");
	}

	@Test
	public void whereTestCase2() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where price between 100 and 200");
	}

	@Test
	public void whereTestCase3() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where price between 100 and 200 and data in ('1','2')");
	}

	@Test
	public void whereTestCase4() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where price between 100 and 200 or data in ('1','2')");
	}

	@Test
	public void whereTestCase5() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500");
	}

	@Test
	public void whereTestCase6() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where price != 500");
	}

	@Test
	public void whereTestCase7() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where data not in ('1','2')");
	}

	@Test
	public void whereTestCase8() throws CompilationException {
		stmtTest("select * from `bigquery-public-data.chicago_taxi_trips.transactions` where data like '%pera%'");
	}

	@Test
	public void randTest() throws CompilationException {
		stmtTest("select sum(price * rand()) as price from `bigquery-public-data.chicago_taxi_trips.transactions` where data like '%pera%'",
				"select sum(price * random()) as price from `bigquery-public-data.chicago_taxi_trips.transactions` where data like '%pera%'");

		stmtTest("select sum(price * random()) as price from `bigquery-public-data.chicago_taxi_trips.transactions` where data like '%pera%'",
				"select sum(price * random()) as price from `bigquery-public-data.chicago_taxi_trips.transactions` where data like '%pera%'");
	}

	@Test
	public void selectWithQuotes() throws CompilationException {
		stmtTest("SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM `bigquery-public-data.chicago_taxi_trips.transactions`",
				"SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM `bigquery-public-data.chicago_taxi_trips.transactions`");
	}

	@Test
	public void selectHaving() throws CompilationException {
		stmtTest(
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING (COUNT(product_price) > 1000) LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING (COUNT(product_price) > 1000) LIMIT 20");
	}

	@Test
	public void selectHavingWithoutBrackets() throws CompilationException {
		stmtTest(
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING COUNT(product_price) > 1000 LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING COUNT(product_price) > 1000 LIMIT 20");
	}

	@Test
	public void selectHavingWithInnerSelect() throws CompilationException {
		stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING COUNT(product_price) > (SELECT avg(transaction_quantity) as avg_quantity FROM `bigquery-public-data.chicago_taxi_trips.order_summary` WHERE inserted_on BETWEEN __FLAIR_INTERVAL_OPERATION(__FLAIR_NOW('day'), '-', '4 hours') AND NOW()) LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM `bigquery-public-data.chicago_taxi_trips.transactions` GROUP BY product_name HAVING COUNT(product_price) > ( SELECT avg(transaction_quantity) as avg_quantity FROM `bigquery-public-data.chicago_taxi_trips.order_summary` WHERE inserted_on BETWEEN (date_trunc('day', NOW()) - interval '4 hours') AND NOW()) LIMIT 20");
	}

	@Test
	public void showTables() throws CompilationException {
		stmtTest("show tables",
				"SELECT CONCAT(table_catalog, '.', table_schema, '.', TABLE_NAME) FROM `bigquery-public-data.chicago_taxi_trips.INFORMATION_SCHEMA.TABLES` WHERE table_schema NOT IN ('information_schema', 'pg_catalog')");
	}

	@Test
	public void showTablesLike() throws CompilationException {
		stmtTest("show tables like '%para%'",
				"SELECT CONCAT(table_catalog, '.', table_schema, '.', TABLE_NAME) FROM `bigquery-public-data.chicago_taxi_trips.INFORMATION_SCHEMA.TABLES` WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%')");
	}

	@Test
	public void showTablesLimit() throws CompilationException {
		stmtTest("show tables limit 4",
				"SELECT CONCAT(table_catalog, '.', table_schema, '.', TABLE_NAME) FROM `bigquery-public-data.chicago_taxi_trips.INFORMATION_SCHEMA.TABLES` WHERE table_schema NOT IN ('information_schema', 'pg_catalog') LIMIT 4");
	}

	@Test
	public void showTablesLikeLimit() throws CompilationException {
		stmtTest("show tables like '%para%' limit 5",
				"SELECT CONCAT(table_catalog, '.', table_schema, '.', TABLE_NAME) FROM `bigquery-public-data.chicago_taxi_trips.INFORMATION_SCHEMA.TABLES` WHERE table_schema NOT IN ('information_schema', 'pg_catalog') AND UPPER(TABLE_NAME) LIKE UPPER('%para%') LIMIT 5");
	}

	@Test
	public void parseDateFunction() throws CompilationException {
		stmtTest("select datefmt(custom_field, 'yyyy-MM-dd') from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1",
				"select to_char(custom_field::timestamp, 'yyyy-MM-dd') from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1");
	}

	@Test
	public void dateFormatDateTime() throws CompilationException {
		stmtTest("select date_time(order_date) from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500",
				"select to_char(order_date::timestamp, 'DD-Mon-YYYY HH24:MI') from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500");
	}

	@Test
	public void dateFormatTime() throws CompilationException {
		stmtTest("select time(order_date) from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500",
				"select to_char(order_date::timestamp, 'HH24:MI') from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500");
	}

	@Test
	public void parseAggregationFunction() throws CompilationException {
		stmtTest("select column1, count(column2) from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1",
				"select column1, count(column2) from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1");
	}

	@Test
	public void parseDistinctCountFunction() throws CompilationException {
		stmtTest("select column1, distinct_count(column2) from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1",
				"select column1, count(distinct column2) from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1");
	}

	@Test
	public void parseLimitAndOffset() throws CompilationException {
		stmtTest("select column1 from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1 limit 10 offset 53",
				"select column1 from `bigquery-public-data.chicago_taxi_trips.transactions` where a = 1 limit 10 offset 53");
	}

	@Test
	public void parseFlairTypeCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on >= __FLAIR_CAST(timestamp, '2019-11-03 22:00:00.000000') and __FLAIR_CAST(double, 20) > 0 GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on >= to_timestamp('2019-11-03 22:00:00.000000','YYYY-MM-DD HH24:MI:SS.US') and CAST(20 as double precision) > 0 GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairTypeCastLike() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE UPPER(__FLAIR_CAST(flair_string, product_id)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE UPPER(CAST(product_id as TEXT)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairIntervalOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on BETWEEN NOW() AND (NOW() - interval '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairIntervalAndCastOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(__FLAIR_CAST(timestamp,'2019-11-03 22:00:00.000000'), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE updated_on BETWEEN NOW() AND (to_timestamp('2019-11-03 22:00:00.000000','YYYY-MM-DD HH24:MI:SS.US') - interval '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereInLongExpression() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN (__FLAIR_CAST(timestamp, '2019-11-03 22:00:00.000000'), 1231) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN (to_timestamp('2019-11-03 22:00:00.000000','YYYY-MM-DD HH24:MI:SS.US'),1231) GROUP BY customer_city");
	}

	@Test
	public void parseWhereInOneCondition() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN ( __FLAIR_CAST(timestamp, 121) ) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE product_id IN (to_timestamp(121,'YYYY-MM-DD HH24:MI:SS.US')) GROUP BY customer_city");
	}

	@Test
	public void parseFlairCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE column1 = __FLAIR_CAST(bigint, product_id)",
				"SELECT updated_on as updated_on FROM `bigquery-public-data.chicago_taxi_trips.transactions` WHERE column1 = CAST(product_id as bigint)");
	}

	@Test
	public void flairFlairNow() throws CompilationException {
		stmtTest("select __FLAIR_NOW(), __FLAIR_NOW('day') from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and __FLAIR_NOW('day', CUSTOM_NOW()) > 0",
				"select NOW(), date_trunc('day', NOW()) from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and date_trunc('day', CUSTOM_NOW()) > 0");
	}

	@Test
	public void flairTruncWithTimestamp() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, timestamp, 'second') from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and __FLAIR_TRUNC(udpated_on, timestamp, 'second') > 0",
				"select date_trunc('second', inserted_on) from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and date_trunc('second', udpated_on) > 0");
	}

	@Test
	public void flairTruncWithVarchar() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, varchar, 'second') from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and __FLAIR_TRUNC(udpated_on, int, 'second') > 0",
				"select inserted_on from `bigquery-public-data.chicago_taxi_trips.transactions` where price = 500 and udpated_on > 0");
	}

	@Test
	public void flairRaw() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM __FLAIR_RAW([[select * from myql_tables mt left join mysql_schema ms on ms.id = mt.request_id where mt.col = 123 order by limit 11]]) as subquery WHERE column1 = 123",
				"SELECT updated_on as updated_on FROM (select * from myql_tables mt left join mysql_schema ms on ms.id = mt.request_id where mt.col = 123 order by limit 11) as subquery WHERE column1 = 123");
	}
}
