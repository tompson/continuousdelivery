package documentation.company

import at.martinahrer.cd.Application
import at.martinahrer.cd.CompanyRepository
import at.martinahrer.cd.model.Company
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentation
import org.springframework.restdocs.constraints.ConstraintDescriptions
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.StringUtils
import org.springframework.web.context.WebApplicationContext

import javax.servlet.RequestDispatcher

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*
import static org.springframework.restdocs.payload.PayloadDocumentation.*
import static org.springframework.restdocs.snippet.Attributes.key
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by martin on 20/03/16.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application)
@WebAppConfiguration
class GeneralApiDocumentation {

    Closure<String> VALUE_WRITER = { def content -> this.objectMapper.writeValueAsString(content) }

    @Rule
    public RestDocumentation restDocumentation = new RestDocumentation("build/generated-snippets/general");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private RestDocumentationResultHandler document;

    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;


    @Before
    public void setUp() {
        this.document = document("{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(this.document)
                .build();

        return
    }

    @Test
    public void headersExample() throws Exception {
        this.document.snippets(responseHeaders(
                headerWithName("Content-Type").description("The Content-Type of the payload, e.g. `application/hal+json`")));

        this.mockMvc.perform(get("/api/"))
                .andExpect(status().isOk());
    }

    @Test
    public void errorExample() throws Exception {
        this.document.snippets(responseFields(
                fieldWithPath("error").description("The HTTP error that occurred, e.g. `Bad Request`"),
                fieldWithPath("message").description("A description of the cause of the error"),
                fieldWithPath("path").description("The path to which the request was made"),
                fieldWithPath("status").description("The HTTP status code, e.g. `400`"),
                fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred")));

        this.mockMvc
                .perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI,
                "/api/companies")
                .requestAttr(RequestDispatcher.ERROR_MESSAGE,
                "The company 'http://localhost:8080/api/companies/123' does not exist"))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("error", is("Bad Request")))
                .andExpect(MockMvcResultMatchers.jsonPath("timestamp", is(notNullValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("status", is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("path", is(notNullValue())));
    }

    @Test
    public void indexExample() throws Exception {
        this.document.snippets(
                links(
                        linkWithRel("companies").description("The <<resources-companies,Companies resource>>"),
                        linkWithRel("profile").description("The <<resources-profile,Profile resource>>")),
                responseFields(
                        fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")));

        this.mockMvc.perform(get("/api/"))
                .andExpect(status().isOk());
    }
}
