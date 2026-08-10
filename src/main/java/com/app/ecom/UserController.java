package com.app.ecom;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 *   <li><strong>A missing user is reported as success.</strong> Asking for an id
 *       that does not exist answers {@code 200 OK} with an empty body instead of
 *       {@code 404}. See {@link #getUser} — this is the most misleading thing in
 *       the class.</li>
 *   <li><strong>The endpoints disagree about their content type.</strong> Both
 *       {@code GET}s reply with JSON; {@code POST} replies with plain text. See
 *       {@link #createUser} for why that is awkward.</li>
 *   <li><strong>The base path is written out three times.</strong> A single
 *       {@code @RequestMapping("/api/users")} on the class would state it once
 *       and let each method describe only what it adds. Three copies is the
 *       point at which one of them eventually gets edited alone.</li>
 *   <li><strong>Nothing survives a restart</strong>, because the service holds
 *       users in memory. That limitation lives in {@link UserService}, not
 *       here.</li>
 * </ul>
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
     * Returns a single user by id.
     *
     * <p>Answers {@code GET /api/users/{id}}. The {@code {id}} in the path is a
     * placeholder, and {@code @PathVariable} tells Spring to pull that piece of
     * the URL out and hand it over as the method argument. Requesting
     * {@code /api/users/3} therefore arrives here with {@code id} set to 3.
     *
     * <p>Spring converts the text from the URL into a {@link Long} on the way in,
     * which comes with a useful side effect for free: a request for
     * {@code /api/users/abc} never reaches this method at all. Spring cannot make
     * a number out of {@code abc}, so it answers {@code 400 Bad Request} on its
     * own. Verified against a running instance.
     *
     * <h3>The part that needs fixing: a missing user looks like a success</h3>
     *
     * <p>{@link UserService#fetchUser} returns {@code null} when no user has that
     * id, and this method passes that straight back. When a {@code @RestController}
     * method returns {@code null}, Spring has nothing to serialise, so it replies
     * with an empty response. Measured against a running instance:
     *
     * <pre>
     *   GET /api/users/1     200  application/json  {"firstName":"Alpha","id":1,...}
     *   GET /api/users/999   200  (no content type) (empty body, 0 bytes)
     *   GET /api/users/abc   400  application/json  {"status":400,...}
     * </pre>
     *
     * <p>The middle line is wrong in a way that matters. {@code 200} means
     * <em>your request succeeded</em>, and the lookup did not succeed — there is
     * no such user. A caller checking only the status code concludes everything
     * is fine and then tries to read fields off nothing.
     *
     * <p>It is also indistinguishable from a genuine empty response, so a client
     * cannot tell "no such user" apart from "something went strangely wrong",
     * which are two situations you would want to handle very differently.
     *
     * <p>The correct answer is {@code 404 Not Found}, which says precisely what
     * happened: the URL is well formed, the route exists, and there is nothing at
     * it. Note this is the opposite call from {@link #getAllUsers}, and for a good
     * reason — asking for <em>all</em> users when there are none has a valid
     * answer ({@code []}), whereas asking for <em>one specific</em> user that does
     * not exist does not.
     *
     * @param id the id taken from the URL path
     * @return the matching user, or {@code null} if there is none — which reaches
     *         the caller as an empty {@code 200} rather than a {@code 404}
     */
    @GetMapping("/api/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.fetchUser(id);
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
