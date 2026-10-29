package nl.kabulter.threekids.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import nl.kabulter.threekids.application.PeopleService;
import nl.kabulter.threekids.domain.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PeopleController.class)
class PeopleControllerTests {

    private static final String ADA = """
            { "id": 42, "name": "Ada Lovelace", "birthDate": "1815-12-10" }
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PeopleService people;

    @Test
    void registersThePersonFromTheRequest() throws Exception {
        given(people.register(any())).willReturn(List.of());

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(ADA));

        verify(people).register(new Person(42, "Ada Lovelace", LocalDate.of(1815, 12, 10)));
    }

    @Test
    void answers444WithoutABodyWhenNobodyMatches() throws Exception {
        given(people.register(any())).willReturn(List.of());

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(ADA))
                .andExpect(status().is(444))
                .andExpect(content().string(""));
    }
}
