# 智能企业知识库平台（Intelligent Enterprise Knowledge Base Platform）

> 一个「企业知识库管理系统 + AI 智能助手」的一体化平台。后端基于 **Spring Boot 3** 提供团队协作、文档管理与审核、标签分类、评论收藏等完整业务能力；AI 层基于 **LangGraph ReAct Agent + RAG 向量检索**，让智能客服能够「调用工具查询业务数据 + 检索企业知识库 + 保留多轮对话记忆」。

---

## 一、项目简介

平台解决企业知识沉淀与查询效率低的问题，核心包含两大能力：

| 能力 | 说明 | 技术落地 |
|------|------|----------|
| **知识库管理** | 团队协作、文档全生命周期管理（草稿→审核→发布→归档）、版本回溯、标签分类、评论/点赞/收藏 | Spring Boot 3 + MyBatis + MySQL |
| **AI 智能问答** | 基于 ReAct 推理的智能客服，可调用工具查询团队/文档数据、向量检索知识库生成回答、支持多轮对话记忆 | FastAPI + LangGraph + LangChain + Chroma |

**业务闭环示例**：用户问「帮我查一下团队 ID 为 1 的信息，并用系统中的《三字经》总结团队成员相处之道」→ Agent 自主决定调用 `team_info_query` 工具 + `rag_summarize` 检索知识库 → 组合两条结果输出自然语言回答，且全程保留上下文记忆。

---

