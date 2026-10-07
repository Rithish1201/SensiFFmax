package com.sensiffmax.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sensiffmax.app.core.ui.theme.CyberColors

/**
 * CyberDialog — Modal dialog with cyber styling.
 */
@Composable
fun CyberDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    confirmText: String = "CONFIRM",
    dismissText: String = "CANCEL",
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = CyberColors.Elevated,
            border = BorderStroke(1.dp, CyberColors.Border),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = CyberColors.CyberCyan
                )

                Spacer(Modifier.height(16.dp))

                content()

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDismiss != null) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = CyberColors.TextSecondary
                            )
                        ) {
                            Text(
                                text = dismissText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (onConfirm != null) {
                        CyberButton(
                            text = confirmText,
                            onClick = onConfirm
                        )
                    }
                }
            }
        }
    }
}
