package input.parser.impl;

import input.model.Section;
import input.model.impl.CompositeSection;
import input.model.impl.UrlSection;
import input.parser.SadParser;
import input.xmltemplatereader.TemplateStructure;
import input.xmltemplatereader.XmlReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Source;

public class WikiParser implements SadParser {

	private static final String SLASH = "/";
	private String urlBase;
	private int lastIndexValid;
	
	
	public Section getSad(String path, String urlWiki) {
		
		URL url= null;
		try {
			 url= new URL(urlWiki);
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		}
		
		urlBase= url.getProtocol()+"://"+url.getHost();
		Source source=null;
		try {
			source = new Source(url);
		} catch (IOException e) {
			
			e.printStackTrace();	
			}
		
		CompositeSection document=  generateStructure(source,path);
		
		
		return document;
	}

	private CompositeSection generateStructure(Source source,String templatePath) {
		
		
		List<TemplateStructure> templateFormat = new XmlReader(templatePath).getPlainStructure();
		CompositeSection document= new CompositeSection();		
	
		List<Attribute> urls = source.getURIAttributes();

		validateUrls(templateFormat,urls,document);	
		
		if(document.getSubSections() != null && cantNodeLink(templateFormat) > document.getSubSections().size()){
			return null;
		}		

		return document;
	}
	
	private int cantNodeLink(List<TemplateStructure> templateFormat) {
		int cant=0;
		for (Iterator iterator = templateFormat.iterator(); iterator.hasNext();) {
			TemplateStructure templateStructure = (TemplateStructure) iterator.next();
			if(templateStructure.hasLink()){
				cant++;
			}
			
		}
		
		return cant;
	}


	private void validateUrls(List<TemplateStructure> template,
			List<Attribute> urls,CompositeSection document ) {

		TemplateStructure firstItem = template.get(0);
		descartarUrls(firstItem, urls);
		int urlIndex = 0;
		int templateIndex = 0;		
		
		boolean valid = urls.size() > 0 && validateAgainstTemplate(document, template, templateIndex,urls, urlIndex);

		if (valid) {
			System.out.println("Estructura VAlida");
			descartarUrlsSobrantes(urls, this.lastIndexValid);			
		} else {
			document = null;
		}
	}
	

	private void descartarUrls(TemplateStructure templateItem,
			List<Attribute> urls) {
		boolean matching = false;
		int indexFirstMatch = 0;
		for (; !matching && indexFirstMatch < urls.size(); indexFirstMatch++) {
			if (matching(templateItem, urls.get(indexFirstMatch))) {
				matching = true;
			} else {
				urls.remove(indexFirstMatch);
				indexFirstMatch--;
			}
		}

	}
	
