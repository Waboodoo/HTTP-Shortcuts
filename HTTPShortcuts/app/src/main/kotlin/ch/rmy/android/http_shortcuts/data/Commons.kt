package ch.rmy.android.http_shortcuts.data

import androidx.annotation.CheckResult
import io.reactivex.Completable

object Commons { // TODO: Find better name

    @CheckResult
    fun setVariableValue(variableId: String, value: String): Completable =
        Transactions.commit { realm ->
            Repository.getVariableById(realm, variableId)?.value = value
        }

    @CheckResult
    fun resetVariableValues(variableIds: List<String>): Completable =
        Transactions.commit { realm ->
            variableIds.forEach { variableId ->
                Repository.getVariableById(realm, variableId)?.value = ""
            }
        }

}