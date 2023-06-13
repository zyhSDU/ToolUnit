package helper.scxml.scxml2.t5_traffic_train

object EnvObjHelper {
    fun getTrafficEnvObj1() = EnvHelper.Env(
        envStateConstraintLHM = StrategyTripleHelper.dEnvStateConstraintLHM,
        envEventLHM = helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.getBaseEnvEventLHM().also {
            it["Train"] = linkedMapOf(
                "train_go" to 10.0,
                "train_wait" to 1.0
            )
        },
        getIRenEventSelectorFun = {
            StrategyTripleHelper.stateRenEventSelector1
        }
    )
}