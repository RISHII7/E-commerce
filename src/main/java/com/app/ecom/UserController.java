package com.app.ecom;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * <h2>Where the URLs come from</h2>
 *
 * <p>{@code @RequestMapping("/api/users")} on the class sets the base path for
 * every endpoint in it. Each method then declares only the part that is its own,
 * and Spring joins the two:
 *
 * <pre>
 *   class     @RequestMapping("/api/users")
 *   method    @GetMapping            -&gt;  GET  /api/users
 *   method    @GetMapping("/{id}")   -&gt;  GET  /api/users/{id}
 *   method    @PostMapping           -&gt;  POST /api/users
 *   method    @PutMapping("/{id}")   -&gt;  PUT  /api/users/{id}
 * </pre>
 *
 * <p>Writing the full path on every method also works and is what this class did
 * originally. The problem with that is not the repetition itself but what
 * repetition invites: with the same string in four places, moving the resource
 * means changing four lines, and missing one leaves a single endpoint stranded at
 * the old URL. That failure is quiet — the application starts, three endpoints
 * work, and the fourth returns 404 with nothing to suggest why.
 *
 * <p>Stated once, the base path cannot disagree with itself.
 *
 * <h2>Why every method returns {@code ResponseEntity}</h2>
 *
 * <p>A method can simply return a {@link User} and let Spring wrap it up, which
 * is what these did originally. The catch is that you then only control the
 * <em>body</em>. The status code is whatever Spring decides, and that is always
 * {@code 200} unless something throws.
 *
 * <p>{@link org.springframework.http.ResponseEntity} is the whole response —
 * status, headers and body together — so the method decides all three. That is
 * what makes {@link #getUser} able to answer {@code 404} for a user that does
 * not exist instead of pretending the request succeeded.
 *
 * <p>It costs a little noise at every return statement. The trade is worth it as
 * soon as one endpoint needs a status other than {@code 200}, and using it
 * consistently means the response shape is visible in the method signature
 * rather than being an invisible default.
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
 *   <li><strong>Nothing is validated.</strong> No endpoint checks the body it is
 *       given, and {@link #updateUser} is where that stops being harmless: an
 *       empty JSON object erases both of a user's names and is answered
 *       {@code 200}. A caller who misspells a field name destroys data and is
 *       told it worked.</li>
 *   <li><strong>The endpoints disagree about their content type.</strong> Both
 *       {@code GET}s reply with JSON; {@code POST} and {@code PUT} reply with
 *       plain text. See {@link #createUser} for why that is awkward.</li>
 *   <li><strong>Writing endpoints do not return what they wrote.</strong>
 *       {@link #createUser} and {@link #updateUser} both answer with a fixed
 *       sentence, so a caller who wants to see the result has to fetch it
 *       separately — even though the server is holding it at the moment it
 *       replies.</li>
 *   <li><strong>Each method decides its own "not found" response.</strong>
 *       {@link #getUser} and {@link #updateUser} now build a {@code 404} each,
 *       from different starting points — one from an empty
 *       {@link java.util.Optional}, the other from a {@code false}. This is the
 *       second copy, which is exactly when the pattern is worth extracting.
 *       Having the service throw a "not found" exception, translated centrally by
 *       one exception handler, would state the rule once — and would give the
 *       response a body explaining what was missing, which neither of these
 *       can.</li>
 *   <li><strong>Nothing survives a restart</strong>, because the service holds
 *       users in memory. That limitation lives in {@link UserService}, not
 *       here.</li>
 * </ul>
 *
 * @see UserService where the users and the rules actually live
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
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
     * <p>The {@code 200} is stated explicitly here through
     * {@link org.springframework.http.ResponseEntity#ok}. It would have been the
     * default anyway, so this is not changing the behaviour — it keeps every
     * endpoint in the class declaring its status the same way, rather than some
     * declaring it and others relying on a default.
     *
     * @return {@code 200} with all users, in the order they were added
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(userService.fetchAllUsers());
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
     * <h3>A missing user answers 404, not 200</h3>
     *
     * <p>{@link UserService#fetchUser} hands back an {@link java.util.Optional},
     * which either holds the user or is empty. The two cases are turned into
     * responses in one expression:
     *
     * <pre>
     *   return userService.fetchUser(id)
     *       .map(ResponseEntity::ok)
     *       .orElseGet(() -&gt; ResponseEntity.notFound().build());
     * </pre>
     *
     * <p>{@code map} runs only when a user was found, wrapping it in a
     * {@code 200}. {@code orElseGet} supplies the {@code 404} when it was not.
     *
     * <p>{@code orElseGet} rather than {@code orElse} is deliberate.
     * {@code orElse} takes a value, so its argument is built on every call —
     * including the calls that found a user and will never use it. {@code orElseGet}
     * takes a function and only calls it when the {@code Optional} is empty. The
     * cost here is one small object, so it is not about speed; it is that
     * {@code orElse} silently does work that gets thrown away, and that habit stops
     * being harmless the moment the fallback is expensive or has side effects.
     *
     * <p>Measured against a running instance:
     *
     * <pre>
     *   GET /api/users/1     200  application/json  {"firstName":"Alpha","id":1,...}
     *   GET /api/users/999   404  (empty body)
     *   GET /api/users/abc   400  application/json  {"status":400,...}
     * </pre>
     *
     * <p>The middle line used to answer {@code 200} with an empty body, which was
     * wrong in a way that mattered. {@code 200} means <em>your request
     * succeeded</em>, and the lookup did not succeed — there is no such user. A
     * caller checking only the status code concluded everything was fine and then
     * tried to read fields off nothing. It was also indistinguishable from a
     * genuine empty response, so a client could not tell "no such user" apart
     * from "something went strangely wrong".
     *
     * <p>{@code 404} says precisely what happened: the URL is well formed, the
     * route exists, and there is nothing at it.
     *
     * <p>Note this is deliberately the <strong>opposite</strong> call from
     * {@link #getAllUsers}, which answers {@code 200} with {@code []} rather than
     * {@code 404} when there are no users at all. Asking for <em>all</em> users
     * when there are none has a valid answer; asking for <em>one specific</em>
     * user that does not exist does not. The two are not inconsistent, and the
     * reasoning is recorded on both so neither gets "tidied up" into matching the
     * other.
     *
     * <p>The response body for a {@code 404} is empty. That is acceptable because
     * the status already carries the whole message, though a short problem-detail
     * body would be friendlier once there is a standard error format to follow.
     *
     * @param id the id taken from the URL path
     * @return {@code 200} with the user, or {@code 404} with an empty body if no
     *         user has that id
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.fetchUser(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
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
     * <p>The body is still a {@code String}, and wrapping it in a
     * {@code ResponseEntity} does not change that: Spring picks how to serialise
     * based on the body type, so a {@code String} still goes out as
     * {@code text/plain} rather than JSON. Confirmed against a running instance —
     * both {@code GET}s answer {@code application/json} while this answers
     * {@code text/plain;charset=UTF-8}, so a caller cannot parse every response
     * from this API the same way.
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
     * @return {@code 200} with a fixed confirmation sentence, sent as
     *         {@code text/plain}
     */
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userService.addUser(user);
        return ResponseEntity.ok("User Added Successfully");
    }

    /**
     * Replaces an existing user's names.
     *
     * <p>Answers {@code PUT /api/users/{id}}. This is the first endpoint that
     * takes both a path variable and a request body: the {@code id} says
     * <em>which</em> user to change, and the body says <em>what to change it to</em>.
     *
     * <p>{@code PUT} rather than {@code POST} because the operation is
     * idempotent — sending the same request twice leaves the user in exactly the
     * same state as sending it once. That is not a technicality: it means a client
     * that loses the connection and retries cannot do any harm, which is precisely
     * why {@code PUT} exists as a separate method from {@code POST}.
     *
     * <h3>What identifies the user</h3>
     *
     * <p>The {@code id} in the URL, and only that. Any id in the request body is
     * ignored, so {@code PUT /api/users/1} with {@code {"id":555,...}} in the body
     * still updates user 1 and leaves them as user 1 — verified against a running
     * instance. Allowing the body to win would mean the resource you edited is no
     * longer at the URL you edited it through.
     *
     * <h3>Responses</h3>
     *
     * <pre>
     *   PUT /api/users/1    {"firstName":"Updated","lastName":"Name"}
     *                       -&gt; 200  text/plain  User Updated Successfully
     *   PUT /api/users/999  {"firstName":"Ghost","lastName":"User"}
     *                       -&gt; 404  (empty body)
     * </pre>
     *
     * <p>The {@code 404} is the same decision as {@link #getUser}: asking to
     * change a user that does not exist has no sensible successful answer. Note
     * this endpoint deliberately does <strong>not</strong> create the user in that
     * case. Some APIs treat {@code PUT} to a missing id as "create it here", which
     * is defensible, but it sits badly with server-assigned ids — the client would
     * be choosing the id after all.
     *
     * <h3>Careful: an empty body erases both names</h3>
     *
     * <p>Both names are overwritten every time, so a request with fields missing
     * sets them to {@code null} and still reports success:
     *
     * <pre>
     *   PUT /api/users/1  {}   -&gt;  200 User Updated Successfully
     *   GET /api/users/1       -&gt;  {"firstName":null,"id":1,"lastName":null}
     * </pre>
     *
     * <p>Strictly this is what {@code PUT} is supposed to do — it means "make the
     * resource look like this", not "change these bits". The problem is that
     * nothing validates the body, so a caller who misspells a field name wipes
     * the data and is told it worked. Rejecting blank names would turn that from a
     * silent accident into a {@code 400}.
     *
     * <p>A caller who genuinely wants to change one field without touching the
     * other is asking for {@code PATCH}, which does not exist here yet.
     *
     * <h3>The response body has the same problem as {@link #createUser}</h3>
     *
     * <p>It is a plain {@code String}, so this endpoint answers {@code text/plain}
     * while both {@code GET}s answer JSON. It also does not return the updated
     * user, so a caller wanting to see the result has to fetch it again.
     *
     * @param id          which user to update, taken from the URL path
     * @param updatedUser the new values, read from the JSON request body; only
     *                    the names are used
     * @return {@code 200} with a confirmation sentence, or {@code 404} if no user
     *         has that id
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        boolean updated = userService.updateUser(id, updatedUser);
        if (updated)
            return ResponseEntity.ok("User Updated Successfully");
        return ResponseEntity.notFound().build();
    }
}
