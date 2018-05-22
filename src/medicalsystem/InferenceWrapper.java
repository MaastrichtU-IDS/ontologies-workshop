package medicalsystem;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class InferenceWrapper {
	private static ManchesterOWLSyntaxOWLObjectRendererImpl man = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private OWLClassExpression cls;
	
	public InferenceWrapper(OWLClassExpression c) {
		cls = c;
	}
	
	public OWLClassExpression getClassExpression() {
		return cls;
	}
	
	public String toString() {
		String result = man.render(cls);
		int index = result.indexOf("Pizza");
		if (index != -1)
			return "This pizza is " + result.substring(0, index);
		return result;
	}

}
