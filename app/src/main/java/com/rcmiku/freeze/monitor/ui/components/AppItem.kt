package com.rcmiku.freeze.monitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rcmiku.freeze.monitor.R
import com.rcmiku.freeze.monitor.model.AppInfo
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppItem(appInfo: AppInfo) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = appInfo.appIcon),
            contentDescription = appInfo.appName,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = appInfo.appName,
                style = MiuixTheme.textStyles.title4,
            )
            Text(
                text = buildString {
                    append(pluralStringResource(R.plurals.process,appInfo.runningProcess,appInfo.runningProcess))
                    if (appInfo.frozenProcess > 0) {
                        append(" ")
                        append(stringResource(R.string.frozen, appInfo.frozenProcess))
                    }
                },
                style = MiuixTheme.textStyles.subtitle,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant
            )
            Text(
                text = buildString {
                    if (appInfo.appRes < 1024) {
                        append(stringResource(R.string.size_in_mb, appInfo.appRes))
                    } else {
                        append(
                            stringResource(R.string.size_in_gb, appInfo.appRes / 1024f),
                        )
                    }
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }
        if (appInfo.freezeType.isNotEmpty())
            Badge(
                containerColor = MiuixTheme.colorScheme.onPrimaryVariant
            ) {
                Text(
                    text = stringResource(R.string.freeze_badge, appInfo.freezeType),
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.primaryVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
    }
}

