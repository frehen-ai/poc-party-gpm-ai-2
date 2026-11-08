package no.frehen.pocpartygpm.test.context;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static no.frehen.pocpartygpm.constant.ProfileConstant.PROFILE_INTEGRATION_TEST;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Testcontainers
@Import({
        PostgresContainerConfiguration.class,
})
//@EnableMethodSecurity(securedEnabled = true, proxyTargetClass = true)
@SpringBootTest
@ActiveProfiles(PROFILE_INTEGRATION_TEST)
public @interface SpringBootIntegrationTest {
    @AliasFor(attribute = "properties", annotation = SpringBootTest.class)
    String[] properties() default {};
}
