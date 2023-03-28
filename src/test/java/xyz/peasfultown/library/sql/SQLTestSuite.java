package xyz.peasfultown.library.sql;

import org.junit.platform.suite.api.*;

@Suite
@SuiteDisplayName("JUnit Platform Suite Demo")
//@SelectPackages("xyz.peasfultown.library.sql")
@SelectClasses({ConnectionTest.class})
@IncludeClassNamePatterns(".*Test")
public class SQLTestSuite {
}
