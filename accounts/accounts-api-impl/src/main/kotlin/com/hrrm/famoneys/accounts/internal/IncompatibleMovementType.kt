package com.hrrm.famoneys.accounts.internal

import com.hrrm.famoneys.commons.core.FamoneyException

class IncompatibleMovementType : FamoneyException {
    constructor() : super() {}
    constructor(
        message: String?, cause: Throwable?, enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace) {
    }

    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(message: String?) : super(message) {}
    constructor(cause: Throwable?) : super(cause) {}
}