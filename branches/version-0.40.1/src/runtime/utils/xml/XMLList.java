package runtime.utils.xml;

import java.util.ArrayList;
import java.util.Collection;

public class XMLList extends ArrayList<XML> 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public XMLList() {
	}

	public XMLList(int arg0) {
		super(arg0);
	}

	public XMLList(Collection<? extends XML> arg0) {
		super(arg0);
	}
	public XMLList(String strXml) {
		super();
		strXml = "<root>" + strXml + "</root>";
		XML x = new XML(strXml);
		this.addAll(x.getChildren());
		
		
	}
}
