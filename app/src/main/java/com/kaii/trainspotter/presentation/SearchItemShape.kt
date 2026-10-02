package com.kaii.trainspotter.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import com.kaii.trainspotter.compose.widgets.search.SearchItemPositon
import com.kaii.trainspotter.helpers.RoundedCornerConstants

fun getSearchItemShapeFromPosition(position: SearchItemPositon) = when (position) {
    SearchItemPositon.Single -> RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_LARGE)

    SearchItemPositon.Top -> RoundedCornerShape(
        topStart = RoundedCornerConstants.ROUNDING_LARGE,
        topEnd = RoundedCornerConstants.ROUNDING_LARGE,
        bottomStart = RoundedCornerConstants.ROUNDING_SMALL,
        bottomEnd = RoundedCornerConstants.ROUNDING_SMALL
    )

    SearchItemPositon.Middle -> RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_SMALL)

    SearchItemPositon.Bottom -> RoundedCornerShape(
        topStart = RoundedCornerConstants.ROUNDING_SMALL,
        topEnd = RoundedCornerConstants.ROUNDING_SMALL,
        bottomStart = RoundedCornerConstants.ROUNDING_LARGE,
        bottomEnd = RoundedCornerConstants.ROUNDING_LARGE
    )
}