package ru.itmo.storage.local

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree

class AVLTreeTest {
    @Test
    fun stressTest() {
        val elements = listOf("1", "2", "3", "4", "5", "6", "7")

        elements.permutations().forEach { permutation ->
            val tree = DefaultAVLTree()

            permutation.forEach { tree.upsert(it, "value") }
            permutation.forEach {
                Assertions.assertEquals("value", tree.find(it))
            }
            Assertions.assertEquals(null, tree.find("-1"))

            println("Permutation $permutation success")
        }
    }
}

private fun <E> List<E>.permutations(builtSequence: List<E> = listOf()): List<List<E>> =
    if (isEmpty()) {
        listOf(builtSequence)
    } else {
        flatMap { (this - it).permutations(builtSequence + it) }
    }
