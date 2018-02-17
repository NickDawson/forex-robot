package com.trading.forex.security

import com.trading.forex.entity.Role
import com.trading.forex.entity.User
import com.trading.forex.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

class AppUserDetailsServiceTest extends Specification {


    def "testLoadUserByUsernameOK"() {

        // init & inject Mocks
        def userRepository=Mock(UserRepository){
            findByUsername("user_1") >> User.builder().username("user_1").password("pwd").roles(Arrays.asList(new Role("TRADER","description"))).build()
        }
        AppUserDetailsService appUserDetailsService=new AppUserDetailsService(userRepository);

        given:
        def username = "user_1"
        when: 'load user detail  '
        def result = appUserDetailsService.loadUserByUsername(username)

        then: 'expect result'
        result.getPassword()=="pwd"
        result.getUsername()==username
        result.getAuthorities().size()==1
    }

    def "testLoadUserByUsernameKO"() {

        // init & inject Mocks
        def userRepository=Mock(UserRepository){
            findByUsername("user_2") >> User.builder().username("user_2").password("pwd").roles(Arrays.asList(new Role("TRADER","description"))).build()
        }
        AppUserDetailsService appUserDetailsService=new AppUserDetailsService(userRepository);

        given:
        def username = "user_1"
        when: 'load user detail  '
        def result = appUserDetailsService.loadUserByUsername(username)

        then: 'expect result'
        thrown(UsernameNotFoundException)
    }
}