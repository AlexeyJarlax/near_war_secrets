package com.pavlov.nearWarSecrets.ui.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val appName = stringResource(id = R.string.app_name_in_main_page)
    val appVersionText = stringResource(id = R.string.app_version_text)
    val packageManager = context.packageManager
    val packageName = context.packageName
    val appVersion = try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "N/A"
    }
    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$appName $appVersionText $appVersion",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButtonOne(
                        onClick = {
                            val appId = context.packageName
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.share_app_text, appId)
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.share_app_title)
                                )
                            )
                        },
                        text = stringResource(id = R.string.share_the_app),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Share
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButtonOne(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data =
                                    Uri.parse("mailto:${context.getString(R.string.support_email)}")
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    context.getString(R.string.support_email_subject)
                                )
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.support_email_text)
                                )
                            }
                            context.startActivity(intent)
                        },
                        text = stringResource(id = R.string.write_to_support),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Mail
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButtonOne(
                        onClick = {
                            val url = context.getString(R.string.user_agreement_url)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        text = stringResource(id = R.string.user_agreement),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.DocumentScanner
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomButtonOne(
                        onClick = {
                            val url = context.getString(R.string.developers_page_url)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        text = stringResource(id = R.string.developers_page),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.PrecisionManufacturing
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.new_about_app),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    )
}

//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.about_app_long2),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.about_app_long3),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.about_app_long4),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.about_app_long5),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.about_app_long6),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = stringResource(id = R.string.how_does_the_key_works_details),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//        }
//    )
//}