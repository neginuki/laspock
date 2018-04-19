Laspock
=========

Lastaflute のテストケースを Spock で書けるように支援します。

## 機能

- フィーチャメソッド単位のトランザクション
- @Resource を指定したフィールドのDI
- 自分で new したクラスのDI


## 設定

**build.gradle**
```
apply plugin: 'groovy'

repositories {
    // ...
    maven {url 'https://neginuki.github.io/laspock'}
}

dependencies {
    // ...
    testCompile "world.sake:laspock:0.0.15"
}
```
