"""
数据查询工具 - Agent 的核心数据层

职责：
    1. 意图识别（detect_intent）：根据用户文本判断想要查询什么数据
    2. 数据获取（fetch_data）：根据意图调用后端 API 获取真实业务数据
    3. 格式化输出：将 API 返回的 JSON 数据格式化为 LLM 可理解的文本

支持的意图类型：
    search_cars    - 搜索/浏览车辆列表（按品牌、车型、关键词筛选）
    car_detail     - 查询单辆车的详细参数信息
    hot_cars       - 获取热门车型排行（按成交订单数排序）
    stores         - 查询门店信息（按城市筛选）
    user_bookings  - 查询当前用户的订单列表（需要登录）
    user_profile   - 查询当前用户的个人信息（需要登录）
    car_reviews    - 查询某款车的用户评价
    none           - 无法识别的意图，返回 None 让 LLM 自由回答
"""
import re
from typing import Optional

import httpx


# ============================================================
# 意图识别规则 - 关键词字典和正则表达式
# ============================================================

# 品牌关键词字典 - 键为品牌名，值为该品牌下的所有车型关键词
# 用于匹配用户消息中的品牌/车型，判断用户想查询哪个品牌的车辆
BRAND_KEYWORDS = {
    # 大众集团
    "大众": ["大众", "朗逸", "宝来", "速腾", "迈腾", "帕萨特", "途观", "途昂", "polo", "桑塔纳", "途岳", "途铠", "揽巡", "揽境", "高尔夫", "探岳", "探歌", "CC"],
    "奥迪": ["奥迪", "A3", "A4", "A6", "Q3", "Q5", "A8", "Q7", "Q2L", "Q5L", "A4L", "A6L", "RS"],
    "保时捷": ["保时捷", "718", "卡宴", "帕拉梅拉", "Macan", "玛卡", "911", "Taycan"],
    "斯柯达": ["斯柯达", "明锐", "速派", "柯迪亚克", "晶锐", "柯米克"],

    # 日系品牌
    "丰田": ["丰田", "卡罗拉", "凯美瑞", "汉兰达", "雷凌", "RAV4", "普拉多", "皇冠", "荣放", "致炫", "亚洲龙", "赛那", "兰德酷路泽"],
    "本田": ["本田", "思域", "雅阁", "CR-V", "飞度", "凌派", "XR-V", "UR-V", "皓影", "缤智", "型格", "奥德赛", "艾力绅"],
    "日产": ["日产", "轩逸", "天籁", "奇骏", "逍客", "骐达", "蓝鸟", "楼兰", "途达", "劲客"],
    "马自达": ["马自达", "马3", "马6", "昂克赛拉", "阿特兹", "CX-4", "CX-5", "CX-30"],
    "三菱": ["三菱", "欧蓝德", "劲炫", "帕杰罗"],
    "雷克萨斯": ["雷克萨斯", "ES", "RX", "NX", "UX", "LS", "LX"],
    "英菲尼迪": ["英菲尼迪", "Q50L", "QX50", "QX60"],

    # 美系品牌
    "别克": ["别克", "英朗", "君威", "君越", "GL8", "昂科威", "威朗", "昂科旗", "微蓝"],
    "福特": ["福特", "福克斯", "蒙迪欧", "锐界", "探险者", "福睿斯", "金牛座", "烈马"],
    "雪佛兰": ["雪佛兰", "科鲁泽", "迈锐宝", "探界者", "科沃兹", "开拓者"],
    "凯迪拉克": ["凯迪拉克", "CT4", "CT5", "XT4", "XT5", "XT6", "ATS-L"],

    # 德系豪华
    "宝马": ["宝马", "3系", "5系", "X3", "X5", "1系", "2系", "4系", "7系", "X1", "X2", "X4", "X6", "i3", "iX3"],
    "奔驰": ["奔驰", "C级", "E级", "GLC", "A级", "S级", "GLE", "GLB", "CLA", "V级", "EQB", "EQC"],

    # 国产燃油/混动
    "哈弗": ["哈弗", "H6", "F7", "大狗", "初恋", "神兽", "赤兔", "M6", "酷狗"],
    "吉利": ["吉利", "帝豪", "星越", "博越", "星瑞", "远景", "缤越", "豪越", "银河L7", "银河L6"],
    "长安": ["长安", "逸动", "CS35", "CS55", "CS75", "UNI-V", "UNI-T", "UNI-K", "欧尚"],
    "奇瑞": ["奇瑞", "艾瑞泽", "瑞虎7", "瑞虎8", "捷途", "星途"],
    "比亚迪": ["比亚迪", "汉", "唐", "宋", "元", "秦", "海豹", "海狮", "驱逐舰", "护卫舰"],
    "广汽传祺": ["传祺", "GS4", "GS8", "影豹", "M8", "M6", "GA6"],
    "红旗": ["红旗", "H5", "H9", "HS5", "HS7", "E-QM5"],

    # 国产新能源纯电
    "理想": ["理想", "L6", "L7", "L8", "L9", "ONE"],
    "蔚来": ["蔚来", "ET5", "ET7", "ES6", "ES7", "ES8", "EC7"],
    "小鹏": ["小鹏", "P5", "P7", "G3", "G6", "G9", "X9"],
    "问界": ["问界", "M5", "M7", "M9"],
    "极氪": ["极氪", "001", "007", "009", "X"],
    "零跑": ["零跑", "C11", "T03", "C01"],
    "哪吒": ["哪吒", "V", "U", "S", "GT"],
    "深蓝": ["深蓝", "SL03", "S7"],
    "小米": ["小米", "SU7", "SU7 Pro", "SU7 Max", "小米汽车"],

    # 韩系
    "现代": ["现代", "伊兰特", "索纳塔", "ix35", "途胜", "名图"],
    "起亚": ["起亚", "K3", "K5", "智跑", "狮铂拓界", "嘉华"]
}

