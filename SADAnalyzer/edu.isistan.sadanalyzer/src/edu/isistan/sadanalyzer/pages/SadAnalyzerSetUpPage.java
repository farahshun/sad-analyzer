package edu.isistan.sadanalyzer.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wb.swt.ResourceManager;

import SadModel.Sad;

import edu.isistan.reassistant.ccdetector.model.CrosscuttingConcernRuleSet;
import edu.isistan.sadanalyzer.editor.Messages;
import edu.isistan.sadanalyzer.editor.SadAnalyzerEditor;
import edu.isistan.sadanalyzer.model.SadAnalyzerProject;
import edu.isistan.sadanalyzer.providers.RutaScriptLabelProvider;
import edu.isistan.sadanalyzer.providers.SadSectionLabelProvider;
import edu.isistan.sadanalyzer.query.UIMASADQueryAdapter;
import edu.isistan.uima.unified.ruta.CrosscuttingConcernAdapted;
import edu.isistan.uima.unified.ruta.RutaEngine;
import edu.isistan.uima.unified.ruta.RutaScript;
import edu.isistan.uima.unified.typesystems.sad.SadSection;


public class SadAnalyzerSetUpPage extends FormPage {

	public static final String ID = "edu.isistan.sadanalyzer.pages.SadAnalyzerSetUpPage";
	public static final String TITLE = Messages.SadAnalyzerEditor_Configuration;
	
	private ListViewer listViewerSections;
	private ListViewer listViewerSectionsSelected;
	private ListViewer listQualityAttributesSource;
	private ListViewer listQualityAttributesSelected;
	private List listQualityAtributes1;
	private List listQualityAtributes2;
	private List listSections;
	private List listSectionsSelected;
	
	

	
	protected CrosscuttingConcernRuleSet rulesModelRoot;
	

	
	private UIMASADQueryAdapter uimaRoot;
	
	private RutaEngine rutaEngine;
	
	private SadAnalyzerProject modelRoot;
	private Sad sadModel;

	
	public SadAnalyzerSetUpPage(){
		super(ID, TITLE);
	}
	
	private void createRutaModel() {
		
		rutaEngine = new RutaEngine(sadModel.getLocale());
		
	}
	public SadAnalyzerSetUpPage(FormEditor editor) {
		super(editor, ID, TITLE);

		uimaRoot = ((SadAnalyzerEditor)getEditor()).getUimaRoot();
		modelRoot = ((SadAnalyzerEditor)getEditor()).getSadProjectModel();
		sadModel=((SadAnalyzerEditor)getEditor()).getSadModel();
	}
	
