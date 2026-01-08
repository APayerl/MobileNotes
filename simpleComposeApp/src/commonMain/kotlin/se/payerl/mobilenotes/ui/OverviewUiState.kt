package se.payerl.mobilenotes.ui

/**
 * UI State for Overview screen.
 */
sealed interface OverviewUiState<out Type> {
    data object Loading : OverviewUiState<Nothing>
    data class Success<Type>(val list: List<Type>, val name: String? = null) : OverviewUiState<Type>
    data class Error(val message: String) : OverviewUiState<Nothing>
}
