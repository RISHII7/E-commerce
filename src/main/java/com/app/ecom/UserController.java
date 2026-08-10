package com.app.ecom;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The web entry point for everything to do with users.
 *
 * <h2>What this class is responsible for</h2>
 *
 * <p>Only the web side of things: take an incoming HTTP request, ask
 * {@link UserService} to do the actual work, and hand back whatever comes out.
 * There is no logic here and no data stored here — deliberately. If you find
 * yourself wanting to add either, it almost certainly belongs in the service
 * instead.
 *
 * <p>Keeping the controller this thin has a practical payoff: when the database
 * eventually replaces the in-memory list, not one line of this file changes.
 *
 * <h2>What {@code @RestController} does</h2>
 *
 * <p>Two things. It tells Spring to create one of these at startup and route
 * matching web requests to it, and it says that whatever a method returns is the
 * response body itself rather than the name of a page to render. Spring converts
 * that return value automatically, which is why no method below mentions JSON.
 *
 * <h2>How it gets hold of the service</h2>
 *
 * <p>{@code @RequiredArgsConstructor} is a Lombok annotation that writes a
 * constructor taking every {@code final} field — here, just {@link UserService}.
 * Spring sees a constructor with one argument it recognises and supplies the
 * service automatically.
 *
 * <p>This is worth doing on purpose rather than reaching for {@code @Autowired}
 * on the field. Because the field is {@code final} and set in the constructor,
 * it can never be reassigned or left null, and a plain unit test can pass in its
 * own {@code UserService} with {@code new UserController(service)} — no Spring
 * involved at all.
 *
 * <h2>Known rough edges</h2>
 *
 * <ul>
 *   <li><strong>The two endpoints disagree about their content type.</strong>
 *       {@code GET} replies with JSON; {@code POST} replies with plain text.
 *       See {@link #createUser} for why that is awkward.</li>
 *   <li><strong>The path is written out twice.</strong> A single
 *       {@code @RequestMapping("/api/users")} on the class would state it once
 *       and let each method describe only what it adds.</li>
 *   <li><strong>Nothing survives a restart</strong>, because the service holds
 *       users in memory. That limitation lives in {@link UserService}, not
 *       here.</li>
 * </ul>
 *
 * <p>All of these are tracked as follow-up issues.
 *
 * @see UserService where the users and the rules actually live
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    /**
     * The service that owns the users and does the real work.
     *
     * <p>Supplied by Spring through the constructor Lombok generates. It is
     * {@code final} so it is set exactly once, at construction, and can never be
     * swapped out afterwards.
     */
    private final UserService userService;

    /**
     * Returns every user currently held.
     *
     * <p>Answers {@code GET /api/users} with a JSON array.
     *
     * <p>When there are no users this returns {@code 200 OK} with {@code []},
     * not a {@code 404}. That is a deliberate decision: "show me all users" is a
     * perfectly valid question when the answer happens to be none. Treating it as
     * an error would force every caller to write a special case for a situation
     * that is not actually exceptional.
     *
     * @return all users, in the order they were added
     */
    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        return userService.fetchAllUsers();
    }

    /**
     * Adds a new user.
     *
     * <p>Answers {@code POST /api/users}. {@code @RequestBody} tells Spring to
     * read the JSON out of the request and build a {@link User} from it, matching
     * the JSON field names to the field names on the class.
     *
     * <h3>What this currently replies, and why it needs revisiting</h3>
     *
     * <p>Returning a bare {@code String} means Spring sends it back as
     * {@code text/plain}, not JSON. So {@code GET} on this same path answers with
     * {@code application/json} while {@code POST} answers with plain text — a
     * caller cannot simply parse every response from this API the same way.
     *
     * <p>The sentence itself is also not much use to a program. It confirms that
     * something worked, but it does not say <em>which</em> user was created or
     * where to find it, so a client that needs the new id has no way to get it
     * short of fetching the whole collection again and guessing.
     *
     * <p>That gap became sharper once ids started being assigned by the server.
     * {@link UserService#addUser} now decides the id and writes it onto the user,
     * so at the moment this method returns, the exact value the caller needs is
     * sitting right there — and is thrown away in favour of a fixed sentence.
     *
     * <p>The usual convention is {@code 201 Created}, with the newly created user
     * as the body and a {@code Location} header pointing at it. That is worth
     * changing sooner rather than later: once anything is consuming this endpoint,
     * altering the response shape becomes a breaking change.
     *
     * <h3>What the caller controls, and what they do not</h3>
     *
     * <p>Any {@code id} sent in the request body is <strong>ignored</strong>. The
     * service overwrites it with the next value in its own sequence, so clients
     * cannot choose their own ids and cannot collide with an existing one.
     *
     * <p>The names, on the other hand, are not checked at all. A request with no
     * first name, no last name, or empty strings for both is accepted and stored
     * exactly as sent.
     *
     * @param user the user to add, read from the JSON request body
     * @return a fixed confirmation sentence, sent as {@code text/plain}
     */
    @PostMapping("/api/users")
    public String createUser(@RequestBody User user) {
        userService.addUser(user);
        return "User Added Successfully";
    }
}
