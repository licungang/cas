package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentMongoDbConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link MongoDbConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentMongoDbConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.consent.mongo.host=localhost",
        "cas.consent.mongo.port=27017",
        "cas.consent.mongo.userId=root",
        "cas.consent.mongo.password=secret",
        "cas.consent.mongo.authenticationDatabaseName=admin",
        "cas.consent.mongo.dropCollection=true",
        "cas.consent.mongo.databaseName=consent"
    })
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
public class MongoDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
