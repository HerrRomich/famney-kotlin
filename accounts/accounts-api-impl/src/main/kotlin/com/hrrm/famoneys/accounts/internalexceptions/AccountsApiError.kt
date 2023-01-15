package com.hrrm.famoneys.accounts.internalexceptions

import com.hrrm.famoney.jaxrs.ApiError
import javax.ws.rs.core.Response

enum class AccountsApiError(override val message: String, override val status: Response.Status) : ApiError {
    NO_ACCOUNT_ON_GET_ALL_ACCOUNT_MOVEMENTS(
        "No account was found for request of all account movements.",
        Response.Status.NOT_FOUND
    ),
    NO_ACCOUNT_ON_CHANGE(
        "No account was found for request on account change.",
        Response.Status.NOT_FOUND
    ),
    NO_ACCOUNT_ON_GET_ACCOUNT(
        "No account was found for request by id.",
        Response.Status.NOT_FOUND
    ),
    NO_ACCOUNT_ON_ADD_MOVEMENT(
        "No account was found for adding a movement.",
        Response.Status.NOT_FOUND
    ),
    NO_MOVEMENT_ON_GET_MOVEMENT(
        "No movement was found for request by id in a specified account.",
        Response.Status.NOT_FOUND
    ),
    NO_ACCOUNT_ON_CHANGE_MOVEMENT(
        "No account was found for changing a movement.",
        Response.Status.NOT_FOUND
    ),
    NO_MOVEMENT_ON_CHANGE_MOVEMENT("No movement was found for changing.", Response.Status.NOT_FOUND);

    override val code = name
    override val prefix = "accounts"
}