	private boolean validateAgainstTemplate(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {

		boolean valid = false;
		UrlSection urlSection = new UrlSection(urlBase);
		TemplateStructure templateNode = template.get(templateIndex);
		// Si nodo en el template es un item
		if (templateNode.isItem()) {
			document.addSection(urlSection);
			//item lleno
			if(templateNode.isWritten()){
				urlSection.setAtt(urls.get(urlIndex),templateNode.getTitle());
				if(templateNode.isChildLink()){
					//Item con link hijos					
					templateIndex++;
					valid= validateChildLinks(document,template,templateIndex, urls, urlIndex);
				}else{
					//Item normal
					valid = validateItem(document,template,templateIndex,urls,urlIndex);
				}				

			}else{//Item con link pendiente
					urlSection.setAttNotWritten(urls.get(urlIndex),templateNode.getTitle());
				
					valid = validateItemNotWritten(document,template,templateIndex,urls,urlIndex);
				 }
		} else {// Seccion con link escrito
			if (!templateNode.isLinkType()&& !templateNode.isItem() && templateNode.hasLink() && templateNode.isWritten()) {
				document.addSection(urlSection);
				urlSection.setAtt(urls.get(urlIndex),templateNode.getTitle());
				valid = validateSectionWithLink(document, template,
						templateIndex, urls, urlIndex);

			}else
			{	//Seccion con link sin completar
				if(!templateNode.isLinkType()&&!templateNode.isItem() && templateNode.hasLink() && !templateNode.isWritten()){
					document.addSection(urlSection);
					urlSection.setAttNotWritten(urls.get(urlIndex),templateNode.getTitle());
					valid = validateSectionWithLinkNotWritten(document,template,templateIndex,urls,urlIndex);
						
				}			
			else {
				// Seccion sin link
				if (!templateNode.isLinkType()&&!templateNode.hasLink()) {
					valid = validateSectionWithoutLink(document,template,templateIndex,urls,urlIndex);
				   }
							
			    }

			}
		}
		return valid;

	}

	private boolean validateChildLinks(CompositeSection document, List<TemplateStructure> template, int templateIndex, List<Attribute> urls, int urlIndex) {
		int index=0;
		String sectionUrl= urls.get(urlIndex).getValue();
		try{
			URL url = new URL(urlBase+sectionUrl);
			Source source=new Source(url);
			List<Attribute> urlsChildLink = source.getURIAttributes();
			descartarUrls(template.get(templateIndex), urlsChildLink);
			int urlsChildLinkIndex=0;
			index= validateCHildLinksAgainstTemplate( document,template, templateIndex, urlsChildLink, urlsChildLinkIndex);
			if(index!=-1){
				return validateAgainstTemplate(document, template, index, urls, ++urlIndex);
		   }
		}catch (Exception e) {
			return false;
		 }
		return false;
	}

	private int validateCHildLinksAgainstTemplate(
			CompositeSection document, List<TemplateStructure> template,
			int templateIndex, List<Attribute> urlsChildLink,
			int urlsChildLinkIndex) {
		
		int index = 0;
		boolean valid=false;
		
		for (; templateIndex < template.size() && !valid;templateIndex++) {
			UrlSection urlSection = new UrlSection(urlBase);
			TemplateStructure templateNode = template.get(templateIndex);
			if (templateNode.isLinkType()) {
				if (matching(templateNode, urlsChildLink.get(urlsChildLinkIndex))) {
					urlSection.setAtt(urlsChildLink.get(urlsChildLinkIndex),templateNode.getTitle());
					document.addSection(urlSection);
					urlsChildLinkIndex++;				
					
				}else{
					return -1;
				}
			}else{
				index=templateIndex;
				valid=true;
			}
			
		}	
		
		return index;

	}
	private boolean validateSectionWithLinkNotWritten(
			CompositeSection document, List<TemplateStructure> template,
			int templateIndex, List<Attribute> urls, int urlIndex) {
		
		boolean valid = false;
		TemplateStructure templateNode = template.get(templateIndex);
		Attribute att= urls.get(urlIndex);
		String urlRaw=  att.getValue();
		
		if (urlRaw.contains("action=edit"))	{

			// Seccion VAcia
			if (templateNode.isEmpty()) {
				/**
				 * Agrego urls hasta el proximo matching...
				 */
				valid = validateEmptySection(document,template,templateIndex,urls,urlIndex);

			} else {
				// no puedo preguntar por el siguiente del template y de las
				// url.. porq el template tambien es lineal. Ver de usar las dos
				// estructuras mezcladas.

				if (templateIndex < template.size() - 1	&& urlIndex < urls.size() - 1) {
					// debo validar hijos y luego avanzar. COmo la estructura
					// del template es lineal. Simplemente llamo recursivamente
					// avanzando en las dos estructuras
					valid = validateAgainstTemplate(document,template,++templateIndex,urls,++urlIndex);
				} else {

					if (templateIndex < template.size() - 1) {
						valid = true;
						this.lastIndexValid = urlIndex;
					} else {
						// Recorri las urls completas. Estructura invalida
						valid = false;
					}

				}
			}

		} else {
			valid = false;
		}
		return valid;
	}

	

	private boolean validateSectionWithoutLink(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {

		boolean valid = false;
		// No hay con que hacer matching por eso no se llama a dicha funcion

		// Si seccion vacia recorro hasta el prox matching
		if (template.get(templateIndex).isEmpty()) {
			/**
			 * Agrego urls hast ael proximo matching...
			 */
			valid = validateEmptySection(document, template, templateIndex,
					urls, urlIndex);

		} else {// Sin link y con hijos. se llama recursivamente solo avanzando
				// en el template porq como no tiene link no hay que avanzar en
				// la pagina

			valid = validateAgainstTemplate(document,template,++templateIndex,urls,urlIndex);

		}
		return valid;
	}
	
	private boolean validateItem(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {
		boolean ret;

		TemplateStructure templateNode = template.get(templateIndex);
		if (matching(templateNode, urls.get(urlIndex))) {

			// Avanzo en ambas estructuras *1
			if (templateIndex < template.size() - 1
					&& urlIndex < urls.size() - 1) {
				ret = validateAgainstTemplate(document, template,
						++templateIndex, urls, ++urlIndex);
			} else {
				// Recorri el template entero. Estructura valida
				if (templateIndex == template.size() - 1) {
					ret = true;
					this.lastIndexValid = urlIndex;
				} else {
					// Recorri las urls completas. Estructura invalida
					ret = false;
				}
			}
		} else {
			// Corto. estructura invalida
			ret = false;
		}
		return ret;
	}
	
	private boolean matching(TemplateStructure templateItem, Attribute url) {

		// Reemplazo los espacios en blanco en los titulos del template por
		// guines bajpos para pder comparar con la url en la wiki
		String title = templateItem.getTitle().replaceAll(" ", "_");
		// Obtengo la url del tag actual
		String rawUrl = url.getValue().substring(1, url.getValue().length());
		// Me quedo con el final despues de la ultima barra
		String[] linkSplit = rawUrl.split(SLASH);
		String link = linkSplit[linkSplit.length - 1];

		/**
		 * A veces el link en la url tiene agregado adelante el nombre del
		 * proyecto o algun otro valor entonces busco las diferentes palabras
		 * del titulo del template en la url
		 */

		// String[] titlesPotentials= templateItem.getTitle().split(" ");
		// String titlePotential="";
		//
		// for (int i = 0; i < titlesPotentials.length; i++) {
		// titlePotential= titlesPotentials[i];
		// if(link.contains(titlePotential)){
		// return true;
		// }
		//
		//
		// }

		return link.toLowerCase().contains(title.toLowerCase());
	}
	
	private boolean validateSectionWithLink(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {

		boolean valid = false;
		TemplateStructure templateNode = template.get(templateIndex);
		if (matching(templateNode, urls.get(urlIndex))) {

			// Seccion VAcia
			if (templateNode.isEmpty()) {
				/**
				 * Agrego urls hast ael proximo matching...
				 */
				valid = validateEmptySection(document, template, templateIndex,
						urls, urlIndex);

			} else {
				// no puedo preguntar por el siguiente del templat y de las
				// url.. porqel template tambien es lineal. Ver d eusar las dos
				// estructuras mezcladas.

				if (templateIndex < template.size() - 1
						&& urlIndex < urls.size() - 1) {
					// debo validar hijos y luego avanzar. COmo la estructura
					// del template es lineal. Simplemente llamo recursivamente
					// avanzando en las dos estructuras

					valid = validateAgainstTemplate(document, template,
							++templateIndex, urls, ++urlIndex);

				} else {

					if (templateIndex < template.size() - 1) {
						valid = true;
						this.lastIndexValid = urlIndex;
					} else {
						// Recorri las urls completas. Estructura invalida
						valid = false;
					}

				}
			}

		} else {
			valid = false;
		}
		return valid;
	}
	
	private boolean validateEmptySection(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {

		boolean ret = false;

		if (templateIndex < template.size() - 1) {

			templateIndex = getNextItemToMatch(template, templateIndex);
			TemplateStructure templateItemNext = template.get(templateIndex);
			urlIndex = addUrlUntilNextMatch(document, templateItemNext, urls,
					urlIndex);
			ret = validateAgainstTemplate(document, template, templateIndex,
					urls, urlIndex);

		} else {

			// Si entra aca se deben validar el resto de las urls. Ver la forma
			// de solucionar porque va agregar todas las que siguen a
			// continuacion
			urlIndex = addRemainingUrls(document, urls, urlIndex);
			ret = true;
			this.lastIndexValid = urlIndex;

		}
		return ret;
	}

	/**
	 * Retorna el proximo elemento con el cual hacer matching. Avanza hasta que
	 * el nodo del template tenga un link
	 * 
	 * @param templateIter
	 * @return
	 */

	private int getNextItemToMatch(List<TemplateStructure> template, int index) {

		TemplateStructure ret = template.get(index);
		while (!ret.hasLink()) {
			if (index < template.size())
				ret = template.get(++index);
		}

		return index;
	}
	
	private void descartarUrlsSobrantes(List<Attribute> urls, int index) {

		while (index < urls.size() - 1) {
			urls.remove(index);

		}

	}
	/**
	 * Avanza en el iterador mientra no haga matching.
	 * 
	 * @param wrapper
	 * @param templateItemNext
	 * @param urlsIter
	 */
	private int addUrlUntilNextMatch(CompositeSection document,
			TemplateStructure templateItemNext, List<Attribute> urls,
			int urlIndex) {

		// Mientras haya urls y no haga matching sigue avanzando ene el iterador
		while (urlIndex < urls.size()
				&& !matching(templateItemNext, urls.get(urlIndex))) {
			UrlSection child = new UrlSection(urlBase);
			child.setAtt(urls.get(urlIndex),templateItemNext.getTitle());
			document.addSection(child);
			urlIndex++;
		}
		return urlIndex;

	}
	
	/**
	 * Avanza hasta el final del iterador
	 * 
	 * @param wrapper
	 * @param urlsIter
	 */
	private int addRemainingUrls(CompositeSection document, List<Attribute> urls,
			int urlIndex) {

		while (urlIndex < urls.size()) {
			UrlSection child = new UrlSection(urlBase);
			String title= getTitle(urls.get(urlIndex));
			child.setAtt(urls.get(urlIndex),title);
			document.addSection(child);
			urlIndex++;
		}

		return urlIndex;

	}

	private String getTitle(Attribute attribute) {
		String rawUrl = attribute.getValue().substring(1, attribute.getValue().length());
		// Me quedo con el final despues de la ultima barra
		String[] linkSplit = rawUrl.split(SLASH);
		String link = linkSplit[linkSplit.length - 1];
		
		String title =link.replaceAll("_"," ");
		return title;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
//	public static void main(String[] args) throws IOException {
//		
//		double t1, t2;
//        t1 = System.currentTimeMillis();
//		
//		String path= "https://wiki.sei.cmu.edu/sad/index.php/The_Java_Pet_Store_SAD";
//		
//		SadParser parser= SadParserFactory.getParser(SadParserFactory.WIKI);
//		
//		CompositeSection doc= (CompositeSection) parser.getSad(path);
//		
//     
//		t2= System.currentTimeMillis();
//		
//		System.out.println("Tiempo de ejecucion: "+(t2-t1)/1000.0);
//
//	}
	
	private boolean validateItemNotWritten(CompositeSection document,
			List<TemplateStructure> template, int templateIndex,
			List<Attribute> urls, int urlIndex) {
		boolean ret;
	
		Attribute att= urls.get(urlIndex);
		String urlRaw=  att.getValue();
		if (urlRaw.contains("action=edit")) {

			// Avanzo en ambas estructuras *1
			if (templateIndex < template.size() - 1
					&& urlIndex < urls.size() - 1) {
				ret = validateAgainstTemplate(document, template,
						++templateIndex, urls, ++urlIndex);
			} else {
				// Recorri el template entero. Estructura valida
				if (templateIndex == template.size() - 1) {
					ret = true;
					this.lastIndexValid = urlIndex;
				} else {
					// Recorri las urls completas. Estructura invalida
					ret = false;
				}
			}
		} else {
			// Corto. estructura invalida
			ret = false;
		}
		return ret;
	}

}
