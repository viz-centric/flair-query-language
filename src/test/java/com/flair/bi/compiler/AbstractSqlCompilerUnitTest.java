package com.flair.bi.compiler;

import org.junit.Test;

import com.project.bi.exceptions.CompilationException;
import com.project.bi.query.FlairCompiler;

public abstract class AbstractSqlCompilerUnitTest<T extends FlairCompiler> extends AbstractCompilerUnitTest<T> {

	protected static final String SIMPLE_SELECT_STMT = "Select data from transactions";

	/**
	 * Test of {@link AbstractSqlCompilerUnitTest#SIMPLE_SELECT_STMT}
	 * 
	 * @throws CompilationException
	 */
	@Test
	public void simpleSelectStatementTest() throws CompilationException {
		stmtTest(SIMPLE_SELECT_STMT, expectedSimpleSelectStatementResult());
	}

	protected String expectedSimpleSelectStatementResult() {
		return "Select data from transactions";
	}

}
