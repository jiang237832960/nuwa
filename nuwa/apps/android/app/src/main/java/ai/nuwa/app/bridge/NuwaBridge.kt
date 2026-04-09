package ai.nuwa.app.bridge

import ai.nuwa.app.accessibility.NuwaAccessibilityService
import ai.nuwa.app.data.model.*
import org.json.JSONObject
import org.json.JSONArray

class NuwaBridge {
    
    private val accessibility get() = NuwaAccessibilityService.instance
    
    fun isServiceConnected(): Boolean {
        return accessibility != null
    }
    
    fun parseIntent(text: String): Intent {
        val intentType = detectIntentType(text)
        val targetApp = detectTargetApp(text)
        val entities = extractEntities(text, intentType)
        val confidence = calculateConfidence(intentType, entities)
        
        return Intent(
            rawText = text,
            intentType = intentType,
            targetApp = targetApp,
            entities = entities,
            confidence = confidence
        )
    }
    
    private fun detectIntentType(text: String): IntentType {
        return when {
            text.contains("发消息") || text.contains("发送消息") || text.contains("发微信") -> IntentType.SendMessage
            text.contains("搜索") || text.contains("找") || text.contains("查找") -> IntentType.SearchContact
            text.contains("导航") || text.contains("去") || text.contains("到") -> IntentType.Navigate
            text.contains("打开") || text.contains("启动") -> IntentType.OpenApp
            text.contains("保存") || text.contains("下载") -> IntentType.SaveImage
            text.contains("账单") || text.contains("查账") || text.contains("账本") -> IntentType.QueryBill
            text.contains("文件") || text.contains("文档") -> IntentType.OpenFile
            else -> IntentType.Unknown
        }
    }
    
    private fun detectTargetApp(text: String): String? {
        return when {
            text.contains("微信") || text.contains("wechat") -> "com.tencent.mm"
            text.contains("高德") || text.contains("地图") -> "com.autonavi.minimap"
            text.contains("支付宝") || text.contains("alipay") -> "com.eg.android.AlipayGphone"
            text.contains("wps") || text.contains("文档") -> "cn.wps.moffice_eng"
            text.contains("淘宝") -> "com.taobao.taobao"
            text.contains("京东") -> "com.jingdong.app.mall"
            text.contains("美团") -> "com.sankuai.meituan"
            else -> null
        }
    }
    
    private fun extractEntities(text: String, intentType: IntentType): List<Entity> {
        val entities = mutableListOf<Entity>()
        
        val contactPattern = Regex("给(.+?)发|向(.+?)发|发给(.+?)[发信]|联系人(.+?)[信发]")
        contactPattern.find(text)?.let { match ->
            val name = match.groupValues.filter { it.isNotBlank() }.lastOrNull() ?: ""
            if (name.isNotBlank()) {
                entities.add(Entity(name = "联系人", value = name, entityType = EntityType.Person))
            }
        }
        
        if (entities.isEmpty()) {
            val simplePattern = Regex("给(.+?)发消息|发给(.+)")
            simplePattern.find(text)?.let { match ->
                match.groupValues.filter { it.isNotBlank() }.lastOrNull()?.let { name ->
                    if (name.length <= 20) {
                        entities.add(Entity(name = "联系人", value = name, entityType = EntityType.Person))
                    }
                }
            }
        }
        
        val locationPattern = Regex("导航到(.+?)[到行]|去(.+?)[的地]|目的地(.+)")
        locationPattern.find(text)?.let { match ->
            match.groupValues.filter { it.isNotBlank() }.lastOrNull()?.let { location ->
                entities.add(Entity(name = "目的地", value = location, entityType = EntityType.Location))
            }
        }
        
        return entities
    }
    
    private fun calculateConfidence(intentType: IntentType, entities: List<Entity>): Float {
        if (intentType == IntentType.Unknown) return 0.3f
        
        var confidence = 0.6f
        
        if (entities.isNotEmpty()) {
            confidence += 0.2f
        }
        
        return confidence.coerceAtMost(1.0f)
    }
    
