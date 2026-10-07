package com.sensiffmax.app.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberBottomSheet — Bottom sheet with cyber styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = CyberColors.Elevated,
        contentColor = CyberColors.TextPrimary,
        tonalElevation = 0.dp,
        scrimColor = CyberColors.Background.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = CyberColors.TextTertiary
                ) {}
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        content = content
    )
}
