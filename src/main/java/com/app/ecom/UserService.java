package com.app.ecom;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the user data and the rules that go with it.
 *
 * <h2>Why this class exists</h2>
 *
 * <p>Before this class, {@link UserController} did three separate jobs at once:
 * it dealt with the web (reading requests, returning responses), it stored the
 * users, and it was the only place any rule could possibly live. That works
 * while there are two endpoints, and stops working shortly afterwards.
 *
 * <p>Splitting the service out draws a line. The controller now only knows about
 * HTTP, and this class only knows about users. Neither has to care how the other
 * one works, which means either can change without dragging the other along.
 *
 * <p>The practical payoff shows up when the database arrives: swapping the list
 * below for real database calls is a change to <em>this file only</em>. The
 * controller does not know the difference, and neither does anyone calling the
 * API.
 *
 * <h2>What {@code @Service} does</h2>
 *
 * <p>It tells Spring to create one of these at startup and keep it, ready to
 * hand to anything that asks for a {@code UserService} — which is exactly how
 * the controller gets hold of it without ever calling {@code new}.
 *
 * <p>Functionally {@code @Service} behaves the same as the more general
 * {@code @Component}. The reason to prefer it is that it says out loud what this
 * class is for: business logic, as opposed to a controller or a repository.
 *
 * <h2>Things to know before this sees real traffic</h2>
 *
 * <p><strong>There is one of these, shared by everyone.</strong> Spring creates a
 * single instance and every request uses it, on whatever thread happens to be
 * free. Two people signing up at the same instant therefore run
 * {@link #addUser} at the same instant, on the same {@code ArrayList}.
 *
 * <p>{@code ArrayList} is not built for that. Adding an item is three separate
 * steps under the hood — read how many items there are, write the new one into
 * that slot, then store the new count. Two threads running those three steps at
 * once can interleave badly: both write into the same slot and one user simply
 * disappears, with no error and nothing in the log.
 *
 * <p><strong>Nothing here is written to disk.</strong> The list lives in memory
 * only, so stopping the application throws away every user. That is a deliberate
 * choice for now rather than an oversight — it keeps the shape of the API
 * settled before the complications of a database arrive.
 *
 * <p>Both points are tracked as follow-up issues rather than left to be
 * rediscovered later.
 *
 * @see UserController the web layer that calls into this class
 */
@Service
public class UserService {

    /**
     * Every user the application currently knows about.
     *
     * <p>This is standing in for a database table. It starts empty on every boot
     * and is never written anywhere, so its contents last exactly as long as the
     * process does.
     *
     * <p>Users appear in the order they were added, because that is simply how a
     * list behaves. Nothing sorts them and nothing promises that order will hold
     * once a real database is answering the query instead.
     */
    private List<User> userList = new ArrayList<>();

    /**
     * Hands back every user currently held.
     *
     * <p>No filtering, no sorting, no paging — the whole collection, in insertion
     * order. That is fine at this size and will need revisiting long before the
     * list gets large, since returning ten thousand users in one response helps
     * nobody.
     *
     * <p>Worth knowing: this returns the actual internal list, not a copy of it.
     * Whoever receives it is holding this object's own data, and could in
     * principle change it. Nothing does that today, but the door is open.
     *
     * @return the live list of users, in the order they were added
     */
    public List<User> fetchAllUsers() {
        return userList;
    }

    /**
     * Adds a user to the collection.
     *
     * <p>The user is taken exactly as given. Nothing checks that the names are
     * filled in, nothing assigns an id, and nothing objects if the id supplied
     * matches a user that already exists. Whatever arrives is what gets stored.
     *
     * <p>The full list is returned rather than just the new user. The controller
     * currently ignores that return value, so it is doing no work for anyone —
     * a good sign the method probably wants a different shape once the response
     * format is settled.
     *
     * @param user the user to store, used as-is with no validation
     * @return every user held, including the one just added
     */
    public List<User> addUser(User user) {
        userList.add(user);
        return userList;
    }
}
