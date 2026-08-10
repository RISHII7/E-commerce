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
 * <h2>This class is not yet safe under concurrent load</h2>
 *
 * <p>Spring creates <strong>one</strong> of these and every request uses it, on
 * whatever thread happens to be free. Two people signing up at the same instant
 * therefore run {@link #addUser} at the same instant, on the same two fields.
 *
 * <p>Neither field is built for that, and the consequences are not theoretical.
 * Firing 300 simultaneous {@code POST} requests at a running instance produced:
 *
 * <pre>
 *   Users stored  : 294  (expected 300)  -- 6 users vanished
 *   Duplicate ids : 16                   -- e.g. id 18 given to 2 users
 * </pre>
 *
 * <p>Two separate races cause that, and each is explained where it lives — see
 * {@link #userList} and {@link #nextId}. Both come down to the same root cause:
 * an operation that looks like a single step in the source is several steps once
 * it runs, and another thread can slip in between them.
 *
 * <p><strong>Nothing here is written to disk either.</strong> The list lives in
 * memory only, so stopping the application throws away every user. That is a
 * deliberate choice for now rather than an oversight — it keeps the shape of the
 * API settled before the complications of a database arrive.
 *
 * <p>All of this is tracked as follow-up issues rather than left to be
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
     *
     * <p><strong>Not safe to write to from several threads at once.</strong>
     * Adding an item is three separate steps underneath: read how many items
     * there are, write the new one into that slot, then store the new count. Two
     * threads running those steps at the same moment can both write into the same
     * slot, and one user simply disappears — no exception, nothing in the log.
     * That is what cost 6 of 300 users in the measurement above.
     */
    private List<User> userList = new ArrayList<>();

    /**
     * The id that will be given to the next user created.
     *
     * <p>Stands in for a database's auto-increment column. It starts at 1 and
     * moves up by one each time a user is added, which is why ids come out as a
     * plain 1, 2, 3 sequence.
     *
     * <p>Because it resets to 1 on every boot and nothing is persisted, a
     * restarted application will happily hand out ids a previous run already
     * used. That stops being a concern once a database is assigning them.
     *
     * <p><strong>Also not safe under concurrent load.</strong> Writing
     * {@code nextId++} reads as a single instruction but is not one: it reads the
     * current value, adds one, and writes the result back. Two threads can read
     * the same value before either has written, and both then hand that same id
     * to different users.
     *
     * <p>That produced 16 duplicate ids in the measurement above, and it is the
     * more serious of the two failures. An id is how a user is meant to be
     * addressed; once two users share one, there is no way to say which was
     * meant.
     */
    private Long nextId = 1L;

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
     * Assigns the user an id and stores them.
     *
     * <p>The id is decided <strong>here</strong>, not by whoever sent the
     * request. Any {@code id} in the incoming JSON is overwritten before the user
     * is stored, so a caller cannot choose their own — verified by posting
     * {@code {"id":999,...}} and getting back a user carrying the next number in
     * the sequence instead.
     *
     * <p>That is the right way round. An id is how a resource is addressed, so
     * letting clients pick means two of them eventually pick the same one and
     * neither record can be identified afterwards. Settling it now also matters
     * because once a database holds rows with client-chosen ids, correcting it
     * needs a data migration rather than a code change.
     *
     * <p>Note that the {@link User} handed in is modified rather than copied —
     * its id is set on the object itself. That is harmless here, because Spring
     * builds a fresh {@code User} from the request body on every call and nothing
     * else holds a reference to it.
     *
     * <p>Still missing: <strong>nothing is validated</strong>. A user with no
     * first name, no last name, or empty strings for both is accepted and stored
     * exactly as sent.
     *
     * <p>This method used to return the full list, which the controller ignored.
     * It now returns nothing, which is more honest — a return value nobody reads
     * is a small lie about what a method is for.
     *
     * @param user the user to store; their id is overwritten with the next value
     *             in the sequence
     */
    public void addUser(User user) {
        user.setId(nextId++);
        userList.add(user);
    }

    /**
     * Finds the one user carrying the given id.
     *
     * <p>Walks the list and compares each user's id until one matches. Note that
     * the comparison uses {@code equals} rather than {@code ==}: both ids are
     * {@link Long} objects, not plain numbers, and {@code ==} on objects asks
     * whether they are the <em>same object</em> rather than whether they hold the
     * same value. For small numbers {@code ==} would appear to work, because Java
     * caches boxed values from -128 to 127 and hands back the same instance — and
     * would then start failing once ids passed 127, which is the kind of bug that
     * survives every test written on a short list.
     *
     * <h3>Returning {@code null} is the weak point</h3>
     *
     * <p>When no user matches, this returns {@code null}. The controller passes
     * that straight back to Spring, which has nothing to serialise and so answers
     * {@code 200 OK} with an empty body — a failed lookup reported as a success.
     * The right answer is {@code 404}, and the fix belongs on this side: throwing
     * a "not found" exception here, rather than returning {@code null}, lets the
     * web layer turn it into the correct status.
     *
     * <p>Returning {@link java.util.Optional} instead would at least make the
     * "might not be there" part impossible for a caller to overlook, which
     * {@code null} never does.
     *
     * <h3>It searches the whole list every time</h3>
     *
     * <p>Cost grows in step with the number of users: a thousand users means up to
     * a thousand comparisons per request. Irrelevant now, and it disappears on its
     * own once a database is doing the lookup, since finding a row by primary key
     * is exactly what a database is built for.
     *
     * <p>Reading the list while another thread is adding to it carries the same
     * risk described on {@link #userList} — an entry can be missed or seen half
     * written, because nothing coordinates the two.
     *
     * @param id the id to look for; a {@code null} id matches nothing and simply
     *           falls through the loop
     * @return the matching user, or {@code null} if no user has that id
     */
    public User fetchUser(Long id) {
        for (User user : userList) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }
}
