package ru.demo.data.device

import ru.demo.domain.base.ApplicationException

class NeverAskedAgainPermissionException(message: String): ApplicationException(message)