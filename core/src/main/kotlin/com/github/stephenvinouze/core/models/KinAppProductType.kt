package com.github.stephenvinouze.core.models

import com.github.stephenvinouze.core.managers.KinAppManager

/**
 * Created by stephenvinouze on 10/05/2017.
 */
enum class KinAppProductType(val value: String) {
    INAPP(KinAppManager.INAPP_TYPE), SUBSCRIPTION(KinAppManager.SUBS_TYPE)
}