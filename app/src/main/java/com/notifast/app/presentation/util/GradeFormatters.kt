package com.notifast.app.presentation.util

import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale

object GradeFormatters {

    fun grade(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String {
        if (grade == null) return "-"
        val src = GradeScale.fromId(sourceScaleId)
        return displayScale.formatGrade(GradePolicy.convertGrade(grade, src, displayScale))
    }

    fun average(value: Double, displayScale: GradeScale): String =
        displayScale.formatGrade(value)

    fun gradeWithScale(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String {
        if (grade == null) return "-"
        if (displayScale == GradeScale.SCALE_LETTER) return displayScale.formatGrade(
            GradePolicy.convertGrade(grade, GradeScale.fromId(sourceScaleId), displayScale)
        )
        val formatted = grade(grade, sourceScaleId, displayScale)
        val max = if (displayScale.maxGrade % 1.0 == 0.0) {
            displayScale.maxGrade.toInt().toString()
        } else {
            displayScale.maxGrade.toString()
        }
        return "$formatted/$max"
    }

    fun passing(value: Double, displayScale: GradeScale): String =
        displayScale.formatGrade(value)

    /**
     * Seed text for grade inputs: converts the stored grade from its own scale to
     * [displayScale] (rounded) so the field matches what validation expects.
     */
    fun seed(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String {
        if (grade == null) return ""
        val converted = GradePolicy.convertGrade(grade, GradeScale.fromId(sourceScaleId), displayScale)
        val rounded = displayScale.roundGrade(converted)
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }
}
