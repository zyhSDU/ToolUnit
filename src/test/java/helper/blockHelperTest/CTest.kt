package helper.blockHelperTest

import helper.base.PrintHelper.StringTo.toPrint
import helper.block.BlockHelper
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.Expand.ToBlockList.toBlockArrayList
import helper.block.CBlockHelper
import org.junit.Test

internal class CTest {
    val bf = CBlockHelper.CBlockFactory.bf

    @Test
    fun cTest1() {
        fun tempGetBlock1(): BlockHelper.Block {
            val cIncludesBlock = bf.getEmptyBlock()
            (0 until 3).map {
                cIncludesBlock.addBlock(bf.getIncludeBlock("a${it}.h"))
            }
            return cIncludesBlock
        }

        fun tempGetBlock2(): BlockHelper.Block {
            val cB = bf.getEmptyBlock()
            arrayOf(
                arrayOf("XTime", "tEnd"),
                arrayOf("XTime", "tCur"),
                arrayOf("u32", "tUsed"),
            ).map {
                cB.addBlock(bf.getArgDeclareBlock(it[0], it[1]))
            }
            return cB
        }

        fun tempGetBlock3(): BlockHelper.Block {
            val cb = bf.getEmptyBlock()
            cb.addBlock(bf.getDefineBlock("input", "0"))
            cb.addBlock(bf.getDefineBlock("output", "1"))
            return cb
        }

        fun tempGetBlock4(): BlockHelper.Block {
            val argTypeInt = "uint16_t"
            val argTime = "time"
            val argI = "i"
            val argJ = "j"
            val cB = bf.getEmptyBlock()
            val cMethodBlock = bf.getFunDefineBlock(
                return_type = "void",
                method_name = "delay",
                args_block = bf.getArgBlock(argTypeInt, argTime),
                bBlocks = arrayListOf(
                    bf.getArgDeclareBlock(
                        arg_type = argTypeInt,
                        arg_name = bf.getArgsBlock(
                            arrayListOf(
                                argI.toBlock(),
                                argJ.toBlock(),
                            )
                        )
                    ),
                    bf.getForBlock11(
                        arg_i = argI,
                        i_min = 0,
                        i_max = argTime,
                        bBlocks = arrayListOf(
                            bf.getForBlock11(
                                arg_i = argJ,
                                i_min = 0,
                                i_max = 100,
                            ),
                        ),
                    ),
                ),
            )
            cB.addBlock(cMethodBlock)
            return cB
        }

        fun tempGetBlock5(): BlockHelper.Block {
            val argTypeInt = "unsigned int"
            val methodName = "systemMs"
            val argTypeVoid = "void".toBlock()

            return bf.getFunDefineBlock(
                return_type = argTypeInt,
                method_name = methodName,
                args_block = argTypeVoid,
                bBlocks = arrayListOf(
                    "\nXTime_GetTime(&tEnd);".toBlock(),
                    bf.getReturnBlock(
                        "((tEnd) * 1000) / (COUNTS_PER_SECOND)".toBlock()
                    ),
                ),
            )
        }

        val cb = bf.getEmptyBlock()
        cb.addBlock(tempGetBlock1())
        cb.addLineBlock()
        cb.addBlock(tempGetBlock2())
        cb.addLineBlock()
        cb.addBlock(tempGetBlock3())
        cb.addLineBlock()
        cb.addBlock(tempGetBlock4())
        cb.addLineBlock()
        cb.addBlock(tempGetBlock5())
        cb.addLineBlock()
        val str = cb.getStr()
        str.toPrint()
        val ar = """#include a1.h
#include a1.h
#include a2.h

XTime tEnd;
XTime tCur;
u32 tUsed;

#define input 0 
#define output 1 

void delay(uint16_t time) {
	uint16_t i, j;
	for(i = 0; i<time; i++){
		for(j = 0; j<100; j++){
		}
	}
}

unsigned int systemMs(void) {
	XTime_GetTime(&tEnd);
	return ((tEnd) * 1000) / (COUNTS_PER_SECOND);
}
"""
        assert(str == ar)
    }

