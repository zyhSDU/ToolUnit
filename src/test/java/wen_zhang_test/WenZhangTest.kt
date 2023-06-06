package wen_zhang_test
import kotlin.random.Random
object WenZhangTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = "中国共产党党课内容"
        println(generateReport(input))
    }

    fun generateReport(partyCourseContent: String): String {
        val keywords = listOf("党的历史", "党的理论", "党的宗旨", "党的纪律", "党的组织原则", "党的领导", "党的建设", "党的任务", "党的作用", "党的发展")
        val sampleSentences = listOf(
            "学习了%s，我深感自己的责任重大。",
            "通过学习%s，我对党的事业有了更加清晰的认识。",
            "%s让我明白了党的伟大历程。",
            "学习%s，使我对党的精神内涵有了更深刻的理解。",
            "我会在工作中发扬%s的精神，努力为党的事业做出贡献。"
        )

        val reportBuilder = StringBuilder()
        reportBuilder.append("经过认真学习党课，我对中国共产党有了更深入的了解，明白了我们党的光辉历程、伟大事业以及党的基本理论、基本路线。在此，我将对学习党课内容的感悟进行汇报。\n\n")

        while (reportBuilder.length < 1500) {
            val keyword = keywords[Random.nextInt(keywords.size)]
            val sampleSentence = sampleSentences[Random.nextInt(sampleSentences.size)]
            reportBuilder.append(String.format(sampleSentence, keyword))
            reportBuilder.append("\n\n")
        }

        return reportBuilder.toString()
    }

}