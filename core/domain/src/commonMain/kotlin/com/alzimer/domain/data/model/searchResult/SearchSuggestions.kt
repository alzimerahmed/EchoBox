package com.alzimer.echobox.domain.data.model.searchResult

import com.alzimer.echobox.domain.data.type.SearchResultType

data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<SearchResultType>,
)