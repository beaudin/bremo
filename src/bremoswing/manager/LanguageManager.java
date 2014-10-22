package bremoswing.manager;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LanguageManager {
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	Document document;
	Element root ;
	NodeList rootChildList;
	Element  JLABELS;
	Element  JBUTTONS;
	Element  STRINGS;
	NodeList JLabelsNodeList;
	NodeList JButtonsNodeList;
	NodeList StringsNodeList;
	
	public LanguageManager(String language) {
		
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		    setDocument(new File("./src/bremoswing/ressource/String_"+language.toLowerCase()+".xml"));
		    setRoot();
		    setRootChildList();
		    setJLABELS();
            setJBUTTONS();
            setSTRINGS();
            setJLabelsNodeList();
            setJButtonsNodeList();
            setStringsNodeList();
            
            System.err.println(getJLabelTextByID("swingbremo_label_1"));
            System.err.println(getJLabelTextByID("swingbremo_label_2"));
            System.err.println(getJLabelTextByID("swingbremo_label_3"));
		    
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	public void setDocument(File file) throws SAXException, IOException {
		setDocument(builder.parse(file));
	}

	public Element getRoot() {
		return root;
	}

	public void setRoot(Element root) {
		this.root = root;
	}
	
	public void setRoot() {
		setRoot(document.getDocumentElement());
	}

	public NodeList getRootChildList() {
		return rootChildList;
	}

	public void setRootChildList(NodeList rootChildList) {
		this.rootChildList = rootChildList;
	}
	
	public void setRootChildList() {
		setRootChildList(root.getChildNodes());
	}

	public Element getJLABELS() {
		return JLABELS;
	}

	public void setJLABELS(Element jLABELS) {
		JLABELS = jLABELS;
	}
	
	public void setJLABELS() {
		setJLABELS((Element) root.getElementsByTagName("JLABELS").item(0));
	}
	
	public Element getJBUTTONS() {
		return JBUTTONS;
	}

	public void setJBUTTONS(Element jBUTTONS) {
		JBUTTONS = jBUTTONS;
	}
	
	public void setJBUTTONS() {
		setJBUTTONS((Element) root.getElementsByTagName("JBUTTONS").item(0));
	}

	public Element getSTRINGS() {
		return STRINGS;
	}

	public void setSTRINGS(Element sTRINGS) {
		STRINGS = sTRINGS;
	}
	
	public void setSTRINGS() {
		setSTRINGS((Element) root.getElementsByTagName("STRINGS").item(0));
	}

	public NodeList getJLabelsNodeList() {
		return JLabelsNodeList;
	}

	public void setJLabelsNodeList(NodeList jLabelsNodeList) {
		JLabelsNodeList = jLabelsNodeList;
	}
	
	public void setJLabelsNodeList() {
        setJLabelsNodeList(JLABELS.getElementsByTagName("jlabel"));
	}

	public NodeList getJButtonsNodeList() {
		return JButtonsNodeList;
	}

	public void setJButtonsNodeList(NodeList jButtonsNodeList) {
		JButtonsNodeList = jButtonsNodeList;
	}
	
	public void setJButtonsNodeList() {
		setJButtonsNodeList(JBUTTONS.getElementsByTagName("jbutton"));
	}

	public NodeList getStringsNodeList() {
		return StringsNodeList;
	}

	public void setStringsNodeList(NodeList stringsNodeList) {
		StringsNodeList = stringsNodeList;
	}
	
	public void setStringsNodeList() {
		setStringsNodeList(STRINGS.getElementsByTagName("string"));
	}
	
	public String getJLabelTextByID(String id ) {
		String text = "text";
		for ( int i = 0 ; i < JLabelsNodeList.getLength(); i++ ) {
			Element element = (Element) JLabelsNodeList.item(i) ;
			if (element.getAttribute("id").equals(id)) {
			   text = element.getTextContent();
			   break;
			}
		}
		return text;
	}
	
    public String getJButtonTextByID(String id ) {
    	String text = "";
		for ( int i = 0 ; i < JButtonsNodeList.getLength(); i++ ) {
			Element element = (Element) JButtonsNodeList.item(i) ;
			if (element.getAttribute("id").equals(id)) {
			   text = element.getTextContent();
			   break;
			}
		}
		return text;
		
	}
    
    public String getStringTextByID(String id ) {
    	String text = "";
		for ( int i = 0 ; i < StringsNodeList.getLength(); i++ ) {
			Element element = (Element) StringsNodeList.item(i) ;
			if (element.getAttribute("id").equals(id)) {
			   text = element.getTextContent();
			   break;
			}
		}
		return text;
	}
}
