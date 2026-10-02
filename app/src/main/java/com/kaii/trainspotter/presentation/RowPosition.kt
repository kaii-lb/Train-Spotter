package com.kaii.trainspotter.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import com.kaii.trainspotter.helpers.RoundedCornerConstants

enum class RowPosition {
    Top, Middle, Bottom,
    Single;

    val shape: RoundedCornerShape
        get() = when (this) {
            Single -> RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_LARGE)

            Top -> RoundedCornerShape(
                topStart = RoundedCornerConstants.ROUNDING_LARGE,
                topEnd = RoundedCornerConstants.ROUNDING_LARGE,
                bottomStart = RoundedCornerConstants.ROUNDING_SMALL,
                bottomEnd = RoundedCornerConstants.ROUNDING_SMALL
            )

            Middle -> RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_SMALL)

            Bottom -> RoundedCornerShape(
                topStart = RoundedCornerConstants.ROUNDING_SMALL,
                topEnd = RoundedCornerConstants.ROUNDING_SMALL,
                bottomStart = RoundedCornerConstants.ROUNDING_LARGE,
                bottomEnd = RoundedCornerConstants.ROUNDING_LARGE
            )
        }
}