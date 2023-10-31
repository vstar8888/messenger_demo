package ru.demo.data.device

import ru.demo.domain.base.ApplicationException

class PermissionDeniedException(message: String) : ApplicationException(message)