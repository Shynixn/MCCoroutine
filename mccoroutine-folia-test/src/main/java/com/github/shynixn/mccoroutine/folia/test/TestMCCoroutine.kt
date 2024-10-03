package com.github.shynixn.mccoroutine.folia.test

import com.github.shynixn.mccoroutine.folia.test.impl.TestMCCoroutineImpl

interface TestMCCoroutine {
    companion object {
        /**
         * The driver to load the test implementation of MCCoroutine.
         * Useful for UnitTests.
         */
        val Driver = TestMCCoroutineImpl::class.java.name
    }
}