# 车辆类型关键词映射 - 将用户输入的中文类型词映射为后端 type 字段值
# 用于筛选不同类型的车辆（轿车/SUV/MPV/豪华/跑车）
CAR_TYPE_KEYWORDS = {
    "sedan": ["轿车", "三厢", "sedan"],
    "suv": ["suv", "越野"],
    "mpv": ["mpv", "商务车", "七座", "7座"],
    "luxury": ["豪华", "高端"],
    "sports": ["跑车", "运动"],
}

# 城市名称正则 - 覆盖全国主要城市，用于识别用户消息中的城市关键词
# 匹配到城市名后可结合"门店/地址"等词触发门店查询意图
CITY_PATTERN = re.compile(
    r"(北京|上海|广州|深圳|成都|杭州|武汉|南京|重庆|西安|苏州|天津|"
    r"长沙|郑州|青岛|大连|宁波|厦门|昆明|贵阳|南宁|哈尔滨|沈阳|济南|"
    r"福州|合肥|南昌|太原|石家庄|兰州|银川|西宁|呼和浩特|乌鲁木齐|"
    r"拉萨|海口|三亚|珠海|东莞|佛山|无锡|常州|徐州|温州|嘉兴|绍兴|"
    r"金华|台州|泉州|漳州|龙岩|三明|南平|宁德|莆田|晋江|石狮|南安|惠安|安溪|永春|德化|金门|连江|罗源|闽清|永泰|平潭|长乐|福清|闽侯)"
)

# 常见车型名称正则 - 用于在用户消息中提取具体的车型名
# 当用户问"朗逸多少钱"时，提取出"朗逸"用于精确查询
TRIM_NAME_PATTERN = re.compile(
    r"(朗逸|宝来|速腾|迈腾|帕萨特|卡罗拉|凯美瑞|汉兰达|"
    r"思域|雅阁|CR-V|英朗|君威|君越|GL8|宝马3系|奔驰C级|"
    r"718|轩逸|天籁|奥迪A4|哈弗H6|桑塔纳|polo|雷凌|"
    r"途观|途昂|昂科威|X3|X5|GLC|Q5|Q3|A3|A6)"
)