## 二、技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                       前端（Vue / 用户端 + 管理端）            │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / SSE
┌──────────────────────────▼──────────────────────────────────┐
│              knowledge_platform（Java 后端）                  │
│  Spring Boot 3.2 + MyBatis + MySQL + JWT + AOP + 分块上传     │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐   │
│  │用户认证   │团队管理   │文档管理   │标签/分类  │评论/收藏  │   │
│  │审核流    │版本管理   │分块上传   │AI会话    │统计概览   │   │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST（/ai/* 工具接口）
┌──────────────────────────▼──────────────────────────────────┐
│          agent-knowledge-platform（Python AI 层）             │
│  FastAPI + LangGraph ReAct Agent + Chroma 向量库 + SQLite 记忆 │
│  ┌──────────┬──────────┬──────────┬──────────────────────┐  │
│  │team 工具  │document │rag_summarize│ ReAct 循环 + 记忆    │  │
│  │          │工具      │RAG 检索   │ (AsyncSqliteSaver)    │  │
│  └──────────┴──────────┴──────────┴──────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**关键链路**：
1. Java 后端通过 **SSE** 将用户问题流式转发给 Python Agent 服务；
2. Agent 按 **ReAct（思考→行动→观察）** 循环，自主决定调用哪个工具；
3. 工具通过携带用户 **JWT token** 回调 Java 后端 `/ai/*` 接口，保证数据权限隔离；
4. 检索类问题走 **RAG**（Chroma 向量库 + 通义 text-embedding-v4），结合 qwen 大模型总结回答；
5. 多轮记忆通过 **LangGraph Checkpointer**（SQLite）持久化，支持会话级上下文。

---

## 三、技术栈

**后端（Java）**
- Spring Boot 3.2.11 / JDK 17 / Maven
- MyBatis 3 + PageHelper 分页、MySQL
- Spring AOP（公共字段自动填充）、JWT（jjwt 双端认证）、Lombok
- SpringDoc OpenAPI（接口文档）、FastJSON、SSE（服务端推送）

**AI 层（Python）**
- FastAPI / Uvicorn
- LangChain + LangGraph（`create_agent` ReAct Agent）
- Chroma 向量数据库、DashScope Embedding（text-embedding-v4）、通义千问（qwen3.5-flash）
- SQLite（`AsyncSqliteSaver` 记忆持久化）

---

## 四、核心功能模块

### 后端（knowledge_platform）

1. **用户 & 认证**：用户/平台管理员双端独立登录，JWT 签发与拦截校验，`ThreadLocal`（BaseContext）线程隔离传递当前用户上下文。
2. **团队管理**：创建/解散、队长邀请 + 成员申请双向审批流、角色（队长/管理员/成员）、人数与创建上限约束、邀请过期机制。
3. **文档全生命周期**：草稿→待审核→已通过/已驳回→发布→归档；可见范围（私有/团队内/公开）；**乐观锁版本管理**，支持历史版本回溯与变更备注。
4. **内容生态**：树形分类、标签体系、多级评论（回复）、点赞/收藏/浏览量统计、用户偏好。
5. **文件上传**：**分块上传 + 断点续传 + 秒传（MD5）**，`init / chunk / progress / merge / cancel` 完整协议。
6. **AI 会话**：会话 CRUD、SSE 流式问答、会话记忆同步。
7. **平台管理**：超管/子管理员、用户管理、文档审核、评论批量管理。
8. **工程基建**：AOP 公共字段自动填充、全局异常处理（30+ 自定义异常）、接口文档、分页封装。

### AI 层（agent-knowledge-platform）

1. **ReAct 智能体**：`create_agent` 构建，可自主规划「思考→调用工具→观察结果→再思考」，最多 5 轮工具调用防失控。
2. **3 个业务工具**：
   - `team_info_query`：查询团队详情与成员权限；
   - `document_info_query`：查询文档元数据、统计与内容；
   - `rag_summarize`：向量检索知识库并总结回答。
3. **RAG 向量检索**：txt/pdf/md 多格式文档加载 → `RecursiveCharacterTextSplitter` 分块 → Chroma 持久化 → 相似度检索 Top-K → 大模型总结（prompt 严格约束「仅基于参考资料回答」）。
4. **记忆系统**：SQLite Checkpointer 持久化多轮对话，支持会话记忆的查询/删除。
5. **工具可观测性**：`wrap_tool_call` / `before_model` 中间件埋点，记录工具调用参数与结果日志。

---

## 五、量化成果

> 说明：以下为**可核对的工程量化指标**（依据代码仓库统计），不含未上线的业务流量指标。若已部署并有真实使用数据，可将最后一项「业务指标」替换为实际数字。

### 工程规模

| 指标 | 数据 |
|------|------|
| 后端 Java 文件数 | **204 个** |
| 后端 Java 代码量 | **11,000+ 行**（另 517 行 MyBatis XML） |
| REST 接口数 | **80+ 个**（18 个 Controller） |
| Service 层 | **16 个接口 + 16 个实现** |
| Mapper / Entity | 16 个 Mapper、17 个实体类 |
| 数据库表 | **15 张 MySQL 表** |
| 自定义业务异常 | **30+ 个** |
| AI 层 Python 代码量 | **1,100+ 行** |

### AI 能力指标

| 指标 | 数据 |
|------|------|
| 可调用工具数 | **3 个**（团队/文档/RAG） |
| ReAct 推理循环 | 最多 5 轮工具调用（防失控熔断） |
| 支持文档格式 | 3 种（txt / pdf / md） |
| 文本分块策略 | chunk_size=200，overlap=20（多分隔符递归切分） |
| 向量检索 | Top-K=3，Chroma 持久化 + **MD5 去重**（避免重复入库） |
| 记忆持久化 | SQLite Checkpointer，支持会话级多轮记忆增删查 |

### 性能 / 可靠性设计指标

| 指标 | 数据 |
|------|------|
| 大文件上传 | 分块上传 + 断点续传 + **秒传**，单块校验，支持进度查询与取消 |
| 文档并发安全 | **乐观锁（version 字段）** 防止并发编辑覆盖 |
| 流式响应 | SSE 服务端推送，禁用 Nginx 缓冲，5 分钟超时 |
| 权限隔离 | 双端 JWT，AI 工具回调携带 token 实现数据行级鉴权 |

## 六、项目结构

```
智能企业知识库平台/
├── knowledge_platform/              # Java 后端（Spring Boot 3）
│   ├── src/main/java/.../
│   │   ├── controller/              # 18 个 Controller（admin / user / ai）
│   │   ├── service/ + service/Impl/ # 16 个业务服务
│   │   ├── mapper/                  # 16 个 MyBatis Mapper
│   │   ├── pojo/                    # entity / dto / vo / inner
│   │   ├── aspect/ interceptor/ handler/ config/  # AOP / JWT / 异常 / 配置
│   │   └── properties/ constant/ enums/ exception/ context/
│   ├── src/main/resources/mapper/   # MyBatis XML
│   └── pom.xml
├── agent-knowledge-platform/        # Python AI 层（FastAPI + LangGraph）
│   ├── main.py                      # FastAPI 入口（chat/stream、memory、archive）
│   ├── core/react_agent.py          # ReAct Agent + Checkpointer 记忆
│   ├── rag/                         # vector_store + rag_service（Chroma / RAG）
│   ├── tools/                       # team / document / rag 三个工具 + middleware
│   ├── model/factory.py             # 大模型 & Embedding 工厂（通义）
│   ├── config/ prompts/             # 配置与 Prompt 工程
│   └── utils/                       # 日志、文件、token 透传等工具
├── script.sql               # 数据库建表脚本（15 张表）
└── data/                            # 样例数据
```

---

## 八、快速启动

### 后端

```bash
# 1. 导入 .sql 初始化 MySQL
# 2. 修改 application.yaml 中的数据库账号密码与文件路径
cd knowledge_platform
mvn spring-boot:run
```

### AI 层

```bash
cd agent-knowledge-platform
# 安装依赖（fastapi / uvicorn / langchain / langgraph / langchain-chroma 等）
# 在 config/rag.yml 配置通义大模型与 Embedding 的 API Key
python main.py   # 默认 8000 端口
```

> 后端 `ai.url.agent-base-url` 默认指向 `你的java端网址`，两端需同时启动。（AI层部分数据需要自己补充修改yaml文件）
