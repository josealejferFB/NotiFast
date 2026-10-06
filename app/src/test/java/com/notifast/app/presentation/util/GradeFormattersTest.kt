package com.notifast.app.presentation.util

import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Test

class GradeFormattersTest {

    @Test
    fun `average formats per display scale`() {
        assertEquals("7.5", GradeFormatters.average(7.5, GradeScale.SCALE_0_10))
        assertEquals("15.0", GradeFormatters.average(15.0, GradeScale.SCALE_0_20))
        assertEquals("75", GradeFormatters.average(75.0, GradeScale.SCALE_0_100))
        assertEquals("B", GradeFormatters.average(3.0, GradeScale.SCALE_LETTER))
    }

    @Test
    fun `gradeWithScale shows max of display scale`() {
        assertEquals("7.5/10", GradeFormatters.gradeWithScale(7.5, "0-10", GradeScale.SCALE_0_10))
        assertEquals("75/100", GradeFormatters.gradeWithScale(75.0, "0-100", GradeScale.SCALE_0_100))
        assertEquals("A", GradeFormatters.gradeWithScale(4.0, "A-F", GradeScale.SCALE_LETTER))
    }

    @Test
    fun `null grade renders dash`() {
        assertEquals("-", GradeFormatters.grade(null, "0-10", GradeScale.SCALE_0_10))
    }
}
