package helper.scxml.scxml2.t2_traffic

import helper.scxml.scxml2.Res

object Res {
    val globalTimeId = Res.globalTimeId
    val renStateList = arrayListOf(
        "Aalborg",
        "Wait",
    )
    val renEventList = arrayListOf(
        "bike",
        "car",
        "train",
        "train_wait_back",
        "train_wait_train",
    )

    val c1s = arrayListOf("car", "bike", "train")
    val c2s = arrayListOf("train_wait_train", "train_wait_back")
}