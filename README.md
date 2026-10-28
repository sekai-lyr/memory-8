<div align="center">

# 📖 English Word Learning · 英语单词学习系统

> ⭐ Sekai 系列实战项目 08 / 14 · 单项目独立仓库

![Java](https://img.shields.io/badge/Java-red?style=flat-square&logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Vue 3](https://img.shields.io/badge/Vue3-blue?style=flat-square&logo=vuejs&logoColor=white) ![JPA](https://img.shields.io/badge/JPA-blue?style=flat-square) ![MySQL](https://img.shields.io/badge/MySQL-blue?style=flat-square)

[![Stars](https://img.shields.io/github/stars/sekai-lyr/memory-8?style=flat-square&logo=github)](https://github.com/sekai-lyr/memory-8/stargazers) [![合集](https://img.shields.io/badge/📚_全系列合集-sekai--product-8250DF?style=flat-square)](https://github.com/sekai-lyr/sekai-product)

</div>

---

这是一个完整可运行的英语单词学习 Web 项目。后端使用 Java、Spring Boot、Spring Data JPA、MySQL、Maven；前端使用 Vue 3、Axios 和原生 CSS。当前项目最终访问端口统一为 `8088`。

## 项目结构

后端结构参考 memory-3 的简化风格：`control` 做接口入口，`dataobject` 放数据库对象，`model` 放前后端传输对象和统一返回对象。

```text
memory-8
├── pom.xml
├── README.md
├── database
│   ├── schema.sql
│   └── import-template.csv
├── src/main/java/com/example/wordlearning
│   ├── WordLearningApplication.java
│   ├── config
│   ├── control
│   ├── dataobject
│   ├── model
│   ├── repository
│   ├── service
│   └── util
├── src/main/resources
│   ├── application.properties
│   └── static
└── frontend
    ├── package.json
    ├── vue.config.js
    └── src
```

## 核心文件作用

- `control/*`：REST 接口入口。
- `dataobject/*DO.java`：数据库对象，对应 `sekai_word_` 前缀的数据表。
- `model/*`：请求 DTO、响应 DTO、统一返回对象。
- `repository/*`：Spring Data JPA 数据库操作。
- `service/*` 和 `service/impl/*`：业务逻辑。
- `util/AnswerJudgeUtil.java`：答案清洗和多答案完整匹配。
- `util/CsvImportUtil.java`：CSV 词库导入工具。
- `database/schema.sql`：MySQL 建表 SQL 和示例公共单词。
- `frontend/src/views/*`：Vue 页面。

## 运行步骤

1. 创建并导入数据库：

```bash
mysql -uroot -p < database/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 sekai_friend < database/cet6_words.sql
mysql -uroot -p --default-character-set=utf8mb4 sekai_friend < database/word_bank_booster.sql
```

如需直接导入大型开源英中词库，可下载 ECDICT 的 `ecdict.csv` 后执行：

```bash
python database/import_ecdict.py --csv /path/to/ecdict.csv
python database/cleanup_word_meanings.py
```

2. 检查数据库连接：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/sekai_friend?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
server.port=8088
```

3. 打包前端到 Spring Boot 静态目录：

```bash
cd frontend
npm install
npm run build
```

然后将 `frontend/dist` 中的文件放入：

```text
src/main/resources/static
```

本项目当前已经完成这一步。

4. 启动后端：

```bash
mvn spring-boot:run
```

5. 浏览器访问：

```text
http://localhost:8088/login
```

说明：本项目当前按你的要求统一使用 `8088`。开发时也可以运行 `npm run serve`，但最终展示和你浏览器访问的项目效果都以 Spring Boot 的 `8088` 为准。

## 测试步骤

1. 打开 `http://localhost:8088/login`。
2. 注册账号并登录。
3. 登录后进入单词练习页。
4. 测试随机单词，后端随机接口只返回英文。
5. 对 `various` 输入 `多方面的`，应判定正确。
6. 输入错误答案，应显示正确答案并加入错题本。
7. 在自定义词库新增单词，刷新后仍能看到，说明已经持久化到 MySQL。
8. 在练习页切换“公共词库 / 只练错题 / 自定义词库”模式。
9. 在错题本页面测试删除、清空、CSV 导出。
10. 在学习记录页查看每轮练习统计。

## 学习总结

用户打开前端页面后，Vue 通过 Axios 请求后端接口。后端 `control` 接收请求，调用 `service` 处理业务逻辑，`service` 再通过 `repository` 访问 MySQL。数据库结果返回后，后端组装 `model` DTO，通过统一格式返回给前端，前端再渲染页面。

随机出题时，前端请求 `/api/words/random` 或一轮练习接口。后端随机查询公共词库，只通过 DTO 返回 `id` 和 `english`，不会提前暴露中文答案。

答案校验时，前端提交 `wordId`、`wordType` 和 `answer`。后端查出正确中文释义，用 `AnswerJudgeUtil` 去除首尾空格、句号、冒号，并按中文分号、英文分号、顿号、逗号等拆分多个答案。判断时只做完整释义匹配，不用 `contains`，所以“多方面的”能对，“面的”不会误判。

错题本流程是：答错后前端调用错题接口，后端保存或累加 `wrong_count`。错题复习时，前端从错题本读取单词并组成练习队列。

智能查询不需要手动切换模式。输入英文时按英文忽略大小写查询中文；输入中文时按中文模糊查询英文。

注册时后端使用 BCrypt 保存加密密码；登录时校验密码，成功后前端保存用户信息，并在请求头携带 `X-User-Id`，后端据此区分每个用户自己的错题本、收藏、学习记录和自定义词库。

几个核心概念可以这样理解：

- `dataobject` 是数据库对象。
- `model` 是前后端传输对象。
- `control` 是接口入口。
- `service` 是业务逻辑。
- `repository` 是数据库操作。

后续可以继续扩展发音功能、音标播放、例句朗读、学习日历、高频错题优先复习、Excel 导出错题本、AI 或向量语义判断等。

---

## 🗺️ Sekai 项目全景导航

本项目是 **Sekai 系列作品集** 的一员，全部 14 个实战项目：

| 项目 | 简介 |
|------|------|
| [`memory-1`](https://github.com/sekai-lyr/memory-1) | 📊 二次元数据中心 |
| [`memory-2`](https://github.com/sekai-lyr/memory-2) | 🛒 二次元电商商城 |
| [`memory-3`](https://github.com/sekai-lyr/memory-3) | 📔 二次元回忆录 |
| [`memory-4`](https://github.com/sekai-lyr/memory-4) | 🎋 角色抽签与今日运势 |
| [`memory-5`](https://github.com/sekai-lyr/memory-5) | 🎵 歌词同步音乐播放器 |
| [`memory-6`](https://github.com/sekai-lyr/memory-6) | 🎮 原创角色合成小游戏 |
| [`memory-7`](https://github.com/sekai-lyr/memory-7) | 🖼️ 二次元壁纸图库系统 |
| [`memory-8`](https://github.com/sekai-lyr/memory-8) | 📖 英语单词学习系统 |
| [`memory-9`](https://github.com/sekai-lyr/memory-9) | 📝 表单收集与 AI 对话系统 |
| [`memory-10`](https://github.com/sekai-lyr/memory-10) | 📺 动漫视频导航站 |
| [`memory-11`](https://github.com/sekai-lyr/memory-11) | ⚔️ 游戏王规则卡牌对战 |
| [`memory-12`](https://github.com/sekai-lyr/memory-12) | 🎓 游戏化学习平台 |
| [`memory-13`](https://github.com/sekai-lyr/memory-13) | 🎭 Ren'Py 视觉小说 |
| [`memory-14`](https://github.com/sekai-lyr/memory-14) | 🤖 识番推荐与 RAG 助手平台 |

📦 **一站式浏览全部项目**：[sekai-lyr/sekai-product](https://github.com/sekai-lyr/sekai-product)（monorepo 合集）

<div align="center">

**如果这个项目对你有帮助或启发，欢迎点一个 ⭐ Star 支持一下！**

*Made with ❤️ by [sekai-lyr](https://github.com/sekai-lyr)*

</div>
