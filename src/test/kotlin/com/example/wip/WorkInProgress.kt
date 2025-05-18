package com.example.wip

import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.Inherited

@Suppress("unused")
@ExtendWith(WorkInProgressExtension::class)
@Inherited
annotation class WorkInProgress(val flaky: Boolean = false)
