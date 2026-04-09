package ai.nuwa.app.data.model

data class AppConfig(
    val packageName: String,
    val name: String,
    val category: AppCategory,
    val actions: Map<String, AppActionConfig> = emptyMap()
)

enum class AppCategory {
    PRIMARY,
    SECONDARY,
    TERTIARY
}

data class AppActionConfig(
    val actionType: String,
    val searchBoxId: String? = null,
    val searchButtonText: String? = null,
    val inputBoxId: String? = null,
    val sendButtonText: String? = null,
    val sendButtonId: String? = null,
    val listItemPattern: String? = null,
    val confirmButtonText: String? = null,
    val cancelButtonText: String? = null
)

object AppRegistry {
    
    val PRIMARY_APPS = listOf(
        AppConfig(
            packageName = "com.tencent.mm",
            name = "微信",
            category = AppCategory.PRIMARY,
            actions = mapOf(
                "send_message" to AppActionConfig(
                    actionType = "send_message",
                    searchBoxId = "com.tencent.mm:id/con",
                    searchButtonText = "搜索",
                    inputBoxId = "com.tencent.mm:id/aq0",
                    sendButtonText = "发送",
                    sendButtonId = "com.tencent.mm:id/ay8"
                ),
                "save_image" to AppActionConfig(
                    actionType = "save_image",
                    confirmButtonText = "保存图片",
                    cancelButtonText = "取消"
                )
            )
        ),
        AppConfig(
            packageName = "com.autonavi.minimap",
            name = "高德地图",
            category = AppCategory.PRIMARY,
            actions = mapOf(
                "navigate" to AppActionConfig(
                    actionType = "navigate",
                    searchBoxId = "com.autonavi.minimap:id/bbv",
                    searchButtonText = "搜索",
                    inputBoxId = "com.autonavi.minimap:id/a99",
                    sendButtonText = "导航",
                    sendButtonId = "com.autonavi.minimap:id/bdr"
                )
            )
        ),
        AppConfig(
            packageName = "cn.wps.moffice_eng",
            name = "WPS",
            category = AppCategory.PRIMARY,
            actions = mapOf(
                "open_file" to AppActionConfig(
                    actionType = "open_file",
                    listItemPattern = "doc,xls,ppt,pdf"
                )
            )
        ),
        AppConfig(
            packageName = "com.eg.android.AlipayGphone",
            name = "支付宝",
            category = AppCategory.PRIMARY,
            actions = mapOf(
                "query_bill" to AppActionConfig(
                    actionType = "query_bill",
                    searchBoxId = "com.eg.android.AlipayGphone:id/search_input",
                    searchButtonText = "搜索"
                )
            )
        )
    )
    
    val SECONDARY_APPS = listOf(
        AppConfig(
            packageName = "com.taobao.taobao",
            name = "淘宝",
            category = AppCategory.SECONDARY
        ),
        AppConfig(
            packageName = "com.jingdong.app.mall",
            name = "京东",
            category = AppCategory.SECONDARY
        ),
        AppConfig(
            packageName = "com.sankuai.meituan",
            name = "美团",
            category = AppCategory.SECONDARY
        ),
        AppConfig(
            packageName = "com.MobileTicket",
            name = "12306",
            category = AppCategory.SECONDARY
        )
    )
    
    fun getAppConfig(packageName: String): AppConfig? {
        return (PRIMARY_APPS + SECONDARY_APPS).find { it.packageName == packageName }
    }
    
    fun isPrimaryApp(packageName: String): Boolean {
        return PRIMARY_APPS.any { it.packageName == packageName }
    }
}
