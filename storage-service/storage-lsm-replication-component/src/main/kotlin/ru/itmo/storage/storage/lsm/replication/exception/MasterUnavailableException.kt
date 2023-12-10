package ru.itmo.storage.storage.lsm.replication.exception

import ru.itmo.storage.storage.core.exception.StorageComponentException

class MasterUnavailableException: StorageComponentException("Master node is not available")