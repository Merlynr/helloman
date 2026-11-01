package com.lifeonline.app.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChart(
    tec: Int,
    vit: Int,
    san: Int,
    gold: Int,
    rel: Int,
    maxStat: Int = 100,
    modifier: Modifier = Modifier.height(210.dp)
) {
    val labels = listOf("智力\nTEC", "气血\nVIT", "心力\nSAN", "财力\nGOLD", "人脉\nREL")
    val rawValues = listOf(tec, vit, san, gold, rel)

    Box(modifier = modifier.padding(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 34.dp.toPx()
            val angleStep = (2 * Math.PI / 5).toFloat()
            val startAngle = (-Math.PI / 2).toFloat() // 顶部起点

            // 1. 绘制同心多边形背景网格 (4层: 25%, 50%, 75%, 100%)
            for (level in 1..4) {
                val currentR = radius * (level / 4f)
                val gridPath = Path()
                for (i in 0 until 5) {
                    val angle = startAngle + i * angleStep
                    val x = center.x + currentR * cos(angle)
                    val y = center.y + currentR * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = RetroCardBorder,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. 绘制轴线与文本
            val textPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#DCFCE7")
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            for (i in 0 until 5) {
                val angle = startAngle + i * angleStep
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)

                // 轴线
                drawLine(
                    color = RetroCardBorder,
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 1.dp.toPx()
                )

                // 标签绘制
                val labelR = radius + 22.dp.toPx()
                val labelX = center.x + labelR * cos(angle)
                val labelY = center.y + labelR * sin(angle)

                drawContext.canvas.nativeCanvas.drawText(
                    "${labels[i].replace("\n", " ")} ${rawValues[i]}",
                    labelX,
                    labelY + 8f,
                    textPaint
                )
            }

            // 3. 绘制玩家实际五维数据多边形
            val statPath = Path()
            val points = mutableListOf<Offset>()
            for (i in 0 until 5) {
                val ratio = (rawValues[i].toFloat() / maxStat.toFloat()).coerceIn(0.1f, 1.2f)
                val currentR = radius * ratio
                val angle = startAngle + i * angleStep
                val x = center.x + currentR * cos(angle)
                val y = center.y + currentR * sin(angle)
                val pt = Offset(x, y)
                points.add(pt)
                if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
            }
            statPath.close()

            // 填充半透明多边形
            drawPath(
                path = statPath,
                color = RetroGreenPrimary.copy(alpha = 0.25f)
            )

            // 绘制绿色边框
            drawPath(
                path = statPath,
                color = RetroGreenPrimary,
                style = Stroke(width = 2.dp.toPx())
            )

            // 绘制关键顶点光点
            points.forEach { pt ->
                drawCircle(
                    color = RetroAmber,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}
