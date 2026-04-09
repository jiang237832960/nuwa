package ai.nuwa.app.bridge

import ai.nuwa.app.accessibility.NuwaAccessibilityService

class AccessibilityBridge {
    
    private var service: NuwaAccessibilityService? = null

    fun initialize(): Boolean {
        service = NuwaAccessibilityService.instance
        return service != null
    }

    fun isServiceRunning(): Boolean {
        return service != null
    }

    fun getUiTree(): String? {
        return service?.getCurrentUiTree()?.toString()
    }

    fun performClickByText(text: String): Boolean {
        val node = service?.findNodeByText(text)
        return if (node != null) {
            val result = service?.performClick(node) ?: false
            node.recycle()
            result
        } else {
            false
        }
    }

    fun performSwipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return service?.performSwipe(x1, y1, x2, y2) ?: false
    }
}
