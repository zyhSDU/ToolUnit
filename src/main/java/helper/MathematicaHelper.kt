package helper

import com.wolfram.jlink.KernelLink
import com.wolfram.jlink.MathLinkException
import com.wolfram.jlink.MathLinkFactory
import helper.base.ScannerHelper

object MathematicaHelper {
    private const val mathkernelPath = "D:\\Program Files\\Wolfram Research\\Mathematica\\12.0\\mathkernel"

    private var kernelLink: KernelLink? = null

    fun getKernelLink(): KernelLink? {
        if (kernelLink == null) {
            try {
                kernelLink = MathLinkFactory.createKernelLink("-linkmode launch -linkname '$mathkernelPath'")
                kernelLink?.discardAnswer()
            } catch (e: MathLinkException) {
                println("An error occurred connecting to the kernel.")
                if (kernelLink != null) kernelLink!!.close()
                return kernelLink
            }
        }
        return kernelLink
    }

    var isPrepare = false
    fun prepare() {
        if (isPrepare) return
        kernelLink?.run {
            evaluateToInputForm("Needs[\"" + KernelLink.PACKAGE_CONTEXT + "\"]", 0)
            evaluateToInputForm("ConnectToFrontEnd[]", 0)
            isPrepare = true
        }
    }

    fun evaluate(string: String): String {
        if (!isPrepare) {
            prepare()
        }
        if (string.isBlank() || string.isEmpty()) {
            return ""
        }
        getKernelLink()?.run {
            return evaluateToInputForm(string.trim(), 0)
        }
        return ""
    }

    @JvmStatic
    fun main(args: Array<String>) {
        ScannerHelper.map {
            evaluate(it).run {
                println(this)
            }
        }
    }
}