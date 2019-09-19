package com.osias.blockchain.model.repository

/**
 * Interface para expor o erro do servidor
 */
interface RepositoryErrorDelegate {
    fun onError(error: Error)
}

open class BaseRepository {

    var delegate: RepositoryErrorDelegate? = null

}