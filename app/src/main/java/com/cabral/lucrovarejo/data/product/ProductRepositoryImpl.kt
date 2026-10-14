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

    override suspend fun getProducts(): List<ProductModel> = withProductFailure {
        remoteDataSource.getProducts().map { it.toModel() }
    }

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
