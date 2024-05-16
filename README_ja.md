# ZombiesMode
Hypixel Zombies のプレイを強化するクライアントMod<br>
このModは完全に非公式です。自己責任でご利用ください。

## 主な機能
- ダメージコンボ数
- 残り弾数の可視化
- リロードアニメーション
- クリティカルヒット演出
- 蘇生時間ゲージ
- 討伐進捗ゲージ
- プレイヤーの自動透過
- ゲーム内メッセージの整頓
- Zombiesモードの検知 (検知していない場合にこのModが極力動作しないようになっています)


## 前提
- Legacy Fabric
- Minecraft 1.8.9


## デバッグ用サブコマンド
コマンド: `/myZombies (subCommand)`, `/myZm (subCommand)`<br>

- forceInGame <true/false> - Zombiesモードの有効性を強制的に変更
- disableHotbarRender <true/false> - ホットバー描画の有効性
- disablePlayerVisibility <true/false> - プレイヤー透過機能の有効性
- disableChatMixin <true/false> - チャット処理の有効性
- test - 実験用
- modeJoin - Zombiesに参加したことにする
- modeQuit - Zombiesを退出したことにする
- dumpItems - インベントリの全アイテムのNBTをファイルに書き出し