    fun planTask(intent: Intent): Task {
        val steps = when (intent.intentType) {
            IntentType.SendMessage -> planSendMessageSteps(intent)
            IntentType.Navigate -> planNavigateSteps(intent)
            IntentType.OpenFile -> planOpenFileSteps(intent)
            IntentType.QueryBill -> planQueryBillSteps(intent)
            else -> emptyList()
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
        val contactName = intent.entities.find { it.entityType == EntityType.Person }?.value ?: ""
        val appPackage = intent.targetApp ?: "com.tencent.mm"
        
        val steps = mutableListOf<Step>()
        
        steps.add(Step(
            id = 1,
            action = Action.OpenApp,
            target = Target(
                selector = Selector(SelectorType.PackageName, appPackage)
            ),
            message = "打开微信"
        ))
        
        steps.add(Step(
            id = 2,
            action = Action.Wait,
            message = "等待界面加载"
        ))
        
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
        
        if (contactName.isNotBlank()) {
            steps.add(Step(
                id = 6,
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "搜索")
                ),
                message = "点击搜索"
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
        }
        
        return steps
    }
    
    private fun planNavigateSteps(intent: Intent): List<Step> {
        val destination = intent.entities.find { it.entityType == EntityType.Location }?.value ?: ""
        val steps = mutableListOf<Step>()
        
        steps.add(Step(
            id = 1,
            action = Action.OpenApp,
            target = Target(
                selector = Selector(SelectorType.PackageName, "com.autonavi.minimap")
            ),
            message = "打开高德地图"
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
        
        if (destination.isNotBlank()) {
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
                action = Action.Click,
                target = Target(
                    selector = Selector(SelectorType.Text, "导航")
                ),
                message = "点击导航"
            ))
        }
        
        return steps
    }
    
    private fun planOpenFileSteps(intent: Intent): List<Step> {
        return listOf(
            Step(
                id = 1,
                action = Action.OpenApp,
                target = Target(
                    selector = Selector(SelectorType.PackageName, "cn.wps.moffice_eng")
                ),
                message = "打开WPS"
            ),
            Step(
                id = 2,
                action = Action.Wait,
                message = "等待WPS加载"
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
                message = "打开支付宝"
            ),
            Step(
                id = 2,
                action = Action.Wait,
                message = "等待支付宝加载"
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
    
    fun executeStep(step: Step): Boolean {
        val service = accessibility ?: return false
        
        return when (step.action) {
            Action.OpenApp -> {
                step.target?.let { target ->
                    target.selector.value.let { packageName ->
                        val intent = android.content.Intent().apply {
                            setPackage(packageName)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            service.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                } ?: false
            }
            
            Action.Click -> {
                step.target?.let { target ->
                    when (target.selector.by) {
                        SelectorType.Text -> {
                            service.findNodeByText(target.selector.value)?.let { node ->
                                val result = service.performClick(node)
                                node.recycle()
                                result
                            }
                        }
                        SelectorType.ResourceId -> {
                            service.findNodeByResourceId(target.selector.value)?.let { node ->
                                val result = service.performClick(node)
                                node.recycle()
                                result
                            }
                        }
                        else -> false
                    }
                } ?: false
            }
            
            Action.Input -> {
                step.target?.let { target ->
                    val inputText = target.text ?: ""
                    when (target.selector.by) {
                        SelectorType.Text -> {
                            service.findNodeByText(target.selector.value)?.let { node ->
                                val result = service.performInput(node, inputText)
                                node.recycle()
                                result
                            }
                        }
                        SelectorType.ClassName -> {
                            val node = service.findNodeByText("")
                            node?.let {
                                val result = service.performInput(it, inputText)
                                it.recycle()
                                result
                            }
                        }
                        else -> false
                    }
                } ?: false
            }
            
            Action.Wait -> {
                Thread.sleep(500)
                true
            }
            
            Action.Swipe -> {
                step.target?.bounds?.let { bounds ->
                    service.performSwipe(
                        bounds.centerX,
                        bounds.centerY,
                        bounds.centerX,
                        0f
                    )
                } ?: false
            }
            
            else -> false
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
