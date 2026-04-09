package ai.nuwa.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class NuwaAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isProcessing = false

    companion object {
        var instance: NuwaAccessibilityService? = null
            private set
        
        private val uiTreeCache = mutableMapOf<String, AccessibilityNodeInfo>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        serviceScope.launch {
            processAccessibilityEvent(event)
        }
    }

    override fun onInterrupt() {
    }

    private suspend fun processAccessibilityEvent(event: AccessibilityEvent) {
        if (isProcessing) return
        isProcessing = true

        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowStateChanged(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleWindowContentChanged(event)
                }
            }
        } finally {
            isProcessing = false
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        broadcastUpdate("window_state_changed", mapOf(
            "package" to packageName,
            "class" to className
        ))
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val tree = buildUiTree(rootNode)
        broadcastUpdate("ui_tree_updated", tree)
        rootNode.recycle()
    }

    private fun buildUiTree(node: AccessibilityNodeInfo): Map<String, Any?> {
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val children = mutableListOf<Map<String, Any?>>()
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                children.add(buildUiTree(child))
                child.recycle()
            }
        }

        return mapOf(
            "id" to node.viewIdResourceName,
            "className" to node.className?.toString(),
            "text" to node.text?.toString(),
            "contentDesc" to node.contentDescription?.toString(),
            "bounds" to mapOf(
                "left" to rect.left,
                "top" to rect.top,
                "right" to rect.right,
                "bottom" to rect.bottom
            ),
            "clickable" to node.isClickable,
            "focusable" to node.isFocusable,
            "children" to children
        )
    }

    fun performClick(nodeInfo: AccessibilityNodeInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val bounds = Rect()
            nodeInfo.getBoundsInScreen(bounds)
            val centerX = (bounds.left + bounds.right) / 2f
            val centerY = (bounds.top + bounds.bottom) / 2f
            
            val path = Path().apply {
                moveTo(centerX, centerY)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .build()
            
            dispatchGesture(gesture, null, null)
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    fun performInput(nodeInfo: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build()
            
            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    private fun broadcastUpdate(action: String, data: Map<String, Any?>) {
        val intent = Intent("ai.nuwa.ACCESSIBILITY_UPDATE").apply {
            putExtra("action", action)
            putExtra("data", android.os.Parcelable::class.java.name)
        }
        sendBroadcast(intent)
    }

    fun getCurrentUiTree(): Map<String, Any?>? {
        val rootNode = rootInActiveWindow ?: return null
        val tree = buildUiTree(rootNode)
        rootNode.recycle()
        return tree
    }

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeRecursive(rootNode, text)
    }

    private fun findNodeRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeRecursive(child, text)?.let { return it }
                child.recycle()
            }
        }
        return null
    }
}
