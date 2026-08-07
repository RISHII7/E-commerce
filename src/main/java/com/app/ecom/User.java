package com.app.ecom;

import lombok.Data;

/**
 * Represents a single user of the store.
 *
 * <p>This is a plain data holder. It carries user information between the
 * controller and whoever is calling the API, and does nothing else — there are
 * no business rules here on purpose.
 *
 * <p>Right now this same class is used for two jobs at once: it is what we keep
 * in memory, and it is also the exact shape of the JSON going in and out of the
 * API. That is fine while the project is this small. Once there is a real
 * database, these two jobs are normally split apart, so that changing how a user
 * is stored does not accidentally change the API that other people depend on.
 */
@Data
public class User {

    /**
     * Unique number identifying this user.
     *
     * <p>Note that nothing generates this value yet — whatever the caller sends
     * in the request body is what gets used, and sending nothing leaves it null.
     */
    private Long id;

    /** The user's first name. */
    private String firstName;

    /** The user's last name. */
    private String lastName;
}