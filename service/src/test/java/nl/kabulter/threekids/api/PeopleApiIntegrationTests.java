package nl.kabulter.threekids.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The real wiring of controller, application service and domain, without a running server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PeopleApiIntegrationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void aSinglePersonWithoutAFamilyDoesNotMatch() throws Exception {
        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": 42, "name": "Ada Lovelace", "birthDate": "1815-12-10" }
                        """))
                .andExpect(status().is(444))
                .andExpect(content().string(""));
    }
}
