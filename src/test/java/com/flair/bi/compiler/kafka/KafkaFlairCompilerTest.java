package com.flair.bi.compiler.kafka;

import com.flair.bi.compiler.AbstractCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class KafkaFlairCompilerTest extends AbstractCompilerUnitTest<KafkaFlairCompiler> {

	public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private Instant now;
	private Clock clock;

	@Override
	protected KafkaFlairCompiler configureCompiler() {
		now = Instant.now();
		clock = Clock.fixed(now, ZoneId.systemDefault());
		return new KafkaFlairCompiler(clock);
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
		stmtTest("Select data, sum(price) as price from transactions group by data order by price asc",
				"Select data, sum(price) as price from transactions group by data ");
	}

	@Test
	public void testSelectStmtGroupByOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions group by data order by price desc",
				"Select data, sum(price) as price from transactions group by data ");
	}

	@Test
	public void testSelectStmtOrderBy1() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions order by price asc",
				"Select data, sum(price) as price from transactions ");
	}

	@Test
	public void testSelectStmtOrderBy2() throws CompilationException {
		stmtTest("Select data, sum(price) as price from transactions order by price desc",
				"Select data, sum(price) as price from transactions ");
	}

	@Test
	public void multipleTestCaseSimple() throws CompilationException {
		stmtTest("select * from transactions;select data from test");
	}

	@Test
	public void multipleTestCaseComplex() throws CompilationException {
		stmtTest("select * from transactions group by data order by price asc;select data where price between 10 and 200",
				"select * from transactions group by data ;select data where price between 10 and 200");
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
				"select column1 from my_table where a = 1 limit 10");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city  LIMIT 20");
	}

	@Test
	public void parseFlairTypeCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on >= __FLAIR_CAST(timestamp, '2019-11-03 22:00:00.000000') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on >= STRINGTOTIMESTAMP('2019-11-03 22:00:00.000000','yyyy-MM-dd HH:mm:ss.SSSSSS') GROUP BY updated_on  LIMIT 20");
	}

	@Test
	public void parseFlairTypeCastLike() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(__FLAIR_CAST(flair_string, product_id)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UCASE(CAST(product_id as VARCHAR)) LIKE UCASE('%123%') GROUP BY updated_on  LIMIT 20");
	}

	@Test
	public void parseFlairCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = __FLAIR_CAST(bigint, product_id)",
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = CAST(product_id as bigint)");
	}

	@Test
	public void parseFlairIntervalOperation() throws CompilationException {
		LocalDateTime now = LocalDateTime.now(clock);
		String formatted = now.format(ISO_LOCAL_DATE_TIME);
		int fourHours = 4 * 60 * 60 * 1000;
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss''Z''') AND (STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss''Z''') - " + fourHours + ") GROUP BY updated_on  LIMIT 20");
	}

	@Test
	public void parseFlairIntervalAndCastOperation() throws CompilationException {
		LocalDateTime now = LocalDateTime.now(clock);
		String formatted = now.format(ISO_LOCAL_DATE_TIME);
		int fourHours = 4 * 60 * 60 * 1000;
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN __FLAIR_NOW() AND __FLAIR_INTERVAL_OPERATION(__FLAIR_CAST(timestamp,'2019-11-03T22:00:00.000000'), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss''Z''') AND (STRINGTOTIMESTAMP('2019-11-03T22:00:00.000000','yyyy-MM-dd HH:mm:ss.SSSSSS') - " + fourHours + ") GROUP BY updated_on  LIMIT 20");
	}

	@Test
	public void parseDistinct() throws CompilationException {
		stmtTest("SELECT DISTINCT * FROM PAGEVIEWS_ORIGINAL LIMIT 10 OFFSET 0",
				"SELECT * FROM PAGEVIEWS_ORIGINAL LIMIT 10");
	}

	@Test
	public void parseWhereInLongExpression() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (__FLAIR_CAST(timestamp, '2019-11-03 22:00:00.000000'), 1231) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (STRINGTOTIMESTAMP('2019-11-03 22:00:00.000000','yyyy-MM-dd HH:mm:ss.SSSSSS'),1231) GROUP BY customer_city");
	}

	@Test
	public void parseWhereInOneCondition() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN ( __FLAIR_CAST(timestamp, 121) ) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (STRINGTOTIMESTAMP(121,'yyyy-MM-dd HH:mm:ss.SSSSSS')) GROUP BY customer_city");
	}

	@Test
	public void selectHavingWithInnerSelect() throws CompilationException {
		LocalDateTime now = LocalDateTime.now(clock);
		String formatted = now.format(ISO_LOCAL_DATE_TIME);
		int fourHours = 4 * 60 * 60 * 1000;
		stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > (SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') AND NOW()) LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > ( SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN (STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss''Z''') - " + fourHours + ") AND STRINGTOTIMESTAMP('" + formatted + "Z','yyyy-MM-dd''T''HH:mm:ss''Z''')) LIMIT 20");
	}

	@Test
	public void flairTruncWithTimestamp() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, timestamp, 'second') from transactions where price = 500 and __FLAIR_TRUNC(udpated_on, timestamp, 'second') > 0",
				"select inserted_on from transactions where price = 500 and udpated_on > 0");
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
