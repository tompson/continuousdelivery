package test

import at.martinahrer.cd.Application
import at.martinahrer.cd.model.Company
import groovy.json.JsonBuilder
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import spock.lang.Narrative
import spock.lang.Specification

/**
 * Test that requires a Servlet Container running at <code>http://${test.server.host}:${test.server.port}</code>.
 */

@ContextConfiguration(classes = IntegrationTestApplication)
@Narrative("As a customer relations manager I want to add a company")
class CompanyApiTest extends Specification {

    @Value('${test.server.host}')
    String host = "localhost"

    @Value('${test.server.port}')
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    def "posting a valid entity"() {
        given:
        JsonBuilder builder = new JsonBuilder()
        builder {
            name 'ACME'
        }
        // better done with rest-assured
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        def request = new HttpEntity<String>(builder.toPrettyString(), headers)

        when:
        def response=restTemplate.postForEntity("http://$host:$port/api/companies", request, Object)

        then:
        response.statusCode == HttpStatus.CREATED
    }
}

