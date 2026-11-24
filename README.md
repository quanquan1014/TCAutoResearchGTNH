# TC Auto Research GTNH

GTNH 2.9.0 的神秘时代（Thaumcraft）研究台辅助模组。

## 功能

- 工具条：自动完成 / 手动求解 / 停止 / 加权·最短 / 解锁要素 / 还原权重 / 权重设置 / 批量研究 / 搜索研究 / 完成报告 / 设置，悬停显示说明。
- 自动完成：放入笔记后自动求解并放置，可暂停/继续。
- 手动求解：后台计算 → 预览棋盘与线路 → 确认后放置；求解失败时自动移除不合理的已放置要素再重试。
- 解锁要素：通过研究台依次合成并解锁缺失要素。
- 权重设置：调整单个要素成本、禁用要素、套用 Wiki 默认、按库存分配；方案可保存/加载。
- 批量研究：处理背包中全部未完成笔记。
- 搜索研究：按目录检索未研究项目，支持生成笔记、解锁目标及其前置、单独获取前置笔记。
- 完成报告与棋盘预览。
- 设置：调整工具条缩放（0.5 ~ 2.0）与位置（按住拖动，可恢复默认）。

## 版本

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
