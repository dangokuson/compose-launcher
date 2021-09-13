package com.lin.comlauncher.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lin.comlauncher.entity.AppInfoBaseBean
import com.lin.comlauncher.entity.AppPos
import com.lin.comlauncher.entity.ApplicationInfo
import com.lin.comlauncher.ui.theme.MyBasicColumn
import com.lin.comlauncher.ui.theme.pagerFlingBehavior
import com.lin.comlauncher.util.LauncherUtils
import com.lin.comlauncher.util.LogUtils
import com.lin.comlauncher.util.SortUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Composable
fun DesktopView(applist: State<AppInfoBaseBean?>){
    var width = LocalConfiguration.current.screenWidthDp
    var height = LocalConfiguration.current.screenHeightDp
    LogUtils.e("load")
    val state = rememberScrollState()
    var context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier
        .width(width = width.dp)
        .height(height = height.dp)
        .verticalScroll(rememberScrollState())
        .horizontalScroll(
            state,
            flingBehavior = pagerFlingBehavior(
                state,
                (applist.value?.homeList?.size ?: 0)
            )
        )
    ) {

        applist.value?.homeList?.forEachIndexed { index, list ->
            val applist = remember { mutableStateListOf<ApplicationInfo>() }
            applist.addAll(list)
            Column(modifier = Modifier
                .width(width = width.dp)
                .height(height = height.dp)) {
                MyBasicColumn(){

                    applist.forEach {
                        var offsetX by remember { mutableStateOf(it.posX.dp) }
                        var offsetY by remember { mutableStateOf(it.posY.dp) }
                        var posX = it.posX
                        var posY = it.posY
                        var off = offsetX

                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .size(it.width.dp, it.height.dp)
                                .offset(posX.dp,posY.dp)
                                .pointerInput(it) {

                                    detectDragGestures(
                                        onDragStart = { off ->
                                            it.isDrag = true
                                            it.orignX = it.posX
                                            it.orignY = it.posY
                                            LogUtils.e("drag app ${it.name}")
                                        },
                                        onDragEnd = {
                                            it.isDrag = false
                                            SortUtils.resetPos(applist,it)
                                            offsetX = it.posX.dp
                                            offsetY = it.posY.dp
                                            LogUtils.e("dragex=${it.posX} dragY=${it.posY}")
                                            coroutineScope.launch {
                                                applist.forEach { appInfo->
                                                    animate(
                                                        typeConverter =  TwoWayConverter(
                                                            convertToVector = { size: AppPos ->
                                                                AnimationVector2D(size.x.toFloat(), size.y.toFloat())
                                                            },
                                                            convertFromVector = { vector: AnimationVector2D ->
                                                                AppPos(vector.v1.toInt(), vector.v2.toInt())
                                                            }
                                                        ),
                                                        initialValue = AppPos(appInfo.posX,appInfo.posY)
                                                        ,targetValue = AppPos(appInfo.orignX,appInfo.orignY)
                                                        ,initialVelocity= AppPos(0,0)
                                                        ,animationSpec =  tween(300),
                                                    ){appPos,velocity->
                                                        appInfo.posX = appPos.x
                                                        appInfo.posY = appPos.y
                                                        offsetX = appInfo.posX.dp
                                                        offsetY = appInfo.posY.dp
                                                    }
                                                }
                                            }
                                        }
                                    ) { change, dragAmount ->
                                        change.consumeAllChanges()
                                        it.posX += dragAmount.x.toDp().value.toInt()
                                        it.posY += dragAmount.y.toDp().value.toInt()
                                        offsetX += dragAmount.x.toDp()
                                        offsetY += dragAmount.y.toDp()
                                    }
                                }
                                .clickable {
//                                    LauncherUtils.startApp(context, it)
                                }) {
                            it.icon?.let { icon->
                                Image(icon.asImageBitmap(), contentDescription = "",
                                    modifier = Modifier.size(56.dp,56.dp))

                            }
                            Text(text = it.name?:"",overflow = TextOverflow.Ellipsis,maxLines = 1,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(4.dp,10.dp,4.dp,0.dp))
                        }

                    }
                }
            }
        }
    }
}
