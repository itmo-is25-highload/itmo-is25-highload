package ru.itmo.storage.local

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.itmo.storage.storage.lsm.avl.AVL_SIZE_BASE_BYTES
import ru.itmo.storage.storage.lsm.avl.CHAR_SIZE_BYTES
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.avl.NODE_SIZE_BASE_BYTES

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

            val expectedSize = 7 * (6 * CHAR_SIZE_BYTES + NODE_SIZE_BASE_BYTES) + AVL_SIZE_BASE_BYTES
            Assertions.assertEquals(expectedSize, tree.sizeInBytes)

            println("Permutation $permutation success")
        }
    }

    @Test
    fun sizeTest() {
        val tree = DefaultAVLTree()

        val elements = listOf("1", "2", "3", "4", "5", "6", "7")
        elements.forEach { tree.upsert(it, "") }

        tree.upsert("1", "")

        val expectedSize = 7 * (CHAR_SIZE_BYTES + NODE_SIZE_BASE_BYTES) + AVL_SIZE_BASE_BYTES
        Assertions.assertEquals(expectedSize, tree.sizeInBytes)
    }

    @Test
    fun copyTest() {
        val tree = DefaultAVLTree()

        val elements = listOf("1", "2", "3", "4", "5", "6", "7")
        elements.forEach { tree.upsert(it, "") }

        val copy = tree.copy()
        tree.upsert("key", "value")

        elements.forEach {
            Assertions.assertEquals("", tree.find(it))
        }
        Assertions.assertNull(copy.find("key"))
        Assertions.assertEquals("value", tree.find("key"))
    }
}

private fun <E> List<E>.permutations(builtSequence: List<E> = listOf()): List<List<E>> =
    if (isEmpty()) {
        listOf(builtSequence)
    } else {
        flatMap { (this - it).permutations(builtSequence + it) }
    }
