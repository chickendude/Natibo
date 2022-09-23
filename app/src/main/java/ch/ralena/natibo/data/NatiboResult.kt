package ch.ralena.natibo.data

import androidx.annotation.StringRes
import ch.ralena.natibo.data.NatiboResult.Failure
import ch.ralena.natibo.data.NatiboResult.Success

/**
 * Wrapper for passing values.
 *
 * [Success] for successful operations, [Failure] for failed operations.
 */
sealed class NatiboResult<T> {
	/**
	 * The operation was successful.
	 *
	 * @param data This holds the object that is the result of the operation.
	 */
	data class Success<T>(val data: T): NatiboResult<T>()

	/**
	 * The operation was unsuccessful.
	 *
	 * @param stringRes The resource string containing the error message.
	 */
	data class Failure<T>(@StringRes val stringRes: Int?): NatiboResult<T>()
}