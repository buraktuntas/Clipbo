package com.bt.clipbo.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    placeholder: String = "Ara...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = leadingIcon ?: {
            Icon(
                Icons.Default.Search,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = trailingIcon ?: {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Temizle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(query)
                keyboardController?.hide()
            }
        )
    )
}

@Composable
fun ExpandableSearchBar(
    isExpanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit = {},
    placeholder: String = "Ara...",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
        }

        if (!isExpanded) {
            IconButton(
                onClick = { onExpandedChange(true) }
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            TextButton(
                onClick = {
                    onExpandedChange(false)
                    onQueryChange("")
                }
            ) {
                Text("İptal")
            }
        }
    }
}

@Composable
fun SearchBarWithFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    filterCount: Int = 0,
    onFilterClick: () -> Unit = {},
    placeholder: String = "Ara...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            placeholder = placeholder,
            trailingIcon = {
                Row {
                    if (filterCount > 0) {
                        FilterChip(
                            onClick = onFilterClick,
                            label = { Text("$filterCount Filtre") },
                            selected = true,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    } else {
                        IconButton(onClick = onFilterClick) {
                            Icon(
                                Icons.Default.Search, // Filtre ikonu yerine
                                contentDescription = "Filtreler"
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = query.isNotEmpty()
                    ) {
                        IconButton(
                            onClick = { onQueryChange("") }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Temizle"
                            )
                        }
                    }
                }
            }
        )
    }
}