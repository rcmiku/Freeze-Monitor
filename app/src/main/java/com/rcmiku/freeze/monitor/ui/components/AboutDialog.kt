package com.rcmiku.freeze.monitor.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rcmiku.freeze.monitor.BuildConfig
import com.rcmiku.freeze.monitor.R
import com.rcmiku.freeze.monitor.util.AppContext
import com.rcmiku.freeze.monitor.util.Shell
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun AboutDialog(showDialog: MutableState<Boolean>) {

    val context = AppContext.context
    val packageManager: PackageManager = context.packageManager
    val applicationInfo = context.applicationInfo
    val icon = packageManager.getApplicationIcon(applicationInfo)
    val v1frozen = Shell.cmd("ls /sys/fs/cgroup/freezer/perf/frozen/freezer.state").first
    val v2UID = Shell.cmd("ls /sys/fs/cgroup/uid_0/cgroup.freeze").first
    val v2frozen = Shell.cmd("ls /sys/fs/cgroup/frozen/cgroup.freeze").first
    val uriHandler = LocalUriHandler.current

    SuperDialog(
        show = showDialog,
        title = stringResource(R.string.about),
        onDismissRequest = {
            dismissDialog(showDialog)
        }
    ) {
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberDrawablePainter(icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.mounted_freezer),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = buildString {
                    if (v1frozen == 0) {
                        append("V1(FROZEN) ")
                    }
                    if (v2UID == 0) {
                        append("V2(UID) ")
                    }
                    if (v2frozen == 0) {
                        append("V2(FROZEN)")
                    }
                    ifEmpty {
                        append(stringResource(R.string.mount_freezer_empty))
                    }
                }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.developer),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.author)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.source_code),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = AnnotatedString(
                    text = "GitHub",
                    spanStyle = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MiuixTheme.colorScheme.primary
                    )
                ),
                modifier = Modifier.clickable(
                    onClick = {
                        uriHandler.openUri("https://github.com/rcmiku/Freeze-Monitor")
                    }
                )
            )
        }
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.app_info)
        )
    }
}