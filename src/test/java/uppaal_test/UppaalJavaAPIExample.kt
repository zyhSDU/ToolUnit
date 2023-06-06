//package uppaal_test
//
//import com.uppaal.engine.Engine
//import com.uppaal.engine.EngineException
//import com.uppaal.model.system.SystemState
//import com.uppaal.plugin.PluginManager
//import java.io.File
//
//fun main() {
//    try {
//        val engine = Engine();
//        val pluginManager = PluginManager.getPluginManager();
//
//        // 加载模型文档
//        val modelFile = File("model.xml");
//        val document = engine.getVerifier().loadDocument(modelFile);
//
//        // 进行验证
//        val system = engine.getVerifier().verify(document);
//
//        // 查询满足条件的状态集合
//        val results = mutableListOf<SystemState>()
//        for (state in system.getReachableStates()) {
//            if (state.getLocation().getName().startsWith("target")) {
//                results.add(state)
//            }
//        }
//
//        // 输出结果
//        println("Number of results found: " + results.size)
//        for (result in results) {
//            println("Found result: " + result.getLocation().getName())
//            for (edge in result.getOutgoingEdges()) {
//                println("\t-> " + edge.getTarget().getLocation().getName())
//            }
//        }
//
//        // 关闭 Uppaal 引擎对象以释放资源
//        engine.getVerifier().close()
//    } catch (e: EngineException) {
//        e.printStackTrace();
//    }
//}
