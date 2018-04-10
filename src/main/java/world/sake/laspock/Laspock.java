package world.sake.laspock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;

/**
 *  Spock 上で以下をサポートする。
 *
 *  <ul>
 *    <li>LastaDI の初期化
 *    <li>テストメソッドのトランザクション（終了でロールバック）
 *    <li>フィールドのDI
 *  </ul>
 *  @author neginuki
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(UTFluteExtension.class)
public @interface Laspock {
}
