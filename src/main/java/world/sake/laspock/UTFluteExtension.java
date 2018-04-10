package world.sake.laspock;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

/**
 * @author neginuki
 */
public class UTFluteExtension extends AbstractAnnotationDrivenExtension<Laspock> {

    @Override
    public void visitSpecAnnotation(Laspock annotation, SpecInfo spec) {
        //spec.addInterceptor(new UTFluteIntercepctor());

        UTFluteInterceptor intercepctor = new UTFluteInterceptor();
        spec.addSetupInterceptor(intercepctor);
        spec.addCleanupInterceptor(intercepctor);
    }
}
