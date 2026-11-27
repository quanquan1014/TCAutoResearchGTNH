# TC Auto Research GTNH

GTNH 的神秘时代（Thaumcraft）研究台辅助模组。

## 功能

- 工具条：自动完成 / 手动求解 / 停止 / 加权·最短 / 解锁要素 / 还原权重 / 权重设置 / 批量研究 / 研究目录 / 完成报告 / 设置，悬停显示说明。
- 自动完成：放入笔记后自动求解并放置，可暂停 / 继续。
- 手动求解：后台计算 → 预览棋盘与线路 → 确认后放置；预览中可切换加权 / 最短；求解失败时自动移除不合理的已放置要素再重试。
- 解锁要素：通过研究台依次合成并解锁缺失要素。
- 权重设置：调整单个要素成本、禁用要素、套用 Wiki 默认值、按库存分配；方案可保存 / 加载。
- 批量研究：处理背包中全部未完成笔记。
- 研究目录：按目录浏览研究项目，显示研究带来的扭曲值；前置未完成时悬停显示缺失的前置；双击前置未完成的研究直接打开「解锁所选及其前置」方案，双击前置齐全的研究生成笔记；支持解锁范围内全部可研究、解锁范围内无扭曲研究。
- 完成报告与棋盘预览。
- 设置：调整工具条缩放（0.5 ~ 2.0）与位置（按住拖动，可恢复默认）。

## 前置模组

- **GTNH 2.9.x**（整合包本体）
- **Thaumcraft**（GTNH 内置的神秘时代 fork）
- **thaumcraft-research-tweaks**（GTNH 内置的研究台重制模组）

以上均为 GTNH 整合包自带内容，无需单独安装。

## 兼容性

- GTNH **2.9.0 / 2.9.1 / 2.9.2**：已适配。
- GTNH **2.8.0**：Thaumcraft API 与容器结构一致，理论兼容，未实测；如遇问题请附日志反馈。

## 版本

- 1.2.0：研究目录支持双击前置未完成的研究打开「解锁所选及其前置」；前置方案界面双击条目可单独获取该研究笔记；生成笔记后列表保持原滚动位置；权重方案未填写名称时提示「请先输入方案名称」。
- 1.1.0：研究目录新增扭曲显示、前置未完成悬停提示、解锁范围内无扭曲研究；「搜索研究」更名为「研究目录」。
- 1.0.0：首个正式版本。

## 构建

需要 JDK 8（toolchain 要求 `vendor=Azul Zulu`）、Gradle 8.x，以及可访问 `nexus.gtnewhorizons.com` 的网络。

```bash
gradle build
```

产物在 `build/libs/`，放入客户端 `mods/` 目录。

Thaumcraft（GTNH fork）为编译期依赖，不随仓库分发。

## 配置

文件 `config/tcautoresearchgtnh.cfg`：

- `general.AutoResearch` 自动完成
- `general.WeightedMode` 加权模式
- `general.InventoryAware` 库存感知
- `general.PreviewBeforeSolving` 执行前预览
- `general.ReportBoardPreview` 报告中显示棋盘
- `general.DisabledAspects` 禁用要素
- `general.GuiScale` 工具条缩放
- `general.ToolbarX` / `general.ToolbarY` 工具条位置
- `weighted.<tag>` 单个要素成本

权重方案存于 `config/TCAutoResearchGTNH-weight-profiles.json`。

## 目录结构

```
src/main/java/com/emil/tcautoresearchgtnh/
  core/            默认权重表（无 Minecraft 依赖）
  research/        求解器与控制器
  client/          GUI 与 Forge 事件
  mixins/          Mixin（鼠标点击、容器传输回包）
  proxy/           代理
  Config.java      配置
```

## 授权

MIT，见 [LICENSE](LICENSE)。上游归属见 [NOTICE](NOTICE)。
