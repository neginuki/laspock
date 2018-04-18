Laspock
=========

Lastaflute のテストケースを Spock で書けるように支援します。

## build.gradle
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
