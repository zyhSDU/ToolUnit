package helper.uppaal.try1

import com.uppaal.engine.*
import com.uppaal.model.core2.*
import com.uppaal.model.io2.UXMLResolver
import com.uppaal.model.io2.XMLReader
import com.uppaal.model.system.UppaalSystem
import com.uppaal.model.system.concrete.ConcreteState
import com.uppaal.model.system.concrete.ConcreteTrace
import com.uppaal.model.system.concrete.ConcreteTransitionRecord
import com.uppaal.model.system.symbolic.SymbolicState
import com.uppaal.model.system.symbolic.SymbolicTrace
import com.uppaal.model.system.symbolic.SymbolicTransition
import helper.base.FileHelper
import helper.base.TimeHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.math.BigDecimal
import java.net.MalformedURLException
import java.net.URL
import kotlin.system.exitProcess

object UppaalHelper {
    /**
     * Sets a label on a location.
     *
     * @param l     the location on which the label is going to be attached
     * @param kind  a kind of the label
     * @param value the label value (either boolean or String)
     * @param x     the x coordinate of the label
     * @param y     the y coordinate of the label
     */
    private fun setLabel(l: Location, kind: LKind, value: Any?, x: Int, y: Int) {
        l.setProperty(kind.value, value)
        val p = l.getProperty(kind.value)
        p.setProperty("x", x)
        p.setProperty("y", y)
    }

    /**
     * Adds a location to a template.
     *
     * @param t       the template
     * @param name    a name for the new location
     * @param exprate an expression for an exponential rate
     * @param x       the x coordinate of the location
     * @param y       the y coordinate of the location
     * @return the new location instance
     */
    private fun addLocation(
        t: Template, name: String?, exprate: String?,
        x: Int, y: Int
    ): Location {
        val l = t.createLocation()
        t.insert(l, null)
        l.setProperty("x", x)
        l.setProperty("y", y)
        if (name != null) setLabel(l, LKind.name_, name, x, y - 28)
        if (exprate != null) setLabel(l, LKind.exponentialrate, exprate, x, y - 28 - 12)
        return l
    }

    /**
     * Sets a label on an edge.
     *
     * @param e     the edge
     * @param kind  the kind of the label
     * @param value the content of the label
     * @param x     the x coordinate of the label
     * @param y     the y coordinate of the label
     */
    private fun setLabel(e: Edge, kind: EKind, value: String?, x: Int, y: Int) {
        e.setProperty(kind.name, value)
        val p = e.getProperty(kind.name)
        p.setProperty("x", x)
        p.setProperty("y", y)
    }

    /**
     * Adds an edge to the template
     *
     * @param t      the template where the edge belongs
     * @param source the source location
     * @param target the target location
     * @param guard  guard expression
     * @param sync   synchronization expression
     * @param update update expression
     * @return
     */
    private fun addEdge(
        t: Template, source: Location, target: Location,
        guard: String?, sync: String?, update: String?
    ): Edge {
        val e = t.createEdge()
        t.insert(e, null)
        e.source = source
        e.target = target
        val x = (source.x + target.x) / 2
        val y = (source.y + target.y) / 2
        if (guard != null) {
            setLabel(e, EKind.guard, guard, x - 15, y - 28)
        }
        if (sync != null) {
            setLabel(e, EKind.synchronisation, sync, x - 15, y - 14)
        }
        if (update != null) {
            setLabel(e, EKind.assignment, update, x - 15, y)
        }
        return e
    }

    class TA(
        val name: String,
        val init: (Template) -> Unit
    )

    fun Document.addTATemplate(ta: TA): Template {
        val t = createTemplate()
        insert(t, null)
        t.setProperty("name", ta.name)
        ta.init(t)
        return t
    }

    fun Document.addTAInstances(taHashSet: LinkedHashMap<String, String>) {
        val sb = StringBuilder()
        taHashSet.map { (k, v) ->
            sb.append("\n${k}=${v}();")
        }
        sb.append("\n")
        sb.append("system")
        taHashSet.keys.withIndex().map { (id, k) ->
            if (id != 0) {
                sb.append(",")
            }
            sb.append(" $k")
        }
        sb.append(";")
        sb.append("\n")

        setProperty("system", sb.toString())
    }

