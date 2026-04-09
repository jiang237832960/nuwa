package ai.nuwa.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.pm.PackageManager
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
        
        const val WECHAT_PACKAGE = "com.tencent.mm"
        const val ACTION_UI_UPDATE = "ai.nuwa.ACCESSIBILITY_UPDATE"
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
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    handleViewClicked(event)
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    handleTextChanged(event)
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
            "class" to className,
            "currentApp" to isAppInForeground(packageName)
        ))
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val tree = buildUiTree(rootNode)
        broadcastUpdate("ui_tree_updated", tree)
        rootNode.recycle()
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        broadcastUpdate("view_clicked", mapOf(
            "package" to (event.packageName ?: ""),
            "className" to (event.className ?: ""),
            "text" to (event.text?.joinToString() ?: "")
        ))
    }

    private fun handleTextChanged(event: AccessibilityEvent) {
        broadcastUpdate("text_changed", mapOf(
            "package" to (event.packageName ?: ""),
            "text" to (event.text?.joinToString() ?: "")
        ))
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
            "enabled" to node.isEnabled,
            "scrollable" to node.isScrollable,
            "childrenCount" to node.childCount,
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

    fun performClickByCoordinate(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .build()
            
            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    fun performLongClick(nodeInfo: AccessibilityNodeInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val bounds = Rect()
            nodeInfo.getBoundsInScreen(bounds)
            val centerX = (bounds.left + bounds.right) / 2f
            val centerY = (bounds.top + bounds.bottom) / 2f
            
            val path = Path().apply {
                moveTo(centerX, centerY)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
                .build()
            
            dispatchGesture(gesture, null, null)
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        }
    }

    fun performInput(nodeInfo: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    fun performScrollForward(nodeInfo: AccessibilityNodeInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
    }

    fun performScrollBackward(nodeInfo: AccessibilityNodeInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        } else {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }
    }

    private fun broadcastUpdate(action: String, data: Map<String, Any?>) {
        val intent = Intent(ACTION_UI_UPDATE).apply {
            putExtra("action", action)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    fun getCurrentUiTree(): Map<String, Any?>? {
        val rootNode = rootInActiveWindow ?: return null
        val tree = buildUiTree(rootNode)
        rootNode.recycle()
        return tree
    }

    fun getCurrentPackageName(): String? {
        return rootInActiveWindow?. packageName?.toString()
    }

    fun findNodeByText(text: String, exactMatch: Boolean = false): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = findNodeRecursive(rootNode, text, exactMatch)
        rootNode.recycle()
        return result
    }

    fun findNodeByContentDesc(desc: String, exactMatch: Boolean = false): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = findNodeByContentDescRecursive(rootNode, desc, exactMatch)
        rootNode.recycle()
        return result
    }

    fun findNodeByResourceId(id: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = findNodeByResourceIdRecursive(rootNode, id)
        rootNode.recycle()
        return result
    }

    fun findNodesByText(text: String): List<AccessibilityNodeInfo> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val results = mutableListOf<AccessibilityNodeInfo>()
        findNodesRecursive(rootNode, text, results)
        rootNode.recycle()
        return results
    }

    fun findClickableNode(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val result = findClickableNodeRecursive(rootNode, text)
        rootNode.recycle()
        return result
    }

    private fun findNodeRecursive(node: AccessibilityNodeInfo, text: String, exactMatch: Boolean): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: ""
        val match = if (exactMatch) nodeText == text else nodeText.contains(text)
        
        if (match && node.isClickable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeRecursive(child, text, exactMatch)?.let { return it }
                child.recycle()
            }
        }
        return null
    }

    private fun findNodeByContentDescRecursive(node: AccessibilityNodeInfo, desc: String, exactMatch: Boolean): AccessibilityNodeInfo? {
        val contentDesc = node.contentDescription?.toString() ?: ""
        val match = if (exactMatch) contentDesc == desc else contentDesc.contains(desc)
        
        if (match) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByContentDescRecursive(child, desc, exactMatch)?.let { return it }
                child.recycle()
            }
        }
        return null
    }

    private fun findNodeByResourceIdRecursive(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == id) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByResourceIdRecursive(child, id)?.let { return it }
                child.recycle()
            }
        }
        return null
    }

    private fun findNodesRecursive(node: AccessibilityNodeInfo, text: String, results: MutableList<AccessibilityNodeInfo>) {
        val nodeText = node.text?.toString() ?: ""
        if (nodeText.contains(text)) {
            results.add(node)
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodesRecursive(child, text, results)
                child.recycle()
            }
        }
    }

    private fun findClickableNodeRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodeText = node.text?.toString() ?: ""
        
        if (nodeText.contains(text) && node.isClickable && node.isEnabled) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findClickableNodeRecursive(child, text)?.let { return it }
                child.recycle()
            }
        }
        return null
    }

    private fun isAppInForeground(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val am = getSystemService(android.app.ActivityManager::class.java)
                am?.runningAppProcesses?.any { it.pkgList.contains(packageName) } ?: false
            } else {
                @Suppress("DEPRECATION")
                getSystemService(android.app.ActivityManager::class.java)
                    ?.getRunningTasks(1)
                    ?.firstOrNull()
                    ?.topActivity?.packageName == packageName
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isWeChatInstalled(): Boolean {
        return try {
            packageManager.getApplicationInfo(WECHAT_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openWeChat(): Boolean {
        val intent = android.content.Intent().apply {
            setClassName(WECHAT_PACKAGE, "$WECHAT_PACKAGE.ui.MainUI")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
