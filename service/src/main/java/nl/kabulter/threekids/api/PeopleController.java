package nl.kabulter.threekids.api;

import java.time.LocalDate;
import nl.kabulter.threekids.application.PeopleService;
import nl.kabulter.threekids.domain.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PeopleController {

    /** The casus answers "nobody matches yet" with HTTP 444 No Response, which is not in {@code HttpStatus}. */
    private static final int NO_RESPONSE = 444;

    private final PeopleService people;

    PeopleController(PeopleService people) {
        this.people = people;
    }

    @PostMapping("/api/v1/people")
    ResponseEntity<Void> register(@RequestBody PersonRequest request) {
        people.register(request.toPerson());
        return ResponseEntity.status(NO_RESPONSE).build();
    }

    record PersonRequest(long id, String name, LocalDate birthDate) {

        Person toPerson() {
            return new Person(id, name, birthDate);
        }
    }
}
