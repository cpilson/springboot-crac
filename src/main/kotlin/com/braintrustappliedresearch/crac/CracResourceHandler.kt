package com.braintrustappliedresearch.crac

import org.crac.Context
import org.crac.Core
import org.crac.Resource
import org.springframework.stereotype.Component

@Component
class CracResourceHandler : Resource {
    init {
        Core.getGlobalContext().register(this)
    }

    override fun beforeCheckpoint(context: Context<out Resource>) {
        println("Preparing for checkpoint")
    }

    override fun afterRestore(context: Context<out Resource>) {
        println("Restored from checkpoint")
    }
}