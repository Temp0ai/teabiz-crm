package com.teabiz.crm.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.teabiz.crm.R

@Composable
fun ChaiwalaBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.08f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Image(
            painter = painterResource(id = R.drawable.chotu_chaiwala),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
        content()
    }
}
