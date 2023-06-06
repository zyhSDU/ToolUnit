package helper

import helper.uppaal.try2.UppaalHelper
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

internal class UppaalHelperTest {
    private fun testTemplate1(resDir: String) {
        val modelPath = "${resDir}model.xml"
        val queryPath = "${resDir}query.q"
        val verifierPath = UppaalHelper.uppaalPath_4_1_26 // 在Windows中，请使用"verifyta.exe"
        val pb = ProcessBuilder(
            verifierPath,
//            "-smt",
//            "-n","100",
//            "--key", UppaalHelper.licensePath3,
            "-s",
            "-t","0",
            modelPath, queryPath,
        )
        pb.redirectErrorStream(true) // 将错误输出合并到标准输出
        val process = pb.start()

        // 读取和打印验证器的输出
        val br = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (br.readLine().also { line = it } != null) {
            println(line)
        }

        // 获取并打印退出代码
        val exitCode = process.waitFor()
        println("UPPAAL SMC退出，退出代码：$exitCode")
    }

    @Test
    fun t1() {
        val resDir = "D:\\Users\\zyh\\GitProject\\ToolUnit\\src\\main\\resources\\uppaal\\model\\jobshop_smc\\"
        testTemplate1(resDir)
    }
}