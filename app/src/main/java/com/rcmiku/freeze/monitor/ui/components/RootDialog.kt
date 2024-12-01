package com.rcmiku.freeze.monitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rcmiku.freeze.monitor.R
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun RootDialog(showDialog: MutableState<Boolean>) {

    val isShow = rememberSaveable { mutableStateOf(false) }
    if (showDialog.value && !isShow.value) {
        isShow.value = !isShow.value
        SuperDialog(
            title = stringResource(R.string.root_title),
            summary = stringResource(R.string.root_message),
            show = showDialog,
            onDismissRequest = {
                dismissDialog(showDialog)
            },
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.confirm),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        dismissDialog(showDialog)
                    }
                )
            }
        }
    }
}