	/**
	 * @see org.eclipse.ui.forms.editor.FormPage#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);		
	}
	

	/**
	 * Create contents of the form.
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
			
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.SadAnalyzerEditor_ConfigurationTitle);
		
		Action executionAnalyzer = new Action("run", Action.AS_CHECK_BOX){
			public void run() {
				executeUimaRutaSadProcesor();
			}
		};
		executionAnalyzer.setToolTipText("Run"); 
		executionAnalyzer.setEnabled(Boolean.TRUE);
		executionAnalyzer.setImageDescriptor(ResourceManager.getPluginImageDescriptor("edu.isistan.sadanalyzer", "icons/run.gif"));
		
		form.getToolBarManager().add(executionAnalyzer);
		form.getToolBarManager().update(true);
		
		
		
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		
		ColumnLayout layout = new ColumnLayout();		
		layout.topMargin = 10;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 20;
		layout.maxNumColumns = 2;
		layout.minNumColumns = 2;
		
	
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(layout);
		
		createDetailAnalyzer(managedForm, Messages.SadAnalyzerEditor_ConfigurationDetail, Messages.SadAnalyzerEditor_ConfigurationDetailDescription);
		createTreeModel(managedForm, Messages.SadAnalyzerEditor_ConfigurationModelTree, Messages.SadAnalyzerEditor_ConfigurationModelTreeDescription);
		createRunUimasad(managedForm, Messages.SadAnalyzerEditor_ConfigurationRun, Messages.SadAnalyzerEditor_ConfigurationRunDescription);
		createQualityAttributes(managedForm, Messages.SadAnalyzerEditor_ConfigurationConcern, Messages.SadAnalyzerEditor_ConfigurationConcernDescription);
	}
	
	private void createRunUimasad(IManagedForm mform, String title, String desc) {
		Composite client = createSection(mform, title, desc, 2);
		FormToolkit toolkit = mform.getToolkit();
					
		// Run
		Button btnAdd=  toolkit.createButton(client, Messages.SadAnalyzerEditor_ConfigurationRunButton,SWT.BUTTON1);
		btnAdd.setImage(ResourceManager.getPluginImage("edu.isistan.sadanalyzer", "icons/run.gif"));
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeUimaRutaSadProcesor();
			}
		});
	}	
	
	
	private void createQualityAttributes(IManagedForm managedForm, String title, String desc) {
		Composite client = createSection(managedForm, title, desc, 3);
		FormToolkit toolkit = managedForm.getToolkit();
		
		listQualityAttributesSource = new ListViewer(client, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		listQualityAttributesSource.setLabelProvider(new RutaScriptLabelProvider());
	
		createRutaModel();
		
		Iterator<RutaScript> scripts = rutaEngine.getScripts().iterator();
		
		for ( ;scripts.hasNext();) {
			RutaScript script = scripts.next();
			
			listQualityAttributesSource.add(script);
		}	
		
				
		GridData gd = new GridData();
		gd.widthHint = 200;
		gd.heightHint = 300;
		listQualityAtributes1 = listQualityAttributesSource.getList();
		listQualityAtributes1.setLayoutData(gd);
		
		
		Composite compositeBtn = toolkit.createComposite(client, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		toolkit.paintBordersFor(compositeBtn);
		FillLayout fl_compositeBtn = new FillLayout(SWT.VERTICAL);
		fl_compositeBtn.marginWidth = 5;
		fl_compositeBtn.marginHeight = 5;
		fl_compositeBtn.spacing = 5;
		compositeBtn.setLayout(fl_compositeBtn);
		
		Button btnAddAll = toolkit.createButton(compositeBtn, ">> Add All", SWT.NONE);
		
		Button btnAdd = toolkit.createButton(compositeBtn, "Add", SWT.NONE);
		btnAdd.setImage(ResourceManager.getPluginImage("edu.isistan.sadanalyzer", "icons/add.gif"));
		btnAdd.setToolTipText("Add Quality attribute");			
				
		Button btnRemove = toolkit.createButton(compositeBtn, "Remove", SWT.NONE);
		btnRemove.setImage(ResourceManager.getPluginImage("edu.isistan.sadanalyzer", "icons/delete.gif"));
		btnRemove.setToolTipText("Remove Quality attribute");
		
		
		listQualityAttributesSelected = new ListViewer(client, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		listQualityAtributes2 = listQualityAttributesSelected.getList();
		listQualityAtributes2.setLayoutData(gd);
		listQualityAttributesSelected.setLabelProvider(new RutaScriptLabelProvider());
		
		Button btnRemoveAll = toolkit.createButton(compositeBtn, "<< Remove All", SWT.NONE);
		
		btnAddAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {				
				for(int i = 0; listQualityAttributesSource.getList().getItems().length > 0; ){
					RutaScript crossC = (RutaScript)listQualityAttributesSource.getElementAt(i);
					crossC.setEnable(true);
					if(!crossC.getList().isEmpty()){
						for(RutaScript rs : crossC.getList()){
							rs.setEnable(true);
						}
					}
					listQualityAttributesSelected.add(crossC);
					listQualityAttributesSource.remove(crossC);
				}				
				refreshList();
				
			}
		});
		
		btnRemoveAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(int i = 0; listQualityAttributesSelected.getList().getItems().length > 0; ){
					RutaScript crossC = (RutaScript)listQualityAttributesSelected.getElementAt(i);
					crossC.setEnable(false);
					if(!crossC.getList().isEmpty()){
						for(RutaScript rs : crossC.getList()){
							rs.setEnable(false);
						}
					}
					listQualityAttributesSource.add(crossC);
					listQualityAttributesSelected.remove(crossC);
				}				
				refreshList();
			}
		});
		
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)listQualityAttributesSource.getSelection();
				if(!selection.isEmpty()) {
					RutaScript crossCutting = (RutaScript)selection.getFirstElement();
					crossCutting.setEnable(true);
					if(!crossCutting.getList().isEmpty()){
						for(RutaScript rs : crossCutting.getList()){
							rs.setEnable(true);
						}
					}
					listQualityAttributesSource.remove(crossCutting);
					listQualityAttributesSelected.add(crossCutting);
					refreshList();
				}
			}
		});	
		
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)listQualityAttributesSelected.getSelection();
				if(!selection.isEmpty()) {
					RutaScript crossCutting = (RutaScript)selection.getFirstElement();
					crossCutting.setEnable(false);
					listQualityAttributesSelected.remove(crossCutting);
					listQualityAttributesSource.add(crossCutting);
					refreshList();
				}
			}
		});	
		
	}	
	
	private void createDetailAnalyzer(IManagedForm mform, String title, String desc) {
		Composite client = createSection(mform, title, desc, 2);
		FormToolkit toolkit = mform.getToolkit();

		IFile file =(IFile)getEditorInput().getAdapter(IFile.class);
		
		GridData gd = new GridData();		
		// Text source
		toolkit.createLabel(client, Messages.SadAnalyzerEditor_SadSource+":");
		String sadFileSource = modelRoot.getSadURI();
		Text sadFileSourceText = toolkit.createText(client, sadFileSource, SWT.SINGLE);
		sadFileSourceText.setEditable(false);
		gd = new GridData();
		gd.widthHint = 350;
		sadFileSourceText.setLayoutData(gd);
				
		toolkit.createLabel(client, Messages.SadAnalyzerEditor_UIMASadFile+":");
		String uimaSadFile = modelRoot.getUimaURI();
		
		Text uimaSadFileText = toolkit.createText(client, uimaSadFile, SWT.SINGLE);
		uimaSadFileText.setEditable(false);
		gd = new GridData();
		gd.widthHint = 350;
		uimaSadFileText.setLayoutData(gd);		
	}
	
	private void createTreeModel(IManagedForm managedForm, String title, String desc) {
		Composite client = createSection(managedForm, title, desc, 3);
		FormToolkit toolkit = managedForm.getToolkit();
		
		listViewerSections = new ListViewer(client, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);		
		listViewerSections.setLabelProvider(new SadSectionLabelProvider(uimaRoot));
		
		//TODO Ver esto
		EList<SadSection> sections = uimaRoot.getSadSection();
		
		for(Iterator<SadSection> it = sections.iterator();it.hasNext();){
			SadSection section = it.next();
			if(null != section.getName()){
				listViewerSections.add(section);
			}
		}
	
				
		GridData gd = new GridData();
		gd.widthHint = 200;
		gd.heightHint = 300;
		listSections = listViewerSections.getList();
		listSections.setLayoutData(gd);	
		
		Composite compositeBtn = toolkit.createComposite(client, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		toolkit.paintBordersFor(compositeBtn);
		FillLayout fl_compositeBtn = new FillLayout(SWT.VERTICAL);
		fl_compositeBtn.marginWidth = 5;
		fl_compositeBtn.marginHeight = 5;
		fl_compositeBtn.spacing = 5;
		compositeBtn.setLayout(fl_compositeBtn);
		
		Button btnAddAll = toolkit.createButton(compositeBtn, ">> Add All", SWT.NONE);
		
		Button btnAdd = toolkit.createButton(compositeBtn, "Add", SWT.NONE);
		btnAdd.setImage(ResourceManager.getPluginImage("edu.isistan.sadanalyzer", "icons/add.gif"));
		btnAdd.setToolTipText("Add Quality attribute");
			
		
		Button btnRemove = toolkit.createButton(compositeBtn, "Remove", SWT.NONE);
		btnRemove.setImage(ResourceManager.getPluginImage("edu.isistan.sadanalyzer", "icons/delete.gif"));
		btnRemove.setToolTipText("Remove Quality attribute");
		
		listViewerSectionsSelected = new ListViewer(client, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		listSectionsSelected = listViewerSectionsSelected.getList();
		listSectionsSelected.setLayoutData(gd);
		listViewerSectionsSelected.setLabelProvider(new SadSectionLabelProvider(uimaRoot));
		Button btnRemoveAll = toolkit.createButton(compositeBtn, "<< Remove All", SWT.NONE);
		
		btnAddAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				for(int i = 0; listViewerSections.getList().getItems().length > 0; ){
					SadSection sad = (SadSection)listViewerSections.getElementAt(i);
					listViewerSectionsSelected.add(sad);
					listViewerSections.remove(sad);
				}				
				refreshList();
			}
		});
		
		btnRemoveAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(int i = 0; listViewerSectionsSelected.getList().getItems().length > 0; ){
					SadSection sad = (SadSection)listViewerSectionsSelected.getElementAt(i);
					listViewerSections.add(sad);
					listViewerSectionsSelected.remove(sad);
				}				
				refreshList();
			}
		});
		
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)listViewerSections.getSelection();
				if(!selection.isEmpty()) {
					SadSection sadSection = (SadSection)selection.getFirstElement();
					listViewerSections.remove(sadSection);
					listViewerSectionsSelected.add(sadSection);
					refreshList();
				}
			}
		});	
		
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)listViewerSectionsSelected.getSelection();
				if(!selection.isEmpty()) {
					SadSection sadSection = (SadSection)selection.getFirstElement();
					listViewerSectionsSelected.remove(sadSection);
					listViewerSections.add(sadSection);
					refreshList();
				}
			}
		});	
	}	
	
	private void refreshList(){
		listSections.update();
		listSectionsSelected.update();
		listQualityAtributes1.update();
		listQualityAtributes2.update();
	}
	
	private Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		
		Composite compositeDetails = new Composite(mform.getForm().getBody(), SWT.NONE);
		compositeDetails.setLayout(new FillLayout(SWT.HORIZONTAL));
		toolkit.adapt(compositeDetails);
		toolkit.paintBordersFor(compositeDetails);		
		
		Section section = toolkit.createSection(compositeDetails, Section.TITLE_BAR 
				| Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		//toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}		

	
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
	}
	

	private void executeUimaRutaSadProcesor(){
	
       
		if((listViewerSectionsSelected.getList().getItems().length > 0) && (listQualityAttributesSelected.getList().getItems().length > 0)){
		
						
				URI platformURI = URI.createFileURI(modelRoot.getUimaURI());
				
				String inputFile =platformURI.toString();
				 		
				IFile file =(IFile)getEditorInput().getAdapter(IFile.class);
				String outputFile = file.getLocationURI().toString();
				outputFile= (String) outputFile.subSequence(0, outputFile.length()-3);
				
				outputFile+="ruta";
				
			   rutaEngine.executePipelineSpecific(inputFile);
			   Vector<CrosscuttingConcernAdapted> crosscuttingConcerns =  rutaEngine.getConcern();
		
			   if(crosscuttingConcerns != null){			
										
				java.util.List<CrosscuttingConcernAdapted> attributes = new ArrayList<CrosscuttingConcernAdapted>();
				for(int i=0; i < crosscuttingConcerns.size(); i++){
					CrosscuttingConcernAdapted c = (CrosscuttingConcernAdapted) crosscuttingConcerns.get(i);
					boolean match = false;
					for(int j=0; j < listQualityAttributesSelected.getList().getItems().length && !match; j++){
						if(listQualityAttributesSelected.getList().getItem(j).equals(c.getName())){
							attributes.add(c);
							match = true;
						}
					}
				}
				
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				for(IEditorReference reference : page.getEditorReferences()){
				  if(reference .getId().equals(SadAnalyzerEditor.ID)){
				    EditorPart part = (EditorPart) reference.getEditor(true);
				    SadAnalyzerEditor editor = (SadAnalyzerEditor)part;
				    SadAnalyzerViewerPage pageView = (SadAnalyzerViewerPage) editor.findPage(SadAnalyzerViewerPage.ID);
				    if(null == pageView){
				    	FormPage sdAnalyzerViewerPage = new SadAnalyzerViewerPage(editor, listViewerSectionsSelected,  attributes, rutaEngine);			    	
						try {
							editor.addPage(sdAnalyzerViewerPage);						
						} catch (PartInitException p) {
							
							p.printStackTrace();
						}
				    }else{
				    	pageView.refresh(listViewerSectionsSelected, attributes);
				    }
				    editor.setActivePage(SadAnalyzerViewerPage.ID);
				  }
				}
			}		
	  }
	}
	

}
