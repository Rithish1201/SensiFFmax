package com.sensiffmax.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sensiffmax.app.core.navigation.SensiFFMaxAppContent
import com.sensiffmax.app.core.ui.theme.SensiFFMaxTheme

/**
 * SensiFFMax Main Activity
 *
 * Single-activity architecture. Hosting SensiFFMaxAppContent navigation graph.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SensiFFMaxTheme {
                SensiFFMaxAppContent()
            }
        }
    }
}
