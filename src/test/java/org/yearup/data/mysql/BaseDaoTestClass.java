package org.yearup.data.mysql; // Or wherever your BaseDaoTestClass resides

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Optional: To use a specific test profile
import org.springframework.test.context.jdbc.Sql; // Optional: To run SQL scripts for setup
import org.springframework.transaction.annotation.Transactional; // Optional: To roll back tests

import javax.sql.DataSource; // Import DataSource

// This annotation tells Spring to load your application context for testing
// Use the 'webEnvironment = SpringBootTest.WebEnvironment.NONE' if you don't need the full web server
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional // ✅ RECOMMENDED: Rolls back each test after it runs, ensuring clean state
@ActiveProfiles("test") // ✅ RECOMMENDED: Use a separate profile for test database config
// Optional: You can run an SQL script before each test class or method
// @Sql(scripts = "classpath:easyshop_test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_EACH_TEST_METHOD)
public abstract class BaseDaoTestClass { // Make it abstract as it's meant to be extended

    @Autowired // ✅ This is where DataSource is injected by Spring
    protected DataSource dataSource;


}
