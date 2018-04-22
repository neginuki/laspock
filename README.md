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

    def "自分で new したクラスのDI"() {
        given:
            def tim = new TestInjectManager()
            def bhv = tim.di new MemberBhv()

        when:
            def name = bhv.selectEntityWithDeletedCheck {
                it.specify().columnMemberName()
                it.acceptPK(1)
            }.getMemberName()

        then:
            name == 'システム管理者'
    }

    def "フィーチャメソッド単位のトランザクション"() {
        given:
            int maxId = memberBhv.selectScalar(Integer.class).max { it.specify().columnMemberId()}.orElse(0)

            def member = new Member()
            member.with {
                memberName = 'newMember'
                email = 'email@emai.com'
                password = 'pass123'
                statusCode = 'OK'
                displayOrder = 1
            }

        when:
            memberBhv.insert member

        then:
            member.memberId > maxId
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
