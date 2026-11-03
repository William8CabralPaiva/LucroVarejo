package com.cabral.lucrovarejo.data.product

import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.model.toModel
import com.cabral.lucrovarejo.data.product.model.toRequest
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    override suspend fun createProduct(product: ProductModel): ProductModel {
        val request = product.toRequest()
        val documentId = withProductFailure {
            remoteDataSource.createProduct(request)
        }
        return request.toModel(documentId)
    }

    override suspend fun updateProduct(product: ProductModel): ProductModel {
        val key = requireNotNull(product.key) { "Product key is required to update" }
        val request = product.toRequest()
        withProductFailure { remoteDataSource.updateProduct(key, request) }
        return request.toModel(key)
    }

    override suspend fun deleteProduct(key: String) {
        withProductFailure { remoteDataSource.deleteProduct(key) }
    }

    override fun observeProducts(): Flow<List<ProductModel>> =
        remoteDataSource.observeProducts()
            .map { records -> records.map { it.toModel() } }
            .catch { failure -> throw failure.toProductFailure() }

    private suspend fun <T> withProductFailure(
        operation: suspend () -> T
    ): T = try {
        operation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: ProductFailure) {
        throw failure
    } catch (failure: FirebaseFirestoreException) {
        val reason = when (failure.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                ProductFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                ProductFailure.Reason.NETWORK
            else -> ProductFailure.Reason.UNKNOWN
        }
        throw ProductFailure(reason, failure)
    } catch (failure: Exception) {
        throw ProductFailure(ProductFailure.Reason.UNKNOWN, failure)
    }
}

private fun Throwable.toProductFailure(): ProductFailure = when (this) {
    is ProductFailure -> this
    is FirebaseFirestoreException -> {
        val reason = when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                ProductFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE -> ProductFailure.Reason.NETWORK
            else -> ProductFailure.Reason.UNKNOWN
        }
        ProductFailure(reason, this)
    }
    else -> ProductFailure(ProductFailure.Reason.UNKNOWN, this)
}
