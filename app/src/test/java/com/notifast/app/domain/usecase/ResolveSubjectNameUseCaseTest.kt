package com.notifast.app.domain.usecase

import com.notifast.app.domain.util.NameValidation
import com.notifast.app.domain.util.SubjectNameRules
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ResolveSubjectNameUseCaseTest {

    private lateinit var useCase: ResolveSubjectNameUseCase

    @Before
    fun setUp() {
        useCase = ResolveSubjectNameUseCase()
    }

    @Test
    fun suggest_returns_original_name_if_not_existing() {
        val result = useCase.suggest("Física", emptyList())
        assertEquals("Física", result)
    }

    @Test
    fun suggest_appends_1_if_name_exists() {
        val result = useCase.suggest("Cálculo", listOf("Cálculo"))
        assertEquals("Cálculo (1)", result)
    }

    @Test
    fun suggest_increments_counter_if_numbered_suffixes_exist() {
        val result = useCase.suggest("Cálculo", listOf("cálculo", "Cálculo (1)"))
        assertEquals("Cálculo (2)", result)
    }

    @Test
    fun suggest_truncates_base_if_result_would_exceed_max_length() {
        val longBase = "A".repeat(50)
        val result = useCase.suggest(longBase, listOf(longBase))
        // Max length is 50. " (1)" takes 4 chars, so base.take(46) + " (1)" is 50 chars.
        assertEquals("${longBase.take(46)} (1)", result)
        assertEquals(SubjectNameRules.MAX_LENGTH, result.length)
    }

    @Test
    fun validate_returns_Blank_for_empty_or_whitespace() {
        assertEquals(NameValidation.Blank, useCase.validate("   ", emptyList()))
        assertEquals(NameValidation.Blank, useCase.validate("", emptyList()))
    }

    @Test
    fun validate_returns_TooLong_for_strings_exceeding_max_length() {
        val overLength = "a".repeat(SubjectNameRules.MAX_LENGTH + 1)
        assertEquals(NameValidation.TooLong, useCase.validate(overLength, emptyList()))
    }

    @Test
    fun validate_returns_Duplicate_if_exists_ignoring_case_and_whitespace() {
        assertEquals(NameValidation.Duplicate, useCase.validate("cálculo ", listOf("Cálculo")))
    }

    @Test
    fun validate_returns_Valid_for_new_proper_name() {
        assertEquals(NameValidation.Valid, useCase.validate("Álgebra", listOf("Cálculo")))
    }
}
