package ru.itmo.storage.server.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.exception.KeyNotFoundException

@WebMvcTest(controllers = [StorageController::class])
internal class StorageControllerTest(@Autowired val mockMvc: MockMvc) {

    private val path = "/keys/{key}"
    private val key = "TestKey"
    private val value = "TestValue"

    @MockBean
    private lateinit var repository: KeyValueRepository

    @Test
    fun `get by key - success`() {
        given(repository.get(key)).willReturn(value)

        val result = mockMvc.perform(get(path, key))
            .andExpect(status().isOk)
            .andReturn()
        val returnValue = getResultValue(result)


        Assertions.assertEquals(value, returnValue)
    }

    @Test
    fun `get by nonexistent key - not found`() {
        given(repository.get(key)).willThrow(KeyNotFoundException(key))

        mockMvc.perform(get(path,  key))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `set key - success`() {
        mockMvc.perform(put(path, key)
            .param("value", value)
        ).andExpect(status().isOk)
    }

    private fun getResultValue(result: MvcResult) = result.response.contentAsString
}
