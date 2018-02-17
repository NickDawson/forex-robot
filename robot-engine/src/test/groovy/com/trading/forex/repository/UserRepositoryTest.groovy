package com.trading.forex.repository

import com.oanda.v20.instrument.CandlestickGranularity
import com.trading.forex.RobotApp
import Symbol
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import spock.lang.Specification

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD

@SpringBootTest(
        classes = RobotApp.class)
@SqlGroup([
        @Sql(scripts = "classpath:sql/user-insert.sql", executionPhase = BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/user-rollback.sql", executionPhase = AFTER_TEST_METHOD)
])
class UserRepositoryTest extends Specification{

    @Autowired
    private UserRepository userRepository;

    def "GetUserDetails"() {

        given:
        def username = "user_1"
        when: 'Find user detail by username '
        def result=userRepository.findByUsername(username)

        then: 'expect result'
        result.getUsername()=="user_1"
        result.getPassword()=="5998343fa311f6ebd075f9b3abf532d43bdbbe3b07fbbbcc8c88cf49c915a6cd"
        noExceptionThrown()
    }

}