class DataService:
    """数据查询服务 - Agent 与后端 API 之间的数据桥梁"""

    def __init__(self, base_url: str):
        """
        初始化数据服务

        Args:
            base_url: 后端微服务网关地址（如 http://localhost:8080/api）
        """
        self.base_url = base_url
        # 异步 HTTP 客户端，复用连接池提高性能
        self.http = httpx.AsyncClient(base_url=base_url, timeout=15.0)

    # ================================================================
    # 意图识别
    # ================================================================

    def detect_intent(self, message: str) -> tuple[str, dict]:
        """
        意图识别 - 根据用户消息文本判断查询意图

        采用关键词匹配 + 正则表达式的规则引擎（非 ML 方案），
        按优先级依次匹配，命中即返回对应意图和提取的参数。

        Args:
            message: 用户输入的文本消息

        Returns:
            tuple[意图标识, 参数字典]
            - 意图标识：search_cars / car_detail / hot_cars / stores /
                       user_bookings / user_profile / car_reviews / none
            - 参数字典：提取的关键词（如 brand、city、keyword 等）
        """
        msg = message.strip()
        msg_lower = msg.lower()

        # ----- 意图1: 用户订单查询 -----
        # 匹配"我的订单""我租的车""我的预订"等
        if re.search(r"(我的|我)\s*(订单|预订|租车|预约|租的)", msg):
            return "user_bookings", {}

        # ----- 意图2: 用户个人信息查询 -----
        # 匹配"我的账户""我的资料""会员信息"等
        if re.search(r"(我的|我)\s*(账户|账号|信息|资料|会员|余额|积分|优惠券)", msg):
            return "user_profile", {}

        # ----- 意图3: 车辆评价查询 -----
        # 匹配"评价""口碑""用户说"等，同时尝试提取车型名
        if re.search(r"(评价|评论|口碑|打分|评分|用户说|买家说)", msg):
            trim_match = TRIM_NAME_PATTERN.search(msg)
            if trim_match:
                return "car_reviews", {"trim_name": trim_match.group(1)}
            return "car_reviews", {}

        # ----- 意图4: 门店查询（带城市） -----
        # 先匹配城市名，再匹配门店相关词，组合触发
        city_match = CITY_PATTERN.search(msg)
        if city_match and re.search(r"(门店|店|网点|地址|在哪|位置|取车|还车)", msg):
            return "stores", {"city": city_match.group(1)}

        # ----- 意图5: 门店查询（不带城市） -----
        if re.search(r"(门店|店|网点|地址|位置|在哪)", msg):
            return "stores", {}

        # ----- 意图6: 品牌/车型搜索 -----
        # 遍历品牌关键词字典，匹配用户提到的品牌
        for brand, keywords in BRAND_KEYWORDS.items():
            for kw in keywords:
                if kw.lower() in msg_lower:
                    # 如果同时包含价格类关键词 -> 查车辆详情（价格信息）
                    if re.search(r"(多少钱|价格|报价|租金|日租|月租|费用|便宜|优惠|贵|划算)", msg):
                        return "car_detail", {"keyword": kw}
                    # 如果同时包含参数类关键词 -> 查车辆详情（配置信息）
                    if re.search(r"(详情|介绍|参数|配置|性能|马力|油耗|座位|空间|动力|长宽高|排量|扭矩)", msg):
                        return "car_detail", {"keyword": kw}
                    # 否则 -> 按品牌搜索车辆列表
                    return "search_cars", {"brand": brand}

        # ----- 意图7: 车辆类型搜索 -----
        # 匹配"轿车""SUV""七座"等类型关键词
        for car_type, keywords in CAR_TYPE_KEYWORDS.items():
            for kw in keywords:
                if kw.lower() in msg_lower:
                    return "search_cars", {"type": car_type}

        # ----- 意图8: 热门车型排行 -----
        # 匹配"热门""畅销""排行""推荐车型""什么车好""订单多"等
        if re.search(r"(热门|热卖|畅销|爆款|排行|排名|最火|最热|推荐车型|热门车型|什么车好|什么车火|卖得好|租得多|成交量|订单多|销量)", msg):
            return "hot_cars", {}

        # ----- 意图9: 通用车辆浏览 -----
        # 兜底的车辆搜索意图，匹配"有哪些""推荐""车型"等泛搜索词
        if re.search(r"(有哪些|有什么|推荐|车型|车款|可选|看看|浏览|列表|全部|所有)", msg):
            return "search_cars", {}

        # ----- 意图10: 单独的价格查询 -----
        # 仅包含价格词但未命中品牌时，尝试提取车型名精确查询
        if re.search(r"(多少钱|价格|报价|租金|日租|月租|费用|便宜|优惠|贵|划算)", msg):
            trim_match = TRIM_NAME_PATTERN.search(msg)
            if trim_match:
                return "car_detail", {"keyword": trim_match.group(1)}

        # 无法识别的意图 - 返回 none，让 LLM 自由发挥回答
        return "none", {}

    # ================================================================
    # 数据获取 - 根据意图调用对应的后端 API
    # ================================================================

    async def fetch_data(self, intent: str, params: dict, token: str = "") -> Optional[str]:
        """
        数据获取路由 - 根据意图分发到对应的处理方法

        Args:
            intent: 意图标识（由 detect_intent 返回）
            params: 提取的参数字典
            token:  用户登录 Token（部分接口需要鉴权）

        Returns:
            格式化后的文本数据，供 LLM 引用回答
            返回 None 表示该意图无需数据查询
        """
        try:
            if intent == "search_cars":
                return await self._search_cars(params)
            elif intent == "car_detail":
                return await self._get_car_detail(params)
            elif intent == "stores":
                return await self._get_stores(params)
            elif intent == "user_bookings":
                return await self._get_user_bookings(token)
            elif intent == "user_profile":
                return await self._get_user_profile(token)
            elif intent == "car_reviews":
                return await self._get_car_reviews(params)
            elif intent == "hot_cars":
                return await self._get_hot_cars()
        except Exception as e:
            return None
        return None

    # ================================================================
    # 各意图的具体数据获取实现
    # ================================================================

    async def _search_cars(self, params: dict) -> Optional[str]:
        """
        搜索车辆列表 - 按品牌/类型筛选，返回前10条结果

        调用后端 GET /cars 获取所有有库存的车辆，
        然后在内存中按品牌名和车型类型进行二次筛选，
        最终格式化为文本列表供 LLM 使用。

        Args:
            params: {"brand": "大众"} 或 {"type": "suv"} 或两者都有

        Returns:
            格式化的车辆列表文本
        """
        resp = await self.http.get("/cars")
        if resp.status_code != 200:
            return None
        data = resp.json().get("data", [])
        if not data:
            return "暂无车辆数据。"

        brand = params.get("brand")
        car_type = params.get("type")

        # 内存过滤 - 按品牌名和车型类型筛选
        filtered = data
        if brand:
            filtered = [c for c in filtered if brand.lower() in c.get("name", "").lower() or brand.lower() in c.get("brand", "").lower()]
        if car_type:
            filtered = [c for c in filtered if c.get("type", "").lower() == car_type.lower()]

        if not filtered:
            return "暂未找到符合条件的车辆。"

        # 格式化输出，每辆车一行，包含名称、价格、类型、座位、燃料、变速箱、状态
        lines = []
        for c in filtered[:10]:
            name = c.get("name", "未知")
            price = c.get("rentalPrice") or c.get("price", "未知")
            car_type_val = c.get("type", "")
            seats = c.get("seats", "")
            fuel = c.get("fuel", "")
            transmission = c.get("transmission", "")
            status = c.get("status", "")
            parts = [f"- {name}"]
            if price:
                parts.append(f"日租金: ¥{price}")
            if car_type_val:
                parts.append(f"类型: {car_type_val}")
            if seats:
                parts.append(f"{seats}座")
            if fuel:
                parts.append(f"{fuel}")
            if transmission:
                parts.append(f"{transmission}")
            if status:
                parts.append(f"状态: {status}")
            lines.append(" | ".join(parts))

        summary = f"共找到 {len(filtered)} 辆车（展示前 {min(len(filtered), 10)} 辆）：\n" + "\n".join(lines)
        if len(filtered) > 10:
            summary += f"\n...还有 {len(filtered) - 10} 辆未展示"
        return summary

    async def _get_car_detail(self, params: dict) -> Optional[str]:
        """
        获取车辆详情 - 查询单辆车的完整参数配置

        先从列表接口模糊匹配车型名，再调用详情接口获取完整参数。
        返回包含品牌、价格、座位、动力参数、尺寸、配置等全量信息。

        Args:
            params: {"keyword": "朗逸"} - 用户提到的车型关键词

        Returns:
            车辆详情文本
        """
        keyword = params.get("keyword", "")
        resp = await self.http.get("/cars")
        if resp.status_code != 200:
            return None
        data = resp.json().get("data", [])

        # 模糊匹配车型名
        matched = [c for c in data if keyword.lower() in c.get("name", "").lower()]
        if not matched:
            matched = data[:3]  # 匹配不到时取前3辆作为推荐

        car = matched[0]
        car_id = car.get("id")
        # 调用详情接口获取完整参数
        detail_resp = await self.http.get(f"/cars/{car_id}")
        if detail_resp.status_code == 200:
            detail = detail_resp.json().get("data", car)
        else:
            detail = car

        # 组装详情文本
        lines = [
            f"车辆名称: {detail.get('name', '未知')}",
            f"品牌: {detail.get('brand', '未知')}",
            f"类型: {detail.get('type', '未知')}",
            f"日租金: ¥{detail.get('rentalPrice') or detail.get('price', '未知')}",
            f"座位数: {detail.get('seats', '未知')}",
            f"燃油类型: {detail.get('fuel', '未知')}",
            f"变速箱: {detail.get('transmission', '未知')}",
            f"状态: {detail.get('status', '未知')}",
        ]
        # 追加可选的性能参数
        if detail.get("horsepower"):
            lines.append(f"马力: {detail['horsepower']}hp")
        if detail.get("displacement"):
            lines.append(f"排量: {detail['displacement']}L")
        if detail.get("torque"):
            lines.append(f"扭矩: {detail['torque']}N·m")
        if detail.get("length") and detail.get("width") and detail.get("height"):
            lines.append(f"尺寸: {detail['length']}×{detail['width']}×{detail['height']}mm")
        if detail.get("features"):
            lines.append(f"特色配置: {detail['features']}")
        if detail.get("descriptions"):
            lines.append(f"介绍: {detail['descriptions']}")

        return "\n".join(lines)

    async def _get_stores(self, params: dict) -> Optional[str]:
        """
        查询门店信息 - 按城市筛选门店列表

        调用后端 GET /stores 获取所有门店，
        如果指定了城市则按 city 或 address 字段过滤。

        Args:
            params: {"city": "北京"} 或 {}（查询全部门店）

        Returns:
            门店列表文本，最多展示5家
        """
        resp = await self.http.get("/stores")
        if resp.status_code != 200:
            return None
        data = resp.json().get("data", [])
        if not data:
            return "暂无门店数据。"

        city = params.get("city")
        filtered = data
        if city:
            # 按城市名或地址中包含城市名来筛选
            filtered = [s for s in filtered if city in s.get("city", "") or city in s.get("address", "")]

        if not filtered:
            return f"未找到{city or ''}的门店信息。"

        # 格式化门店信息：名称、地址、电话、营业时间
        lines = []
        for s in filtered[:5]:
            name = s.get("name", "未知")
            address = s.get("address", "")
            phone = s.get("phone", "")
            hours = s.get("hours", "")
            parts = [f"- {name}"]
            if address:
                parts.append(address)
            if phone:
                parts.append(f"电话: {phone}")
            if hours:
                parts.append(f"营业时间: {hours}")
            lines.append(" | ".join(parts))

        summary = f"共找到 {len(filtered)} 家门店（展示前 {min(len(filtered), 5)} 家）：\n" + "\n".join(lines)
        return summary

    async def _get_user_bookings(self, token: str = "") -> Optional[str]:
        """
        查询用户订单列表 - 需要用户登录后携带 JWT Token

        通过 Token 调用后端 /bookings 接口，获取当前用户的订单记录。
        未登录时返回提示信息，让 Agent 引导用户登录。

        Args:
            token: 用户的 JWT Token

        Returns:
            订单列表文本，包含车辆名、状态、日期、金额、取还车地点
        """
        if not token:
            return "用户预订信息需要登录后查询。请提示用户登录后查看订单。"
        headers = {"Authorization": f"Bearer {token}"}
        resp = await self.http.get("/bookings", headers=headers)
        if resp.status_code != 200:
            return "获取订单信息失败，请稍后再试。"
        bookings = resp.json().get("data", [])
        if not bookings:
            return "您暂无订单记录。"

        # 订单状态中文映射
        status_map = {
            "pending": "待确认",
            "confirmed": "已确认",
            "active": "进行中",
            "completed": "已完成",
            "cancelled": "已取消",
        }

        lines = [f"您的订单记录（共{len(bookings)}条）："]
        for b in bookings[:10]:
            car_name = b.get("carName", "未知车辆")
            status = status_map.get(b.get("status", ""), b.get("status", ""))
            start = b.get("startDate", "")
            end = b.get("endDate", "")
            total = b.get("totalPrice", 0)
            days = b.get("totalDays", 0)
            pickup = b.get("pickupProvince", "") or b.get("pickupLocation", "")
            return_loc = b.get("returnProvince", "") or b.get("returnLocation", "")
            parts = [f"- {car_name} | {status}"]
            if start and end:
                parts.append(f"{start} ~ {end}")
            if days:
                parts.append(f"{days}天")
            if total:
                parts.append(f"¥{total}")
            if pickup:
                parts.append(f"取车: {pickup}")
            if return_loc:
                parts.append(f"还车: {return_loc}")
            lines.append(" | ".join(parts))
        return "\n".join(lines)

    async def _get_user_profile(self, token: str = "") -> Optional[str]:
        """
        查询用户个人信息 - 需要登录

        Args:
            token: 用户的 JWT Token

        Returns:
            用户信息文本（姓名、手机号、会员等级、账户状态）
        """
        if not token:
            return "用户信息需要登录后查询。请提示用户先登录。"
        headers = {"Authorization": f"Bearer {token}"}
        resp = await self.http.get("/user/profile", headers=headers)
        if resp.status_code != 200:
            return "获取用户信息失败，请稍后再试。"
        user = resp.json().get("data", {})
        if not user:
            return "未找到用户信息。"
        lines = [
            f"用户信息：",
            f"- 姓名: {user.get('name', '未知')}",
            f"- 手机号: {user.get('phone', '未知')}",
            f"- 会员等级: {user.get('memberLevel', '普通会员')}",
            f"- 账户状态: {user.get('status', '正常')}",
        ]
        return "\n".join(lines)

    async def _get_car_reviews(self, params: dict) -> Optional[str]:
        """
        查询车辆用户评价 - 先模糊匹配车型，再获取该车款的评价列表

        Args:
            params: {"trim_name": "朗逸"} - 车型名称

        Returns:
            评价列表文本，包含用户名、评分、租期、评价内容
        """
        keyword = params.get("trim_name", "")
        resp = await self.http.get("/cars")
        if resp.status_code != 200:
            return None
        cars = resp.json().get("data", [])

        # 模糊匹配车型名
        matched = [c for c in cars if keyword.lower() in c.get("name", "").lower()]
        if not matched:
            return f'未找到与"{keyword}"相关的车辆评价。'

        trim_id = matched[0].get("id")
        # 获取该车款的评价列表
        review_resp = await self.http.get(f"/reviews/trim/{trim_id}")
        if review_resp.status_code != 200:
            name = matched[0].get('name', '')
            return f'暂无"{name}"的用户评价。'

        reviews = review_resp.json().get("data", [])
        if not reviews:
            name = matched[0].get('name', '')
            return f'暂无"{name}"的用户评价。'

        # 格式化评价：用户名 + 星级 + 租期 + 评价内容
        lines = [f"「{matched[0].get('name', '')}」的用户评价（共{len(reviews)}条）："]
        for r in reviews[:5]:
            user = r.get("userName", "匿名用户")
            rating = r.get("rating", 0)
            content = r.get("content", "")
            days = r.get("carDays", 0)
            stars = "⭐" * rating
            parts = [f"- {user} {stars}"]
            if days:
                parts.append(f"租了{days}天")
            if content:
                parts.append(f'"{content}"')
            lines.append(" ".join(parts))

        return "\n".join(lines)

    async def _get_hot_cars(self) -> Optional[str]:
        """
        获取热门车型排行 - 调用后端 /cars/hot 接口

        返回按已完成订单数降序排列的前 4 款车型，
        包含排名、品牌、车型名、日租金、座位数、燃料类型、成交单数。

        此数据供前端首页"热门车型"区块展示，
        也供 Agent 在用户询问"推荐车型""什么车好"时引用回答。

        Returns:
            热门车型排行文本
        """
        try:
            resp = await self.http.get("/cars/hot")
            if resp.status_code != 200:
                return "暂无热门车型数据。"
            data = resp.json().get("data", [])
            if not data:
                return "暂无热门车型数据。"

            # 格式化排行文本，每辆车标注排名序号
            lines = ["基于真实成交数据的热门车型排行："]
            for i, c in enumerate(data):
                rank = i + 1
                name = c.get("name", "未知")
                brand = c.get("brand", "")
                price = c.get("rentalPrice") or c.get("price", "未知")
                order_count = c.get("orderCount", 0)
                seats = c.get("seats", "")
                fuel = c.get("fuel", "")
                parts = [f"第{rank}名: {brand} {name}"]
                if price:
                    parts.append(f"日租金: ¥{price}")
                if seats:
                    parts.append(f"{seats}座")
                if fuel:
                    parts.append(f"{fuel}")
                parts.append(f"已完成{order_count}单")
                lines.append(" | ".join(parts))

            return "\n".join(lines)
        except Exception:
            return "获取热门车型数据失败，请稍后再试。"

    # ================================================================
    # 对外统一入口
    # ================================================================

    async def query(self, message: str, token: str = "") -> Optional[str]:
        """
        统一查询入口 - AgentService 调用此方法完成意图识别+数据获取

        流程：用户消息 -> 意图识别 -> 数据获取 -> 返回格式化文本

        Args:
            message: 用户输入的文本消息
            token:   用户登录 Token（可选）

        Returns:
            格式化后的业务数据文本，None 表示无法识别意图（由 LLM 自由回答）
        """
        intent, params = self.detect_intent(message)
        if intent == "none":
            return None
        return await self.fetch_data(intent, params, token)

    async def close(self):
        """关闭 HTTP 连接池，释放网络资源"""
        await self.http.aclose()
