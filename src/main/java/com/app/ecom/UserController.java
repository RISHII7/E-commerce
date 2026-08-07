package com.app.ecom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The web entry point for everything to do with users.
 *
 * <p>{@code @RestController} tells Spring two things: create one of these when
 * the application starts and let it handle web requests, and turn whatever the
 * methods return straight into JSON. That is why no method here mentions JSON —
 * Spring does that conversion for us.
 *
 * <p>Users are kept in a plain list in memory. There is no database yet, so
 * <strong>everything is lost the moment the application stops</strong>. That is
 * deliberate for a first version: it keeps the focus on getting the API shape
 * right before bringing in persistence.
 *
 * <p>Two things to be aware of before this goes anywhere near real traffic:
 *
 * <ul>
 *   <li><strong>Spring creates only one instance of this class</strong> and
 *       shares it across every request. Several requests can therefore run
 *       {@code createUser} at the same moment, on different threads, and
 *       {@code ArrayList} is not built to be written to from several threads at
 *       once. Under load that can lose a user or fail outright.</li>
 *   <li><strong>{@code getAllUsers} hands back the real list</strong>, not a
 *       copy, so whoever receives it is holding the controller's own data.</li>
 * </ul>
 *
 * <p>Neither matters while one person is clicking through it by hand. Both are
 * tracked as follow-up work.
 */
@RestController
public class UserController {

    /**
     * Where users are kept while the application is running.
     *
     * <p>Stands in for a database table for now. It starts empty every time the
     * application boots.
     */
    private List<User> userList = new ArrayList<>();

    /**
     * Returns every user currently held.
     *
     * <p>Answers {@code GET /api/users}. Spring turns the returned list into a
     * JSON array, so an empty list comes back as {@code []} rather than as an
     * error or an empty body — which is the correct answer to "show me all
     * users" when there simply are not any yet.
     *
     * @return all users, in the order they were added
     */
    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        return userList;
    }

    /**
     * Adds a new user.
     *
     * <p>Answers {@code POST /api/users}. {@code @RequestBody} tells Spring to
     * read the JSON from the request and build a {@link User} out of it, matching
     * the JSON field names to the field names on the class.
     *
     * <p>Note what this returns: the <em>whole</em> list, not just the user that
     * was created. So the response grows with every call. The more usual REST
     * convention is to return only the newly created user, along with a
     * {@code 201 Created} status and a {@code Location} header pointing at it.
     * As written, this replies {@code 200 OK} with the full collection.
     *
     * <p>Nothing is validated yet either — a request with no name at all, or a
     * duplicate id, is accepted exactly as sent.
     *
     * @param user the user to add, read from the JSON request body
     * @return every user held, including the one just added
     */
    @PostMapping("/api/users")
    public List<User> createUser(@RequestBody User user) {
        userList.add(user);
        return userList;
    }
}