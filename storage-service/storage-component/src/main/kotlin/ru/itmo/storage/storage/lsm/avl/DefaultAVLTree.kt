package ru.itmo.storage.storage.lsm.avl

import ru.itmo.storage.storage.lsm.avl.AVLTree.Entry
import kotlin.collections.ArrayList
import kotlin.math.max

const val LINK_SIZE_BYTES = 8L
const val CHAR_SIZE_BYTES = 2L
const val INT_SIZE_BYTES = 4L
const val LONG_SIZE_BYTES = 8L
const val NODE_SIZE_BASE_BYTES = LINK_SIZE_BYTES * 4 + INT_SIZE_BYTES
const val AVL_SIZE_BASE_BYTES = LINK_SIZE_BYTES + LONG_SIZE_BYTES

class DefaultAVLTree : AVLTree {
    private data class Node(
        val key: String,
        var value: String,
        var height: Int = 1,
        var leftChild: Node? = null,
        var rightChild: Node? = null,
    ) {
        val balance: Int
            get() = (leftChild?.height ?: 0) - (rightChild?.height ?: 0)

        fun deepCopy(): Node = copy(
            leftChild = leftChild?.deepCopy(),
            rightChild = rightChild?.deepCopy(),
        )
    }

    private var root: Node? = null

    override var sizeInBytes: Long = AVL_SIZE_BASE_BYTES
        private set

    override fun upsert(key: String, value: String) {
        val existing = search(root, key)

        if (existing != null) {
            beforeUpdate(existing.value, value)
            existing.value = value
            return
        }

        beforeInsert(key, value)
        root = insert(root, key, value)
    }

    override fun find(key: String): String? = search(root, key)?.value

    override fun copy(): AVLTree {
        val copy = DefaultAVLTree()
        copy.root = root?.deepCopy()
        copy.sizeInBytes = sizeInBytes

        return copy
    }

    override fun orderedEntries(): List<Entry> {
        val ans: MutableList<Entry> = ArrayList()
        inorderTraversal(ans, root)

        return ans
    }

    private fun inorderTraversal(entries: MutableList<Entry>, node: Node?) {
        if (node == null) {
            return
        }
        inorderTraversal(entries, node.leftChild)
        entries.add(Entry(node.key, node.value))
        inorderTraversal(entries, node.rightChild)
    }

    private fun beforeUpdate(oldValue: String, newValue: String) {
        sizeInBytes -= oldValue.length * CHAR_SIZE_BYTES
        sizeInBytes += newValue.length * CHAR_SIZE_BYTES
    }

    private fun beforeInsert(key: String, value: String) {
        sizeInBytes += NODE_SIZE_BASE_BYTES
        sizeInBytes += key.length * CHAR_SIZE_BYTES
        sizeInBytes += value.length * CHAR_SIZE_BYTES
    }

    private fun search(tile: Node?, key: String): Node? {
        tile ?: return null

        return when {
            key == tile.key -> tile
            key > tile.key -> search(tile.rightChild, key)
            // key < tile.key
            else -> search(tile.leftChild, key)
        }
    }

    private fun insert(tile: Node?, key: String, value: String): Node {
        tile ?: return Node(key, value)

        if (key > tile.key) {
            tile.rightChild = insert(tile.rightChild, key, value)
        } else if (key < tile.key) {
            tile.leftChild = insert(tile.leftChild, key, value)
        } else {
            throw IllegalStateException("You shouldn't update value in insert func")
        }

        val balancedTile = balance(tile)
        balancedTile.height = max(getHeight(balancedTile.leftChild), getHeight(tile.rightChild)) + 1

        return balancedTile
    }

    private fun balance(tile: Node?): Node {
        tile ?: throw IllegalStateException("tile cannot be null in balance")

        val statusLeft = getBalance(tile.leftChild)
        val statusRoot = getBalance(tile)
        val statusRight = getBalance(tile.rightChild)

        return if (statusRoot == -2) {
            if (statusRight == 1) {
                bigLeftRotate(tile)
            } else {
                smallLeftRotate(tile)
            }
        } else if (statusRoot == 2) {
            if (statusLeft == -1) {
                bigRightRotate(tile)
            } else {
                smallRightRotate(tile)
            }
        } else {
            tile
        }
    }

    private fun bigRightRotate(tile: Node): Node {
        tile.leftChild = smallLeftRotate(
            tile.leftChild ?: throw IllegalStateException("tile.leftChild cannot be null in bigRightRotate"),
        )

        return smallRightRotate(tile)
    }

    private fun bigLeftRotate(tile: Node): Node {
        tile.rightChild = smallRightRotate(
            tile.rightChild ?: throw IllegalStateException("tile.rightChild cannot be null in bigLeftRotate"),
        )

        return smallLeftRotate(tile)
    }

    private fun smallLeftRotate(tile: Node): Node {
        val tmp = tile.rightChild ?: throw IllegalStateException("tile.rightChild cannot be null in smallLeftRotate")
        tile.rightChild = tmp.leftChild
        tmp.leftChild = tile

        tile.height = max(getHeight(tile.leftChild), getHeight(tile.rightChild)) + 1
        tmp.height = max(getHeight(tmp.leftChild), getHeight(tmp.rightChild)) + 1

        return tmp
    }

    private fun smallRightRotate(tile: Node): Node {
        val tmp = tile.leftChild ?: throw IllegalStateException("tile.leftChild cannot be null in smallRightRotate")
        tile.leftChild = tmp.rightChild
        tmp.rightChild = tile

        tile.height = max(getHeight(tile.leftChild), getHeight(tile.rightChild)) + 1
        tmp.height = max(getHeight(tmp.leftChild), getHeight(tmp.rightChild)) + 1

        return tmp
    }

    private fun getBalance(tile: Node?): Int = tile?.balance ?: 0

    private fun getHeight(tile: Node?): Int = tile?.height ?: 0
}
