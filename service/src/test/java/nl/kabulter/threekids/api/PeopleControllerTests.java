package nl.kabulter.threekids.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private static final String HOMER = """
            { "id": 1, "name": "Homer", "birthDate": "1956-05-12",
              "partner": { "id": 2 }, "children": [{ "id": 3 }, { "id": 4 }, { "id": 5 }] }
            """;

    private static final Person HOMER_RECORD =
            new Person(1, "Homer", LocalDate.of(1956, 5, 12), null, null, 2L, List.of(3L, 4L, 5L));
    private static final Person MARGE_RECORD =
            new Person(2, "Marge", LocalDate.of(1956, 10, 1), null, null, 1L, List.of(3L, 4L, 5L));

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

    @Test
    void registersThePersonWithItsRelations() throws Exception {
        given(people.register(any())).willReturn(List.of());

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(HOMER));

        verify(people).register(HOMER_RECORD);
    }

    @Test
    void answers200WithEverythingKnownAboutTheMatchingPeople() throws Exception {
        given(people.register(any())).willReturn(List.of(HOMER_RECORD, MARGE_RECORD));

        mvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(HOMER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Homer"))
                .andExpect(jsonPath("$[0].birthDate").value("1956-05-12"))
                .andExpect(jsonPath("$[0].parent1").doesNotExist())
                .andExpect(jsonPath("$[0].partner.id").value(2))
                .andExpect(jsonPath("$[0].children[*].id").value(contains(3, 4, 5)))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}
