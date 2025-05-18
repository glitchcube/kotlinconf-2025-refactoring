package com.example.wip

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.DynamicTestInvocationContext
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.opentest4j.AssertionFailedError
import org.opentest4j.TestAbortedException
import java.lang.reflect.Method

@Suppress("unused")
class WorkInProgressExtension(
    allowWorkInProgressByDefault: Boolean = true,
    private var allowSomeWorkInProgressToSucceed: Boolean = false
) :
    BeforeAllCallback,
    BeforeEachCallback,
    AfterAllCallback,
    InvocationInterceptor
{
    private var isWorkInProgress: Boolean = allowWorkInProgressByDefault
    private var failure: Throwable? = null

    private val hasFailure: Boolean get() = failure != null

    fun allowWorkInProgressIf(flag: Boolean) {
        isWorkInProgress = flag
    }
    
    // Annotation is at class level
    override fun beforeAll(context: ExtensionContext) {
        allowSomeWorkInProgressToSucceed = true
    }
    
    override fun beforeEach(context: ExtensionContext) {
        allowSomeWorkInProgressToSucceed = allowSomeWorkInProgressToSucceed ||
            context.element.map { it.getAnnotation(WorkInProgress::class.java).flaky }
                .orElse(false)
    }
    
    override fun afterAll(context: ExtensionContext) {
        if (allowSomeWorkInProgressToSucceed) {
            context.executionException.ifPresent(this::ignoreFailure)
            if (!hasFailure) {
                unexpectedSuccess()
            }
        }
    }

    override fun interceptTestTemplateMethod(
        invocation: Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        runTestAllowingWorkInProgress {
            super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext)
        }
    }

    override fun interceptTestMethod(
        invocation: Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        runTestAllowingWorkInProgress {
            super.interceptTestMethod(invocation, invocationContext, extensionContext)
        }
    }

    override fun interceptDynamicTest(
        invocation: Invocation<Void>,
        invocationContext: DynamicTestInvocationContext,
        extensionContext: ExtensionContext
    ) {
        runTestAllowingWorkInProgress {
            super.interceptDynamicTest(invocation, invocationContext, extensionContext)
        }
    }

    override fun <T : Any?> interceptTestFactoryMethod(
        invocation: Invocation<T>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ): T {
        // Ignore failures but allow successful calls to the factory.  Any tests that are created
        // by the test factory method might be WorkInProgress.
        try {
            return super.interceptTestFactoryMethod(invocation, invocationContext, extensionContext)
        } catch (t: Throwable) {
            ignoreFailure(t)
        }
    }

    private inline fun runTestAllowingWorkInProgress(runIt: () -> Unit) {
        try {
            runIt()
        } catch (t: Throwable) {
            ignoreFailure(t)
        }

        if (!allowSomeWorkInProgressToSucceed) {
            unexpectedSuccess()
        }
    }

    private fun unexpectedSuccess() {
        if (isWorkInProgress) {
            throw AssertionFailedError("work-in-progress test succeeded")
        }
    }

    private fun ignoreFailure(throwable: Throwable): Nothing {
        if (isWorkInProgress) {
            failure = throwable
            throw TestAbortedException("ignoring failure of work-in-progress test", throwable)
        } else {
            throw throwable
        }
    }
}
