package edu.isistan.uima.unified.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.internal.util.UUIDGenerator;


/**
 * 
 * @author Marcos Basso
 * @author Eduardo Solis
 *
 */
public class CompositeSection extends Section{
	
	private static final String SECTION_ID = "COMPOSITE_SECTION_ID";
	private List<Section> subSections;
	
	
	public CompositeSection(){
		subSections = new ArrayList<Section>();
	}
	
	
	public void addSection(Section s){
		subSections.add(s);
	}
	
	public void removeSection(Section s){
		subSections.remove(s);
	}
	
	public List<Section> getSubSections(){
		return subSections;
	}



	public List<Section> getSections() {
		List<Section> list = new ArrayList<Section>();
		Item item = new Item();
		item.setName(this.getName());
		item.setText(this.getText());
		list.add(item);
		for(int i=0; i < subSections.size();i++){			
			Section s = subSections.get(i);
			list.addAll(s.getSections());
		}
		return list;
		
	}


	public String getId() {
	
		return SECTION_ID + UUIDGenerator.generate();
	}
}
