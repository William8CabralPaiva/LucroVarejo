package com.cabral.lucrovarejo.data.di

import com.cabral.lucrovarejo.data.product.di.ProductModule
import com.cabral.lucrovarejo.data.purchase.di.PurchaseModule
import dagger.Binds
import dagger.Module
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModuleBindingTest {
    @Test
    fun productModuleDeclaresSingletonBindings() {
        assertModuleBindings(ProductModule::class.java, 2)
    }

    @Test
    fun purchaseModuleDeclaresSingletonBindings() {
        assertModuleBindings(PurchaseModule::class.java, 5)
    }

    private fun assertModuleBindings(module: Class<*>, expectedBindings: Int) {
        assertNotNull(module.getAnnotation(Module::class.java))
        val bindings = module.declaredMethods.filter { it.isAnnotationPresent(Binds::class.java) }
        assertEquals(expectedBindings, bindings.size)
        assertTrue(bindings.all { method ->
            method.returnType.isAssignableFrom(method.parameterTypes.single())
        })
    }
}
