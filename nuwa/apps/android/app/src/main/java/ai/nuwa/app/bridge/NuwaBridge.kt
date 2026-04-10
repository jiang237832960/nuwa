package ai.nuwa.app.bridge

import ai.nuwa.app.accessibility.NuwaAccessibilityService
import ai.nuwa.app.data.model.*
import ai.nuwa.app.data.repository.ModelRepository
import ai.nuwa.app.inference.InferenceEngine
import ai.nuwa.app.inference.InferenceResult
import ai.nuwa.app.inference.ModelManager
import ai.nuwa.app.inference.ModelManagerHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NuwaBridge(
    private val modelRepository: ModelRepository? = null
) {
    private val accessibility get() = NuwaAccessibilityService.instance
    private var modelManager: ModelManager? = null
    private var inferenceEngine: InferenceEngine? = null
    
    init {
        modelRepository?.let {
            modelManager = ModelManagerHolder.getInstance(it)
        }
    }
    
    fun isServiceConnected(): Boolean {
        return accessibility != null
    }
    
    suspend fun loadModel(modelInfo: ai.nuwa.app.data.repository.ModelInfo): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            modelManager?.let { mm ->
                val result = mm.loadModel(modelInfo)
                result.fold(
                    onSuccess = { engine ->
                        inferenceEngine = engine
                        Result.success(true)
                    },
                    onFailure = { e ->
                        Result.failure(e)
                    }
                )
            } ?: Result.failure(Exception("ModelManager not initialized"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun parseIntent(text: String): Intent = withContext(Dispatchers.Default) {
        val intentType = detectIntentType(text)
        val targetApp = detectTargetApp(text)
        val entities = extractEntities(text, intentType)
        val confidence = calculateConfidence(text, intentType, entities)
        
        Intent(
            rawText = text,
            intentType = intentType,
            targetApp = targetApp,
            entities = entities,
            confidence = confidence
        )
    }
    
    private fun detectIntentType(text: String): IntentType {
        val lower = text.lowercase()
        
        return when {
            lower.contains("发") && (lower.contains("消息") || lower.contains("微信") || lower.contains("短信")) -> IntentType.SendMessage
            lower.contains("搜索") || lower.contains("找") || lower.contains("查找") -> IntentType.SearchContact
            lower.contains("导航") || lower.contains("去") || lower.contains("到") || lower.contains("地图") -> IntentType.Navigate
            lower.contains("打开") || lower.contains("启动") || lower.contains("开启") -> IntentType.OpenApp
            lower.contains("保存") || lower.contains("下载") -> IntentType.SaveImage
            lower.contains("账单") || lower.contains("查账") || lower.contains("账本") -> IntentType.QueryBill
            lower.contains("文件") || lower.contains("文档") || lower.contains("打开文件") -> IntentType.OpenFile
            else -> IntentType.Unknown
        }
    }
    
    private fun detectTargetApp(text: String): String? {
        val lower = text.lowercase()
        
        return when {
            lower.contains("微信") || lower.contains("wechat") || lower.contains("wx") -> "com.tencent.mm"
            lower.contains("高德") || lower.contains("地图") || lower.contains("amap") -> "com.autonavi.minimap"
            lower.contains("支付宝") || lower.contains("alipay") -> "com.eg.android.AlipayGphone"
            lower.contains("wps") || lower.contains("文档") || lower.contains("office") -> "cn.wps.moffice_eng"
            lower.contains("淘宝") || lower.contains("天猫") || lower.contains("taobao") -> "com.taobao.taobao"
            lower.contains("京东") || lower.contains("jd") -> "com.jingdong.app.mall"
            lower.contains("美团") || lower.contains("meituan") -> "com.sankuai.meituan"
            lower.contains("抖音") || lower.contains("tiktok") -> "com.ss.android.ugc.aweme"
            lower.contains("百度") || lower.contains("baidu") -> "com.baidu.searchbox"
            else -> null
        }
    }
    
    private fun extractEntities(text: String, intentType: IntentType): List<Entity> {
        val entities = mutableListOf<Entity>()
        
        when (intentType) {
            IntentType.SendMessage -> {
                extractContactName(text)?.let { name ->
                    entities.add(Entity(
                        name = "联系人",
                        value = name,
                        entityType = EntityType.Person
                    ))
                }
                extractMessageContent(text)?.let { content ->
                    entities.add(Entity(
                        name = "消息内容",
                        value = content,
                        entityType = EntityType.Content
                    ))
                }
            }
            IntentType.Navigate -> {
                extractLocation(text)?.let { location ->
                    entities.add(Entity(
                        name = "目的地",
                        value = location,
                        entityType = EntityType.Location
                    ))
                }
            }
            IntentType.SearchContact -> {
                extractContactName(text)?.let { name ->
                    entities.add(Entity(
                        name = "联系人",
                        value = name,
                        entityType = EntityType.Person
                    ))
                }
            }
            IntentType.OpenFile -> {
                extractFileName(text)?.let { fileName ->
                    entities.add(Entity(
                        name = "文件名",
                        value = fileName,
                        entityType = EntityType.Content
                    ))
                }
            }
            else -> {}
        }
        
        extractTime(text)?.let { time ->
            entities.add(Entity(
                name = "时间",
                value = time,
                entityType = EntityType.Time
            ))
        }
        
        return entities
    }
    
    private fun extractContactName(text: String): String? {
        val patterns = listOf(
            Regex("给(.+?)(发|说|消息)"),
            Regex("发给(.+?)"),
            Regex("向(.+?)发"),
            Regex("跟(.+?)说"),
            Regex("和(.+?)说"),
            Regex("searching? for (.+)"),
            Regex("to (.+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val name = match.groupValues[1].trim()
                if (name.length in 1..20 && !name.contains("消息") && !name.contains("什么")) {
                    return name
                }
            }
        }
        return null
    }
    
    private fun extractMessageContent(text: String): String? {
        val patterns = listOf(
            Regex("说(.+)"),
            Regex("内容是(.+)"),
            Regex("告诉(.+?)说(.+)") 
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null && match.groupValues.size > 1) {
                val content = match.groupValues[1].trim()
                if (content.isNotBlank() && content.length < 500) {
                    return content
                }
            }
        }
        return null
    }
    
    private fun extractLocation(text: String): String? {
        val patterns = listOf(
            Regex("去(.+?)(那里|一下|呗|吗|。|$)"),
            Regex("到(.+?)(那里|一下|呗|吗|。|$)"),
            Regex("导航到(.+)"),
            Regex("去(.+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val location = match.groupValues[1].trim()
                if (location.isNotBlank() && location.length < 100) {
                    val excludeWords = listOf("哪里", "什么", "怎么", "帮", "我", "一下", "呗")
                    if (!excludeWords.any { location.contains(it) }) {
                        return location
                    }
                }
            }
        }
        return null
    }
    
    private fun extractFileName(text: String): String? {
        val patterns = listOf(
            Regex("打开(.+?)(文件|文档)"),
            Regex("(.+\\.\\w+)"),
            Regex("文件(.+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val fileName = match.groupValues[1].trim()
                if (fileName.isNotBlank() && (fileName.contains(".") || fileName.length < 50)) {
                    return fileName
                }
            }
        }
        return null
    }
    
    private fun extractTime(text: String): String? {
        val patterns = listOf(
            Regex("\\d{1,2}点"),
            Regex("今天"),
            Regex("明天"),
            Regex("后天")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.value
            }
        }
        return null
    }
    
    private fun calculateConfidence(text: String, intentType: IntentType, entities: List<Entity>): Float {
        if (intentType == IntentType.Unknown) return 0.3f
        
        var confidence = 0.5f
        
        if (entities.isNotEmpty()) {
            confidence += 0.2f
        }
        
        val targetApp = detectTargetApp(text)
        if (targetApp != null) {
            confidence += 0.15f
        }
        
        when (intentType) {
            IntentType.SendMessage -> {
                if (text.contains("微信") || text.contains("发消息")) confidence += 0.1f
            }
            IntentType.Navigate -> {
                if (text.contains("高德") || text.contains("导航")) confidence += 0.1f
            }
            else -> {}
        }
        
        return confidence.coerceAtMost(1.0f)
    }
    
    fun planTask(intent: Intent): Task {
        val steps = when (intent.intentType) {
            IntentType.SendMessage -> planSendMessageSteps(intent)
            IntentType.Navigate -> planNavigateSteps(intent)
            IntentType.OpenFile -> planOpenFileSteps(intent)
            IntentType.QueryBill -> planQueryBillSteps(intent)
            IntentType.OpenApp -> planOpenAppSteps(intent)
            else -> planUnknownTaskSteps(intent)
        }
        
        return Task(
            id = "task_${System.currentTimeMillis()}",
            intent = intent,
            steps = steps,
            status = TaskStatus.Idle,
            currentStep = 0,
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun planSendMessageSteps(intent: Intent): List<Step> {
        val contactName = intent.entities.find { it.entityType == EntityType.Person }?.value
        val messageContent = intent.entities.find { it.entityType == EntityType.Content }?.value
        val appPackage = intent.targetApp ?: "com.tencent.mm"
        
        val steps = mutableListOf<Step>()
        
        steps.add(Step(
            id = 1,
            action = Action.OpenApp,
            target = Target(
                selector = Selector(SelectorType.PackageName, appPackage)
            ),
            message = "正在打开微信"
        ))
        
        steps.add(Step(
            id = 2,
            action = Action.Wait,
            message = "等待微信启动"
        ))
        
        if (contactName != null) {
            steps.add(Step(
                id = 3,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "通讯录")
                ),
                message = "点击通讯录"
            ))
            
            steps.add(Step(
                id = 4,
                action = Action.Wait,
                message = "等待通讯录加载"
            ))
            
            steps.add(Step(
                id = 5,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "新的朋友")
                ),
                fallback = Step(
                    id = 5,
                    action = Action.Click,
                    target = Target(
                        selector = Selector(SelectorType.Text, "contacts")
                    )
                ),
                message = "点击新的朋友"
            ))
            
            steps.add(Step(
                id = 6,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "搜索")
                ),
                message = "点击搜索框"
            ))
            
            steps.add(Step(
                id = 7,
                action = Action.Input,
                target = Target(
                    selector = Selector(SelectorType.ClassName, "android.widget.EditText"),
                    text = contactName
                ),
                message = "输入联系人: $contactName"
            ))
            
            steps.add(Step(
                id = 8,
                action = Action.Wait,
                message = "等待搜索结果"
            ))
            
            steps.add(Step(
                id = 9,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, contactName)
                ),
                message = "选择联系人: $contactName"
            ))
            
            steps.add(Step(
                id = 10,
                action = Action.Wait,
                message = "等待聊天界面加载"
            ))
        }
        
        if (messageContent != null) {
            steps.add(Step(
                id = 11,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.ClassName, "android.widget.EditText")
                ),
                message = "点击输入框"
            ))
            
            steps.add(Step(
                id = 12,
                action = Action.Input,
                target = Target(
                    selector = Selector(SelectorType.ClassName, "android.widget.EditText"),
                    text = messageContent
                ),
                message = "输入消息: $messageContent"
            ))
        }
        
        return steps
    }
    
    private fun planNavigateSteps(intent: Intent): List<Step> {
        val destination = intent.entities.find { it.entityType == EntityType.Location }?.value
        val steps = mutableListOf<Step>()
        
        steps.add(Step(
            id = 1,
            action = Action.OpenApp,
            target = Target(
                selector = Selector(SelectorType.PackageName, "com.autonavi.minimap")
            ),
            message = "正在打开高德地图"
        ))
        
        steps.add(Step(
            id = 2,
            action = Action.Wait,
            message = "等待地图加载"
        ))
        
        steps.add(Step(
            id = 3,
            action = Action.Click,
            target = Target(
                selector = Selector(SelectorType.Text, "搜索")
            ),
            message = "点击搜索框"
        ))
        
        if (destination != null) {
            steps.add(Step(
                id = 4,
                action = Action.Input,
                target = Target(
                    selector = Selector(SelectorType.ClassName, "android.widget.EditText"),
                    text = destination
                ),
                message = "输入目的地: $destination"
            ))
            
            steps.add(Step(
                id = 5,
                action = Action.Wait,
                message = "等待搜索结果"
            ))
            
            steps.add(Step(
                id = 6,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "导航")
                ),
                message = "点击导航按钮"
            ))
        }
        
        return steps
    }
    
    private fun planOpenFileSteps(intent: Intent): List<Step> {
        val appPackage = intent.targetApp ?: "cn.wps.moffice_eng"
        
        return listOf(
            Step(
                id = 1,
                action = Action.OpenApp,
                target = Target(
                    selector = Selector(SelectorType.PackageName, appPackage)
                ),
                message = "正在打开WPS"
            ),
            Step(
                id = 2,
                action = Action.Wait,
                message = "等待WPS启动"
            ),
            Step(
                id = 3,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "打开文档")
                ),
                message = "点击打开文档"
            )
        )
    }
    
    private fun planQueryBillSteps(intent: Intent): List<Step> {
        return listOf(
            Step(
                id = 1,
                action = Action.OpenApp,
                target = Target(
                    selector = Selector(SelectorType.PackageName, "com.eg.android.AlipayGphone")
                ),
                message = "正在打开支付宝"
            ),
            Step(
                id = 2,
                action = Action.Wait,
                message = "等待支付宝启动"
            ),
            Step(
                id = 3,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "我的")
                ),
                message = "点击我的"
            ),
            Step(
                id = 4,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "账单")
                ),
                message = "点击账单"
            )
        )
    }
    
    private fun planOpenAppSteps(intent: Intent): List<Step> {
        val appPackage = intent.targetApp
        
        return if (appPackage != null) {
            listOf(
                Step(
                    id = 1,
                    action = Action.OpenApp,
                    target = Target(
                        selector = Selector(SelectorType.PackageName, appPackage)
                    ),
                    message = "正在打开应用"
                ),
                Step(
                    id = 2,
                    action = Action.Wait,
                    message = "等待应用启动"
                )
            )
        } else {
            listOf(
                Step(
                    id = 1,
                    action = Action.Wait,
                    message = "无法确定目标应用，请提供更多信息"
                )
            )
        }
    }
    
    private fun planUnknownTaskSteps(intent: Intent): List<Step> {
        return listOf(
            Step(
                id = 1,
                action = Action.Wait,
                message = "正在分析任务..."
            )
        )
    }
    
    fun executeStep(step: Step): Boolean {
        val service = accessibility ?: return false
        
        return when (step.action) {
            Action.OpenApp -> executeOpenApp(step, service)
            Action.Click -> executeClick(step, service)
            Action.Input -> executeInput(step, service)
            Action.Wait -> executeWait(step)
            Action.Swipe -> executeSwipe(step, service)
            Action.Scroll -> executeScroll(step, service)
            Action.Navigate -> executeNavigate(step, service)
            Action.CloseApp -> executeCloseApp(step, service)
        }
    }
    
    private fun executeOpenApp(step: Step, service: NuwaAccessibilityService): Boolean {
        step.target?.let { target ->
            val packageName = target.selector.value
            val intent = android.content.Intent().apply {
                setPackage(packageName)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                service.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
    
    private fun executeClick(step: Step, service: NuwaAccessibilityService): Boolean {
        step.target?.let { target ->
            val node = when (target.selector.by) {
                SelectorType.Text -> service.findNodeByText(target.selector.value)
                SelectorType.ContentDesc -> service.findNodeByContentDesc(target.selector.value)
                SelectorType.ResourceId -> service.findNodeByResourceId(target.selector.value)
                SelectorType.ClassName -> service.findClickableNode(target.selector.value)
                SelectorType.PackageName -> null
            }
            
            if (node != null) {
                val result = service.performClick(node)
                node.recycle()
                return result
            } else {
                val clickableNode = service.findClickableNode(target.selector.value)
                if (clickableNode != null) {
                    val result = service.performClick(clickableNode)
                    clickableNode.recycle()
                    return result
                }
            }
        }
        return false
    }
    
    private fun executeInput(step: Step, service: NuwaAccessibilityService): Boolean {
        step.target?.let { target ->
            val inputText = target.text ?: ""
            val node = when (target.selector.by) {
                SelectorType.Text -> service.findNodeByText(target.selector.value)
                SelectorType.ClassName -> {
                    service.findNodesByText(target.selector.value).firstOrNull()
                }
                else -> service.findNodesByText("").firstOrNull()
            }
            
            if (node != null) {
                val result = service.performInput(node, inputText)
                node.recycle()
                return result
            }
        }
        return false
    }
    
    private fun executeWait(step: Step): Boolean {
        try {
            Thread.sleep(1000)
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun executeSwipe(step: Step, service: NuwaAccessibilityService): Boolean {
        step.target?.bounds?.let { bounds ->
            return service.performSwipe(
                bounds.centerX,
                bounds.centerY,
                bounds.centerX,
                0f
            )
        }
        return false
    }
    
    private fun executeScroll(step: Step, service: NuwaAccessibilityService): Boolean {
        step.target?.let { target ->
            val node = when (target.selector.by) {
                SelectorType.Text -> service.findNodeByText(target.selector.value)
                else -> null
            }
            
            if (node != null) {
                val result = service.performScrollForward(node)
                node.recycle()
                return result
            }
        }
        return false
    }
    
    private fun executeNavigate(step: Step, service: NuwaAccessibilityService): Boolean {
        return executeClick(step, service)
    }
    
    private fun executeCloseApp(step: Step, service: NuwaAccessibilityService): Boolean {
        val packageName = step.target?.selector?.value ?: return false
        return try {
            val intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_DELETE
                data = android.net.Uri.parse("package:$packageName")
            }
            service.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getWorldState(): WorldState {
        val service = accessibility
        return WorldState(
            foregroundApp = service?.getCurrentPackageName() ?: "",
            uiTree = service?.getCurrentUiTree()?.let { parseUiTree(it) },
            deviceResources = DeviceResources(),
            taskState = null
        )
    }
    
    private fun parseUiTree(map: Map<String, Any?>): UiTreeNode {
        return UiTreeNode(
            id = map["id"] as? String,
            className = map["className"] as? String,
            text = map["text"] as? String,
            contentDesc = map["contentDesc"] as? String,
            bounds = (map["bounds"] as? Map<String, Any>)?.let { boundsMap ->
                Bounds(
                    left = (boundsMap["left"] as? Number)?.toInt() ?: 0,
                    top = (boundsMap["top"] as? Number)?.toInt() ?: 0,
                    right = (boundsMap["right"] as? Number)?.toInt() ?: 0,
                    bottom = (boundsMap["bottom"] as? Number)?.toInt() ?: 0
                )
            },
            clickable = map["clickable"] as? Boolean ?: false,
            focusable = map["focusable"] as? Boolean ?: false,
            enabled = map["enabled"] as? Boolean ?: true,
            scrollable = map["scrollable"] as? Boolean ?: false,
            children = (map["children"] as? List<Map<String, Any?>>)?.map { parseUiTree(it) } ?: emptyList()
        )
    }
}