    @Throws(IOException::class)
    fun loadModel(location: String?): Document {
        return try {
            // try URL scheme (useful to fetch from Internet):
            PrototypeDocument().load(URL(location))
        } catch (ex: MalformedURLException) {
//            ex.printStackTrace()
            // not URL, retry as it were a local filepath:
            PrototypeDocument().load(URL("file", null, location))
        }
    }

    @Throws(EngineException::class, IOException::class)
    fun connectToEngine(): Engine {
        val os = System.getProperty("os.name")
        println(os)
        val here = System.getProperty("user.dir")
        var path: String? = null
        //        if ("Linux".equals(os)) {
//            path = here + "/bin-Linux/server";
//        } else if ("Mac OS X".equals(os)) {
//            path = here + "/bin-Darwin/server";
//        } else
        if ("Windows" == os || "Windows 10" == os) {
            path = "$here\\lib\\uppaal\\uppaal64-4.1.26\\bin-Windows\\server.exe"
        } else {
            System.err.println("Unknown operating system.")
            exitProcess(1)
        }
        val engine = Engine()
        engine.setServerPath(path)
        engine.setServerHost("localhost")
        engine.setConnectionMode(EngineStub.BOTH)
        engine.connect()
        return engine
    }

    @Throws(EngineException::class, IOException::class)
    fun compile(engine: Engine, doc: Document?): UppaalSystem {
        // compile the model into system:
        val problems = ArrayList<Problem>()
        val sys = engine.getSystem(doc, problems)
        if (!problems.isEmpty()) {
            var fatal = false
            println("There are problems with the document:")
            for (p in problems) {
                println(p.toString())
                if ("warning" != p.type) { // ignore warnings
                    fatal = true
                }
            }
            if (fatal) {
                System.exit(1)
            }
        }
        return sys
    }

    @Throws(EngineException::class, IOException::class, CannotEvaluateException::class)
    fun symbolicSimulation(engine: Engine, sys: UppaalSystem): SymbolicTrace {
        val trace = SymbolicTrace()
        // compute the initial state:
        var state = engine.getInitialState(sys)
        // add the initial transition to the trace:
        trace.add(SymbolicTransition(null, null, state))
        while (state != null) {
            print(sys, state)
            // compute the successors (including "deadlock"):
            val trans = engine.getTransitions(sys, state)
            // select a random transition:
            val n = Math.floor(Math.random() * trans.size).toInt()
            val tr = trans[n]
            // check the number of edges involved:
            if (tr.size == 0) {
                // no edges, something special (like "deadlock"):
                print("tr.edgeDescription:${tr.edgeDescription}")
            } else {
                // one or more edges involved, print them:
                var first = true
                for (e in tr.edges) {
                    if (first) first = false else print(", ")
                    print(
                        e.processName + ": "
                                + e.edge.source.getPropertyValue("name")
                                + " \u2192 "
                                + e.edge.target.getPropertyValue("name")
                    )
                }
            }
            println()
            // jump to a successor state (null in case of deadlock):
            state = tr.target
            // if successfull, add the transition to the trace:
            if (state != null) trace.add(tr)
        }
        return trace
    }

    @Throws(EngineException::class, IOException::class, CannotEvaluateException::class)
    fun concreteSimulation(engine: Engine, sys: UppaalSystem): ConcreteTrace {
        val trace = ConcreteTrace()
        // compute the initial state:
        var state = engine.getInitialState(sys)
//        // add the initial transition to the trace:
//        trace.add(ConcreteTransition(null, null, state))
//        while (state != null) {
//            print(sys, state)
//            // compute the successors (including "deadlock"):
//            val trans = engine.getTransitions(sys, state)
//            // select a random transition:
//            val n = Math.floor(Math.random() * trans.size).toInt()
//            val tr = trans[n]
//            // check the number of edges involved:
//            if (tr.size == 0) {
//                // no edges, something special (like "deadlock"):
//                print(tr.edgeDescription)
//            } else {
//                // one or more edges involved, print them:
//                var first = true
//                for (e in tr.edges) {
//                    if (first) first = false else print(", ")
//                    print(
//                        e.processName + ": "
//                                + e.edge.source.getPropertyValue("name")
//                                + " \u2192 "
//                                + e.edge.target.getPropertyValue("name")
//                    )
//                }
//            }
//            println()
//            // jump to a successor state (null in case of deadlock):
//            state = tr.target
//            // if successfull, add the transition to the trace:
//            if (state != null) trace.add(tr)
//        }
        return trace
    }

