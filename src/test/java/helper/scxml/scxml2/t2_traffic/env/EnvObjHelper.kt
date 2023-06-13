package helper.scxml.scxml2.t2_traffic.env

import helper.scxml.scxml2.t2_traffic.env.EnvHelper.Env
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper

object EnvObjHelper {
    fun getEnvObj1(): Env {
        return Env(
            envStateConstraintLHM = StrategyTripleHelper.rEnvStateConstraintLHM,
            envEventLHM = StrategyTripleHelper.envEventLHM1,
            getIRenEventSelectorFun = {
                StrategyTripleHelper.stateRenEventSelector1
            },
        )
    }

    fun getEnvObj2(): Env {
        return Env(
            envStateConstraintLHM = StrategyTripleHelper.dEnvStateConstraintLHM,
            envEventLHM = StrategyTripleHelper.envEventLHM1,
            getIRenEventSelectorFun = {
                StrategyTripleHelper.stateRenEventSelector1
            },
        )
    }
}