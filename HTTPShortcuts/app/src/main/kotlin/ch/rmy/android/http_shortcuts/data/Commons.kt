package ch.rmy.android.http_shortcuts.data

object Commons { // TODO: Find better name

    fun setVariableValue(variableId: String, value: String) =
        Transactions.commit { realm ->
            Repository.getVariableById(realm, variableId)?.value = value
        }

    fun resetVariableValues(variableIds: List<String>) =
        Transactions.commit { realm ->
            variableIds.forEach { variableId ->
                Repository.getVariableById(realm, variableId)?.value = ""
            }
        }

}