    @Throws(IOException::class)
    fun saveXTRFile(trace: SymbolicTrace?, file: String) {
        /* BNF for the XTR format just in case
		   (it may change, thus don't rely on it)
		   <XTRFomat>  := <state> ( <state> <transition> ".\n" )* ".\n"
		   <state>     := <locations> ".\n" <polyhedron> ".\n" <variables> ".\n"
		   <locations> := ( <locationId> "\n" )*
		   <polyhedron> := ( <constraint> ".\n" )*
		   <constraint> := <clockId> "\n" clockId "\n" bound "\n"
		   <variables> := ( <varValue> "\n" )*
		   <transition> := ( <processId> <edgeId> )* ".\n"
		*/
        val out = FileWriter(file)
        val it: Iterator<SymbolicTransition> = trace!!.iterator()
        it.next().target.writeXTRFormat(out)
        while (it.hasNext()) {
            it.next().writeXTRFormat(out)
        }
        out.write(".\n")
        out.close()
    }

    val zero = BigDecimal.valueOf(0)

    fun print(
        sys: UppaalSystem,
        s: SymbolicState,
        sb: StringBuilder = StringBuilder(),
        ifPrint: Boolean = true,
    ): StringBuilder {
        sb.append("(")
        var first = true
        for (l in s.locations) {
            if (first) first = false else sb.append(", ")
            sb.append(l.name)
        }
        val `val` = s.variableValues
        for (i in 0 until sys.noOfVariables) {
            sb.append(", ")
            sb.append(sys.getVariableName(i) + "=" + `val`[i])
        }
        val constraints: List<String> = ArrayList()
        s.polyhedron.getAllConstraints(constraints)
        for (cs in constraints) {
            sb.append(", ")
            sb.append(cs)
        }
        sb.append(")\n")
        if (ifPrint) {
            println(sb.toString())
        }
        return sb
    }

    fun print(sys: UppaalSystem, s: ConcreteState) {
        print("(")
        var first = true
        for (l in s.locations) {
            if (first) first = false else print(", ")
            print(l.name)
        }
        val limit = s.invariant
        if (!limit.isUnbounded) {
            if (limit.isStrict) print(", limit<") else print(", limit≤")
            print(limit.doubleValue)
        }
        val `val` = s.cVariables
        val vars = sys.noOfVariables
        for (i in 0 until vars) {
            print(", ")
            print(sys.getVariableName(i) + "=" + `val`[i].getValue(zero))
        }
        for (i in 0 until sys.noOfClocks) {
            print(", ")
            print(sys.getClockName(i) + "=" + `val`[i + vars].getValue(zero))
            print(", ")
            print(sys.getClockName(i) + "'=" + `val`[i + vars].rate)
        }
        println(")")
    }

    fun printSymbolicTrace(
        sys: UppaalSystem,
        trace: SymbolicTrace?,
        sb: StringBuilder = StringBuilder(),
        ifPrint: Boolean = true,
    ): StringBuilder {
        if (trace == null) {
            sb.append("(null trace)\n")
            return sb
        }
        val it: Iterator<SymbolicTransition> = trace.iterator()
        print(sys, it.next().target, ifPrint = false).apply {
            sb.append(toString())
        }
        while (it.hasNext()) {
            val tr = it.next()
            if (tr.size == 0) {
                // no edges, something special (like "deadlock" or initial state):
                sb.append(tr.edgeDescription + "\n")
            } else {
                // one or more edges involved, print them:
                var first = true
                for (e in tr.edges) {
                    if (first) first = false else sb.append(", ")
                    sb.append(
                        e.processName + ": "
                                + e.edge.source.getPropertyValue("name")
                                + " \u2192 "
                                + e.edge.target.getPropertyValue("name")
                    )
                }
            }
            sb.append("\n")

            print(sys, tr.target, ifPrint = false).apply {
                sb.append(toString())
            }
        }
        sb.append("\n")
        if (ifPrint) {
            println(sb.toString())
        }
        return sb
    }

