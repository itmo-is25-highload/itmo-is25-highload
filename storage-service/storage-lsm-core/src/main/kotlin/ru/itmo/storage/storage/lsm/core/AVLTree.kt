package ru.itmo.storage.storage.lsm.core

/**
 * Сбалансированное бинарное дерево
 */
interface AVLTree {

    data class Entry(
        val key: String,
        val value: String,
    )

    /**
     * Пропертя для получения размера дерева в байтах
     *  считается приблизительно
     *
     * размер(ссылка) = 8
     * размер(строка) = строка.size
     * размер(нода) = 4 * размер(ссылка) + размер(ключ) + размер(значение)
     * размер(дерево) = количество_элементов * размер(нода)
     */
    val sizeInBytes: Long

    /**
     * Функция, которая добавляет ключ в дерево (если его не было)
     *  или обновляет value по ключу
     *
     * @param key ключ
     * @param value значение
     */
    fun upsert(key: String, value: String)

    /**
     * Функция для получения значения по ключу
     *   вернет null, если такого значения в дереве нет
     *
     * @param key ключ
     *
     * @return значение по ключу или null
     */
    fun find(key: String): String?

    /**
     * Метод для получения глубокой копии дерева
     *  нужно для асинхронной обработки сохранения дерева в SSTable
     */
    fun copy(): AVLTree

    /**
     * Метод для получения всех значений в упорядоченном по возрастанию списке
     */
    fun orderedEntries(): List<Entry>
}
