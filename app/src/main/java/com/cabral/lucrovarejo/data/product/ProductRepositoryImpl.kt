package com.cabral.lucrovarejo.data.product

import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.model.toModel
import com.cabral.lucrovarejo.data.product.model.toRequest
import com.cabral.lucrovarejo.domain.product.ProductCreationFailure
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
        val documentId = try {
            remoteDataSource.createProduct(request)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: ProductCreationFailure) {
            throw failure
        } catch (failure: FirebaseFirestoreException) {
            val reason = when (failure.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    ProductCreationFailure.Reason.PERMISSION_DENIED
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    ProductCreationFailure.Reason.NETWORK
                else -> ProductCreationFailure.Reason.UNKNOWN
            }
            throw ProductCreationFailure(reason, failure)
        } catch (failure: Exception) {
            throw ProductCreationFailure(ProductCreationFailure.Reason.UNKNOWN, failure)
        }
        return request.toModel(documentId)
    }
}