    @Test
    fun cTest2() {
        val cb = bf.getEmptyBlock()
        val includes_block = bf.getEmptyBlock()
        val includes = arrayListOf(
            "<stdio.h>",
            "\"platform.h\"",
            "\"xgpiops.h\"",
            "\"xgpiops_hw.h\"",
            "\"sleep.h\"",
        )
        includes.map {
            includes_block.addBlock(bf.getIncludeBlock(it))
        }
        cb.addBlock(includes_block)
        val definesInPin = arrayListOf(
            arrayListOf("EMIOKEY1", "54", "PL_KEY1"),
            arrayListOf("EMIOKEY2", "55", "PL_KEY2"),
        )
        val definesOutPin = arrayListOf(
            arrayListOf("EMIO1PUL", "56", "1"),
            arrayListOf("EMIO2", "57", "2"),
            arrayListOf("EMIO3", "58", "3"),
            arrayListOf("EMIO4ENA", "59", "4"),
            arrayListOf("EMIOLED1", "60", "PL_LED1"),
            arrayListOf("EMIOLED2", "61", "PL_LED2"),
        )
        val definesIO = arrayListOf(
            arrayListOf("input", "0", ""),
            arrayListOf("output", "1", ""),
        )
        val defines = arrayListOf(
            definesInPin,
            definesOutPin,
            definesIO,
        )
        defines.map { i ->
            cb.addLineBlock()
            i.map { j ->
                cb.addBlock(bf.getDefineBlock(j[0], j[1]))
                cb.addBlock(bf.getRemarkBlock(arrayListOf(j[2])))
            }
        }
        cb.addLineBlock()
        cb.addBlock(bf.getArgDeclareBlock("XGpioPs", "xGpios"))
        fun getSetInPinBlock(in_pin_name: String): BlockHelper.Block {
            return bf.getFunCallBlock(
                method_name = "XGpioPs_SetDirectionPin",
                args = bf.getArgsBlock(
                    arrayListOf(
                        "&xGpios",
                        in_pin_name,
                        "input",
                    ).toBlockArrayList(),
                ),
                if_line = true,
            )
        }

        val mainBlock = bf.getMainFunDefineBlock()
        definesInPin.map {
            mainBlock.wBlocks[0].run {
                addBlock(getSetInPinBlock(it[0]))
            }
        }
        mainBlock.addLineBlock()
        fun getSetOutPinBlock(out_pin_name: String): BlockHelper.Block {
            val cb = bf.getEmptyBlock()
            cb.addBlock(bf.getRemarkBlock())
            cb.addBlock(
                bf.getFunCallBlock(
                    "XGpioPs_SetDirectionPin",
                    bf.getArgsBlock(
                        arrayListOf(
                            "&xGpios",
                            out_pin_name,
                            "output",
                        ).toBlockArrayList(),
                    ),
                    true,
                )
            )
            cb.addBlock(
                bf.getFunCallBlock(
                    "XGpioPs_SetOutputEnablePin",
                    bf.getArgsBlock(
                        arrayListOf(
                            "&xGpios".toBlock(),
                            out_pin_name.toBlock(),
                            "1".toBlock(),
                        ),
                    ),
                    true,
                )
            )
            return cb
        }
        definesOutPin.map {
            mainBlock.wBlocks[0].run {
                addBlock(getSetOutPinBlock(it[0]))
            }
        }
        cb.addBlock(mainBlock)
        val str = cb.getStr()
        str.toPrint()
        val ar = """#include <stdio.h>
#include "platform.h"
#include "xgpiops.h"
#include "xgpiops_hw.h"
#include "sleep.h"

#define EMIOKEY1 54 //PL_KEY1
#define EMIOKEY2 55 //PL_KEY2

#define EMIO1PUL 56 //1
#define EMIO2 57 //2
#define EMIO3 58 //3
#define EMIO4ENA 59 //4
#define EMIOLED1 60 //PL_LED1
#define EMIOLED2 61 //PL_LED2

#define input 0 
#define output 1 

XGpioPs xGpios;
int main() {
	XGpioPs_SetDirectionPin(&xGpios, EMIOKEY1, input);
	XGpioPs_SetDirectionPin(&xGpios, EMIOKEY2, input);
	XGpioPs_SetDirectionPin(&xGpios, EMIO1PUL, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIO1PUL, 1);
	XGpioPs_SetDirectionPin(&xGpios, EMIO2, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIO2, 1);
	XGpioPs_SetDirectionPin(&xGpios, EMIO3, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIO3, 1);
	XGpioPs_SetDirectionPin(&xGpios, EMIO4ENA, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIO4ENA, 1);
	XGpioPs_SetDirectionPin(&xGpios, EMIOLED1, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIOLED1, 1);
	XGpioPs_SetDirectionPin(&xGpios, EMIOLED2, output);
	XGpioPs_SetOutputEnablePin(&xGpios, EMIOLED2, 1);
}
"""
        assert(str == ar)
    }
}