    fun printConcreteTrace(sys: UppaalSystem, trace: ConcreteTrace?) {
        if (trace == null) {
            println("(null trace)")
            return
        }
        val it: Iterator<ConcreteTransitionRecord> = trace.iterator()
        print(sys, it.next().target)
        while (it.hasNext()) {
            val tr = it.next()
            if (tr.size == 0) {
                // no edges, something special (like "deadlock" or initial state):
                println(tr.transitionDescription)
            } else {
                // one or more edges involved, print them:
                var first = true
                for (e in tr.edges) {
                    if (first) first = false else print(", ")
                    print(
                        e.processName + ": "
                                + e.edge.source.getPropertyValue("name")
                                + " \u2192 "
                                + e.edge.target.getPropertyValue("name")
                    )
                }
            }
            println()
            print(sys, tr.target)
        }
        println()
    }

    fun print(data: QueryData) {
        for (title in data.dataTitles) {
            val plot = data.getData(title)
            println(
                "Plot \"" + plot.getTitle() +
                        "\" showing \"" + plot.yLabel +
                        "\" over \"" + plot.xLabel + "\""
            )
            for (traj in plot) {
                print("Trajectory " + traj.title + ":")
                for (p in traj) print(" (" + p.x + "," + p.y + ")")
                println()
            }
        }
    }

    /**
     * Valid kinds of labels on locations.
     */
    enum class LKind(val value: String) {
        name_("name"),
        init_("init"),
        urgent("urgent"),
        committed("committed"),
        invariant("invariant"),
        exponentialrate("exponentialrate"),
        comments("comments"),
    }

    /**
     * Valid kinds of labels on edges.
     */
    enum class EKind {
        select, guard, synchronisation, assignment, comments
    }

    fun testTry(init: () -> Unit) {
        try {
            init()
        } catch (ex: CannotEvaluateException) {
            System.out.flush()
            ex.printStackTrace(System.err)
            exitProcess(1)
        } catch (ex: EngineException) {
            System.out.flush()
            ex.printStackTrace(System.err)
            exitProcess(1)
        } catch (ex: IOException) {
            System.out.flush()
            ex.printStackTrace(System.err)
            exitProcess(1)
        } catch (ex: ServerException) {
            println(1111111)
            System.out.flush()
            ex.printStackTrace(System.err)
            exitProcess(1)
        } catch (ex: Exception) {
            System.out.flush()
            ex.printStackTrace(System.err)
            exitProcess(1)
        }
    }

    fun createSampleModel(xmlPath: String, init: (String) -> Document) {
        val doc = init(xmlPath)
        FileHelper.createFileIfNotExists(File(xmlPath))
        doc.save(xmlPath)
        File(xmlPath).readText().replace(
            "http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd",
            "lib/uppaal/flat-1_2.dtd"
        ).apply {
            File(xmlPath).writeText(this)
        }
    }

    const val options = "--search-order 0 --diagnostic 0"

    class TraceAndQF {
        var strace: SymbolicTrace? = null
        var ctrace: ConcreteTrace? = null

        // see "verifyta --help" for the description of options
        var qf: QueryFeedback? = object : QueryFeedback {
            override fun setProgressAvail(availability: Boolean) {}
            override fun setProgress(
                load: Int,
                vm: Long,
                rss: Long,
                cached: Long,
                avail: Long,
                swap: Long,
                swapfree: Long,
                user: Long,
                sys: Long,
                timestamp: Long
            ) {
            }

            override fun setSystemInfo(vmsize: Long, physsize: Long, swapsize: Long) {}
            override fun setLength(length: Int) {}
            override fun setCurrent(pos: Int) {}
            override fun setTrace(
                result: Char,
                feedback: String?,
                trace: SymbolicTrace?,
                queryVerificationResult: QueryResult?
            ) {
                strace = trace
            }

            override fun setTrace(
                result: Char,
                feedback: String?,
                trace: ConcreteTrace?,
                queryVerificationResult: QueryResult?
            ) {
                ctrace = trace
            }

            override fun setFeedback(feedback: String?) {
                if (feedback != null && feedback.isNotEmpty()) {
                    println("Feedback: $feedback")
                }
            }

            override fun appendText(s: String?) {
                if (s != null && s.isNotEmpty()) {
                    println("Append: $s")
                }
            }

            override fun setResultText(s: String?) {
                if (s != null && s.isNotEmpty()) {
                    println("Result: $s")
                }
            }
        }
    }

