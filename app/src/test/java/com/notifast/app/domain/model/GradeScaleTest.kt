package com.notifast.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GradeScaleTest {

    @Test
    fun `roundGrade keeps one decimal on 0-10 and 0-20`() {
        assertEquals(7.3, GradeScale.SCALE_0_10.roundGrade(7.34), 0.001)
        assertEquals(14.6, GradeScale.SCALE_0_20.roundGrade(14.56), 0.001)
    }

    @Test
    fun `roundGrade rounds to integer on 0-100`() {
        assertEquals(74.0, GradeScale.SCALE_0_100.roundGrade(73.6), 0.001)
        assertEquals(73.0, GradeScale.SCALE_0_100.roundGrade(73.4), 0.001)
    }

    @Test
    fun `roundGrade keeps one decimal on letter scale GPA`() {
        assertEquals(3.0, GradeScale.SCALE_LETTER.roundGrade(2.95), 0.001)
    }

    @Test
    fun `scale_0_100 default passing is 60`() {
        assertEquals(60.0, GradeScale.SCALE_0_100.minPassingGrade, 0.001)
    }

    @Test
    fun `formatGrade maps letter bands`() {
        assertEquals("A", GradeScale.SCALE_LETTER.formatGrade(4.0))
        assertEquals("B", GradeScale.SCALE_LETTER.formatGrade(3.0))
        assertEquals("C", GradeScale.SCALE_LETTER.formatGrade(2.0))
        assertEquals("F", GradeScale.SCALE_LETTER.formatGrade(0.5))
    }
}
