package com.kaii.trainspotter.domain

import com.kaii.trainspotter.R

enum class TrainInformation(val type: Int) {
    Owner(type = R.string.train_info_owner),
    Operator(type = R.string.train_info_operator),
    Product(type = R.string.train_info_product)
}