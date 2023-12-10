package ru.itmo.storage.storage.lsm.replication.master

import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class ReplicationAspect(
    private val replicationService: ReplicationService
) {
    @Pointcut("within(ru.itmo.storage.storage.core.KeyValueRepository+)")
    protected fun dbOperationsPointcut() {
    }

    @Pointcut("dbOperationsPointcut() && execution(* set(..))")
    protected fun upsertPointcut() {
    }

    @AfterReturning(value = "upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun performReplication(key: String, value: String) {
        replicationService.sendToSyncReplicas(key, value)
    }
}
