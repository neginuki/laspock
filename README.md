Laspock
=========

Lastaflute のテストケースを Spock で書けるように支援します。

## 機能

- @Resource を指定したフィールドのDI
- 自分で new したクラスのDI
- フィーチャメソッド単位のトランザクション

```groovy
@Laspock
class ExampleSpec extends Specification {

    @Resource
    MemberBhv memberBhv

    def "@Resource を指定したフィールドのDI"() {
        when:
            def name = memberBhv.selectEntityWithDeletedCheck {
                it.specify().columnMemberName()
                it.acceptPK(1)
            }.getMemberName()

        then:
            name == 'システム管理者'
    }
}
```

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
