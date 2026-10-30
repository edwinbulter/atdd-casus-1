package nl.kabulter.threekids.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import nl.kabulter.threekids.application.PeopleService;
import nl.kabulter.threekids.domain.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    ResponseEntity<List<PersonJson>> register(@RequestBody PersonJson request) {
        List<Person> matching = people.register(request.toPerson());
        if (matching.isEmpty()) {
            return ResponseEntity.status(NO_RESPONSE).build();
        }
        return ResponseEntity.ok(matching.stream().map(PersonJson::from).toList());
    }

    /** Deletes the people with these IDs and ignores them from now on (phase 2 of the casus). */
    @DeleteMapping("/api/v1/people")
    ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        people.delete(ids);
        return ResponseEntity.noContent().build();
    }

    /** A person record as the API receives and returns it; relations are references like {@code { "id": 7 }}. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record PersonJson(
            long id, String name, LocalDate birthDate,
            Reference parent1, Reference parent2, Reference partner, List<Reference> children) {

        static PersonJson from(Person person) {
            return new PersonJson(person.id(), person.name(), person.birthDate(),
                    Reference.to(person.parent1()), Reference.to(person.parent2()), Reference.to(person.partner()),
                    person.children().stream().map(Reference::new).toList());
        }

        Person toPerson() {
            return new Person(id, name, birthDate, Reference.idOf(parent1), Reference.idOf(parent2),
                    Reference.idOf(partner), children == null ? List.of() : children.stream().map(Reference::id).toList());
        }
    }

    record Reference(long id) {

        static Reference to(Long id) {
            return id == null ? null : new Reference(id);
        }

        static Long idOf(Reference reference) {
            return reference == null ? null : reference.id();
        }
    }
}