    data class XEDS(
        val xmlPath: String,
        val engine: Engine,
        val doc: Document,
        val sys: UppaalSystem,
    ) {

    }

    fun doSomeInUppaalSystem(modelFileName: String, doSome: (XEDS) -> Unit) {
        testTry {
            XMLReader.setXMLResolver(UXMLResolver())
            val engine = connectToEngine()
            val doc = loadModel(modelFileName)
            val sys = compile(engine, doc)
            doSome(XEDS(modelFileName, engine, doc, sys))
            engine.disconnect()
        }
    }

    fun turnXTRToTXT(modelFileName: String, traceName: String) {
        val xtrPath = "$traceName.xtr"
        val txtPath = "$traceName.txt"

        doSomeInUppaalSystem(modelFileName) { (_, _, _, sys) ->
            val parser = Parser(FileInputStream(xtrPath))
            val strace = parser.parseXTRTrace(sys)
            val stringBuilder = printSymbolicTrace(sys, strace, ifPrint = false)
            File(txtPath).writeText(stringBuilder.toString())
        }
    }

    //doSomeInUppaalSystem状态下才能调用
    fun turnXTRsToTXTs(
        xeds: XEDS,
        sourceFileName: String,
        outputFileName: String = sourceFileName,
    ) {
        FileHelper.createDirIfNotExists(outputFileName)
        val sys = xeds.sys
        File(
            sourceFileName
        ).listFiles()?.mapNotNull {
            it
        }?.filter {
            it.name.endsWith(".xtr")
        }?.map {
            println("turnXTRsToTXTs,file,${it}")
            val parser = Parser(FileInputStream(it.absolutePath))
            val strace = parser.parseXTRTrace(sys)
            val stringBuilder = printSymbolicTrace(
                sys,
                strace,
                ifPrint = false,
            )
            File(
                "${outputFileName}${File.separator}${
                    it.name.replace(
                        ".xtr",
                        ""
                    )
                }.txt"
            ).writeText(stringBuilder.toString())
        }
    }

    data class Strategy(
        val states: ArrayList<String>,
        val variables: ArrayList<String>,
        val clockValues: ArrayList<Double>,
    ) {
    }

    object Tests {
        val path = "uppaalModels"

        object Test1 {
            fun createTA1(): TA {
                return TA("Experiment") {
                    // the template has initial location:
                    val l0 = addLocation(it, "L0", "1", 0, 0)
                    l0.setProperty("init", true)
                    // add another location to the right:
                    val l1 = addLocation(it, "L1", null, 150, 0)
                    setLabel(l1, LKind.invariant, "x<=10", l1.x - 7, l1.y + 10)
                    // add another location below to the right:
                    val l2 = addLocation(it, "L2", null, 150, 150)
                    setLabel(l2, LKind.invariant, "y<=20", l2.x - 7, l2.y + 10)
                    // add another location below:
                    val l3 = addLocation(it, "L3", "1", 0, 150)
                    // add another location below:
                    val lf = addLocation(it, "Final", null, -150, 150)
                    // create an edge L0->L1 with an update
                    val e = addEdge(it, l0, l1, "v<2", null, "v=1,\nx=0")
                    e.setProperty(EKind.comments.name, "Execute L0->L1 with v=1")
                    // create some more edges:
                    addEdge(it, l1, l2, "x>=5", null, "v=2,\ny=0")
                    addEdge(it, l2, l3, "y>=10", null, "v=3,\nz=0")
                    addEdge(it, l3, l0, null, null, "v=4")
                    addEdge(it, l3, lf, null, null, "v=5")
                }
            }

            fun createSampleModel(xmlPath: String) {
                createSampleModel(xmlPath) {
                    // create a new Uppaal model with default properties:
                    val doc = Document(PrototypeDocument())
                    // add global variables:
                    doc.setProperty("declaration", "int v;\n\nclock x,y,z;")
                    // add a TA template:
                    val ta = createTA1()
                    doc.addTATemplate(ta)
                    val taHashSet = java.util.LinkedHashMap<String, String>().apply {
                        this["Exp1"] = ta.name
                        this["Exp2"] = ta.name
                    }
                    // add system declaration:
                    doc.addTAInstances(taHashSet)
                    doc
                }
            }

            fun symbolicSimulationTest(
                xeds: XEDS,
                xtrPathFile: String,
                times: Int = 1,
            ) {
                (0 until times).map {
                    val xtrPath = xtrPathFile + "symbolicSimulation${TimeHelper.now(TimeHelper.TimePattern.p3)}.xtr"
                    FileHelper.createFileIfNotExists(xtrPath)
                    var strace: SymbolicTrace? = null
                    var ctrace: ConcreteTrace? = null

                    val engine = xeds.engine
                    val sys = xeds.sys
                    // perform a random symbolic simulation and get a trace:
                    println("===== Random symbolic simulation ${it}=====")
                    strace = symbolicSimulation(engine, sys)

                    // save the trace to an XTR file:
                    println("===== Trace saving and loading =====")
                    saveXTRFile(strace, xtrPath)

                    //deadlock去哪了？
                }
            }

            val test2 = { engine: Engine, doc: Document, sys: UppaalSystem ->
                // simple model-checking:
                val query = Query("E<> Exp1.Final", "can Exp1 finish?")
                println("===== Symbolic check: " + query.formula + " =====")
                val traceAndQF = TraceAndQF()
                println("Result: " + engine.query(sys, options, query, traceAndQF.qf))
                printSymbolicTrace(sys, traceAndQF.strace)
            }

            fun concreteSimulationTest(xeds: XEDS, xtrPathFile: String) {
                val xmlPath = xeds.xmlPath
                val engine = xeds.engine
                val doc = xeds.doc
                val sys = xeds.sys
                val xtrPath = "" +
                        "${xtrPathFile}${File.separator}" +
                        "concreteSimulation${TimeHelper.now(TimeHelper.TimePattern.p3)}.xtr"

                var ctrace: ConcreteTrace? = null

                // perform a random concrete simulation and get a trace:
                println("===== Random concrete simulation =====")
                ctrace = concreteSimulation(engine, sys)

//                // save the trace to an XTR file:
//                println("===== Trace saving and loading =====")
//                saveXTRFile(ctrace, xtrPath)
//
//                val parser = Parser(FileInputStream(xtrPath))
//                // parse a trace from an XTR file:
//                ctrace = parser.parseXTRTrace(sys)
//                printTrace(sys, ctrace)
                //deadlock去哪了？
            }

            fun main0(path: String) {
                val xmlPath = "${path}${File.separator}ModelDemo.xml"
                createSampleModel(xmlPath)
            }

            fun main1(path: String) {
                val xmlPath = "${path}${File.separator}ModelDemo.xml"

                val time = TimeHelper.now(TimeHelper.TimePattern.p2)

                val xtrPathFile = "" +
                        "${path}${File.separator}" +
                        "xtr${time}${File.separator}"
                val txtPathFile = "" +
                        "${path}${File.separator}" +
                        "txt${time}${File.separator}"
                doSomeInUppaalSystem(xmlPath) { sedu ->
                    symbolicSimulationTest(sedu, xtrPathFile, 2)
                    turnXTRsToTXTs(sedu, xtrPathFile, txtPathFile)
                }
            }

            fun main2(path: String) {
                val xmlPath = "${path}${File.separator}ModelDemo.xml"

                val time = TimeHelper.now(TimeHelper.TimePattern.p2)

                val xtrPathFile = "" +
                        "${path}${File.separator}" +
                        "xtr${time}${File.separator}"
                val txtPathFile = "" +
                        "${path}${File.separator}" +
                        "txt${time}${File.separator}"
                doSomeInUppaalSystem(xmlPath) { sedu ->
                    concreteSimulationTest(sedu, xtrPathFile)
//                    turnXTRsToTXTs(sedu, xtrPathFile, txtPathFile)
                }
            }

            @JvmStatic
            fun main(args: Array<String>) {
                val path = "$path${File.separator}mode1"
//                main0(path)
                main1(path)
//                main2(path)
            }
        }
    }
}