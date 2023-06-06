package helper.block

import helper.block.BlockHelper.BlockFactory
import helper.block.BlockHelper.language_ta

object TABlockHelper {
    class TABlockFactory : BlockFactory(
        language = language_ta,
    )
}