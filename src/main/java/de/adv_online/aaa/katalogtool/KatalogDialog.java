/**
 * AAA-Katalogtool
 *
 * (c) 2009-2012 Arbeitsgemeinschaft der Vermessungsverwaltungen der 
 * Länder der Bundesrepublik Deutschland (AdV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * interactive instruments GmbH
 * Trierer Strasse 70-72
 * 53115 Bonn
 * Germany
 */

package de.adv_online.aaa.katalogtool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.SystemUtils;

import de.interactive_instruments.ShapeChange.Converter;
import de.interactive_instruments.ShapeChange.DefaultModelProvider;
import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.TargetConfiguration;
import de.interactive_instruments.ShapeChange.Model.Model;
import de.interactive_instruments.ShapeChange.UI.Dialog;
import de.interactive_instruments.ShapeChange.UI.StatusBoard;
import de.interactive_instruments.ShapeChange.UI.StatusReader;
import shadow.org.apache.commons.lang3.StringUtils;

public class KatalogDialog extends JFrame implements ActionListener, ItemListener, Dialog, StatusReader {
	/**
	 * 
	 */
	public static final String VERSION_TEXT = "1.3.0";
	private static final String TITLE = "AAA-Katalogtool";
	
	private static final long serialVersionUID = -2443287559064380497L;
	
	public static final int STATUS_READY = 1;
	public static final int STATUS_FINISHED = 2;
	
	private File logfile = null;

	class StatusBar extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		JLabel label; 
		
		public StatusBar() {
		    setLayout(new BorderLayout());
		    setPreferredSize(new Dimension(10, 23));
		    this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

			label = new JLabel();
			add(label, BorderLayout.WEST);

		}
		public void setText(String text){
			label.setText(text);
		}
		public void delText(){
			label.setText("");
		}
	}

	class ConvertThread extends Thread {
	 	protected Converter converter = null;
		protected ShapeChangeResult result = null;
		protected Options options = null;
		protected String modelAbsolutePath = null;
		protected KatalogDialog dialog = null;
		protected boolean onlyInitialise; 

		ConvertThread(Converter c, Options o, ShapeChangeResult r, String e, KatalogDialog d) {
            this.converter = c;
            this.options = o;
            this.result = r;
            this.modelAbsolutePath = e;
            this.dialog = d;
            onlyInitialise = false;
        }

		public void setOnlyInitialise(boolean o){
			onlyInitialise = o;
		}
		
		public void initialise() throws ShapeChangeAbortException{
            if(dialog.model!=null){
            	dialog.model.shutdown();
            	dialog.model = null;
            }
				dialog.saveModelMsgText = true;
				String imt = options.parameter("inputModelType");
				DefaultModelProvider mp = new DefaultModelProvider(result, options);
				dialog.model = mp.getModel(imt, modelAbsolutePath, null, null, true, null);
				//            	dialog.model = new EADocument(result, options, modelAbsolutePath);
				dialog.saveModelMsgText = false;
                
            if(onlyInitialise)
            	dialog.threadInitialised();
		}
        public synchronized void restart(){
			result.init();
        	notify();
        }
		
		public void run() {
			String errmsg = null;
        	try{
	            while (true) {
	                try {
		                initialise();
		        		if(dialog.model!=null && onlyInitialise==false){
		        			converter.convert(dialog.model);
		        		}
		        		dialog.threadFinished(null);
		        		synchronized(this) {
		        			wait();
		        			sleep(100);
		        		}
	                } catch (InterruptedException e){
	                	break;
	                }
	            } 
			 } catch (ShapeChangeAbortException ex) {
				 errmsg = ex.getMessage();
				 if(errmsg==null)
					 errmsg = ex.toString();
				 if(errmsg==null)
					 errmsg = "Unknown ShapeChangeAbortException.";
			 } catch (Exception ex) {
				 ex.printStackTrace();
			 } finally{
				 dialog.threadFinished(errmsg);
			 }
        }
		
		public void end(){
			restart();
			interrupt();
		}
	}
        
	class TargetGuiElements{
		private JTextField dirTextField;
		private JButton dirSelButton;
		private JLabel dirLabel;
		private JCheckBox selBox;
		
		private KatalogDialog dialog;
		
		TargetGuiElements(KatalogDialog dlg, String label){
			selBox = null;
			dialog = dlg;
			
			mkElems(label);
		}
		
		private void mkElems(String label){
	        selBox = new JCheckBox(label);
	        selBox.addItemListener(dialog);
	        String opt = options.parameter(dialog.paramKatalogClass,"ausgabeformat");
	        if(opt!=null && opt.toLowerCase().contains(label.toLowerCase()))
	        	selBox.setSelected(true);
		}
	}
	
    private final static ArrayList<String> targetLabels = new ArrayList<String>();
    static {
    	// Reihenfolge der Einträge sollte stabil bleiben - wird verwendet in statusChanged()!
    	targetLabels.add("XML");
    	targetLabels.add("HTML");
    	targetLabels.add("DOCX");
    	targetLabels.add("CSV");
    }
    private StatusBar statusBar;
    
	// Output directory
	private HashMap<String,TargetGuiElements>  targetGuiElems = new HashMap<String,TargetGuiElements>();
	
	JLabel appSchemaFieldLabel;
	JLabel schemaKennFieldLabel;
	JLabel modellartFieldLabel;
	JLabel xsltpfadFieldLabel;
	JLabel outDirFieldLabel;
	JLabel mdlDirFieldLabel;

	JTextField appSchemaField = null; 
	
	JTextField schemaKennField = null; 

	// geerbte Eigenschaften
	private JCheckBox geerbEigBox;

	JTextField modellartField = null; 
	
	private JCheckBox retiredBox;
	private JCheckBox grundDatBox;
	
	private JCheckBox nutzungsartkennungBox;
	
	private JCheckBox profEinschrBox;
	private JCheckBox profDateiBox;
	
	JTextField profileField = null; 

	JTextField xsltpfadField = null; 
	JTextField outDirField = null; 
	JTextField mdlDirField = null; 

	private JCheckBox pkgBox;

	JTextField pkgField = null; 
	
	//commandos
	private JButton startButton;
    private JButton viewLogButton;
	private JButton exitButton;
	
	// Thread
	private ConvertThread ct = null; 
		
	// transformation
	private boolean modelTransformed;
	private boolean transformationRunning;
	
 	protected Converter converter = null;
	protected ShapeChangeResult result = null;
	protected Options options = null;
	protected String modelAbsolutePath = null;
	
	private Model model = null;
	private String modelMsgText = "";
	private boolean saveModelMsgText = false;
	private JComponent newContentPane;
	
	private boolean blocked = false;
	
	private String paramKatalogClass = "de.adv_online.aaa.katalogtool.Katalog";
	
	public KatalogDialog(){
		super(TITLE);
     	converter = null;
    	result = null;
    	options = null;
    	modelAbsolutePath = null;
    	model = null;
    	blocked = false;
	}
	
	public KatalogDialog(Converter c, Options o, ShapeChangeResult r, String xmi) throws ShapeChangeAbortException{
		super(TITLE);
     	converter = null;
    	result = null;
    	options = null;
    	modelAbsolutePath = null;
    	model = null;
    	blocked = false;
		initialise(c, o, r, xmi);
	}
	
	public void initialise(Converter c, Options o, ShapeChangeResult r, String mdl) throws ShapeChangeAbortException{
		
    	try{
	    	String msg = "Akzeptieren Sie die in der mit diesem Tool ausgelieferten Datei 'Lizenzbedingungen zur Nutzung von Softwareskripten.doc' beschriebenen Lizenzbedingungen?"; // Meldung
	    	if(msg!=null){
	    		Object[] options = {"Ja", "Nein"};
	    		int val = JOptionPane.showOptionDialog(null, msg, "Confirmation", JOptionPane.OK_CANCEL_OPTION,
	    			    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
	    		if(val == 1)
	    			System.exit(0);
	    	}
    	}
    	catch(Exception e){
    		System.out.println("Fehler in Dialog: " + e.toString());
    	}
		
		options = o;

		File modelFile = new File(mdl);
        try{
        	modelAbsolutePath = modelFile.getCanonicalFile().getAbsolutePath();
        } catch(IOException e){
        	modelAbsolutePath = "ERROR.model";
        }
		
		converter = new Converter(options, r);
		result = r;
		modelTransformed = false;
		transformationRunning = false;

		StatusBoard.getStatusBoard().registerStatusReader(this);
		
		// frame
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// panel
		newContentPane = new JPanel(new BorderLayout());
        newContentPane.setOpaque(true); 
        setContentPane(newContentPane);

        // target elements
    	for(String label : targetLabels){
            try{
        		TargetGuiElements t = new TargetGuiElements(this, label);
        		targetGuiElems.put(label, t);
            }
            catch(Exception e){
            	throw new ShapeChangeAbortException("Fatal error while creating dialog elements for target " + label + ".\nMessage: " + modelAbsolutePath.toString() + "\nPlease check configuration file.");
            }
    	}

        //JTabbedPane tabbedPane = new JTabbedPane();
		//tabbedPane.addTab("Main options", createMainTab());

        newContentPane.add(createMainTab(), BorderLayout.CENTER);

        statusBar = new StatusBar();

        Box fileBox = Box.createVerticalBox();
        fileBox.add(createStartPanel());
        fileBox.add(statusBar);

        newContentPane.add(fileBox, BorderLayout.SOUTH);

        
		// frame size
        int height = 800;
        int width = 600;

        pack();

        Insets fI = getInsets();
        setSize(width + fI.right + fI.left, height + fI.top + fI.bottom);
        Dimension sD = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((sD.width - width)/2, (sD.height - height)/2);
        this.setMinimumSize(new Dimension(width, height));

        

        // frame closing
        WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent w) {
            	//JOptionPane.showMessageDialog(null, "Nein", "NO", JOptionPane.ERROR_MESSAGE);
            	closeDialog();
            }
          };
          addWindowListener(listener);
	}
	
	public void blockTransformation(boolean b){
		blocked = b;
		if(b==true)
			transformationRunning = true;
		else
			modelTransformed = true;
		
		enableElements();
	}

	public void setVisible(boolean vis){
		enableElements();
		super.setVisible(vis);
	}
	
    private Component createMainTab() {

    	// Übernahme der Eigenschaften
    	String s = "";
    	
    	String appSchemaStr;
		s = options.parameter("appSchemaName");
		if (s!=null && s.trim().length()>0)
			appSchemaStr = s.trim();
		else
			appSchemaStr = "";

    	
        String schemaKennungenStr;
		s = options.parameter(paramKatalogClass,"schemakennungen");
		if (s!=null && s.trim().length()>0)
			schemaKennungenStr = s.trim();
		else
			schemaKennungenStr = "*";
        
        Boolean geerbEigBool = false;
		s = options.parameter(paramKatalogClass,"geerbteEigenschaften");
		if (s!=null && s.equals("true"))
			geerbEigBool = true;

        Boolean retiredBool = false;
		s = options.parameter(paramKatalogClass,"stillgelegteElemente");
		if (s!=null && s.equals("true"))
			retiredBool = true;

	Boolean nutzungsartkennungBool = true;
		s = options.parameter("ignoreTaggedValues");
		if (s!=null && s.contains("AAA:Nutzungsartkennung"))
		    nutzungsartkennungBool = false;

        String modellartenStr;
		s = options.parameter(paramKatalogClass,"modellarten");
		if (s==null || s.trim().length()==0)
			modellartenStr = "";
		else
			modellartenStr = s.trim();
        
        Boolean grundDatBool = false;
		s = options.parameter(paramKatalogClass,"nurGrunddatenbestand");
		if (s!=null && s.equals("true"))
			grundDatBool = true;
        
		Boolean profEinschrBool = false;
		Boolean profDateiBool = false;
        String profileStr;
		s = options.parameter(paramKatalogClass,"profile");
		if (s==null || s.trim().length()==0)
			profileStr = "";
		else
			profileStr = s.trim();
		if (profileStr.length()>0)
			profEinschrBool = true;
		s = options.parameter(paramKatalogClass,"profilquelle");
		if (s!=null && s.trim().equals("Datei"))
			profDateiBool = true;
		        
		Boolean pkgBool = false;
        String pkgStr;
		s = options.parameter(paramKatalogClass,"paket");
		if (s==null || s.trim().length()==0)
			pkgStr = "";
		else
			pkgStr = s.trim();
		if (pkgStr.length()>0)
			pkgBool = true;
		        
        String xsltPfadStr;
		s = options.parameter(paramKatalogClass,"xsltPfad");
		if (s==null || s.trim().length()==0)
			xsltPfadStr = "";
		else{
			if (s.toLowerCase().startsWith("http://")) {
				xsltPfadStr = s;
			} else {
				File f = new File(s.trim());
				if(f.exists())
					xsltPfadStr = f.getAbsolutePath();
				else
					xsltPfadStr = "";
			}
		}

        String outDirStr;
		s = options.parameter(paramKatalogClass,"Verzeichnis");
		if (s==null || s.trim().length()==0)
			outDirStr = "";
		else{
			File f = new File(s.trim());
			if(f.exists())
				outDirStr = f.getAbsolutePath();
			else
				outDirStr = "";
		}

        String mdlDirStr = modelAbsolutePath;

        // Anwendungsschema

		final JPanel appSchemaPanel = new JPanel();
        final JPanel appSchemaInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 5));
        appSchemaPanel.setLayout(new BoxLayout(appSchemaPanel, BoxLayout.X_AXIS));
        appSchemaPanel.setBorder(BorderFactory.createEmptyBorder(15,20,15,10));

        appSchemaField = new JTextField(37);
        appSchemaField.setText(appSchemaStr);
        appSchemaFieldLabel = new JLabel("Name des zu exportierenden Anwendungsschemas:");

        Box asBox = Box.createVerticalBox();
        asBox.add(appSchemaFieldLabel);
        asBox.add(appSchemaField);
        
        pkgBox = new JCheckBox("Eingeschränkt auf Paket:");
        pkgBox.setSelected(pkgBool);
        pkgBox.addItemListener(this);
        pkgField = new JTextField(37);
        pkgField.setText(pkgStr);
        if(pkgStr.length()==0){
        	pkgField.setEnabled(false);
        	pkgField.setEditable(false);
        }
        asBox.add(pkgBox);
        asBox.add(pkgField);

        appSchemaInnerPanel.add(asBox);
        
        appSchemaPanel.add(appSchemaInnerPanel);

    	// Ausgabeoptionen
       
        Box outOptBox = Box.createVerticalBox();

        final JPanel outOptInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        Box skBox = Box.createVerticalBox();
        schemaKennFieldLabel = new JLabel("Liste der zu berücksichtigenden Schema-Kennungen");
        skBox.add(schemaKennFieldLabel);
        schemaKennField = new JTextField(35);
        schemaKennField.setText(schemaKennungenStr);
        schemaKennField.setToolTipText("Nur Klassen mit diesen Kennungen werden exportiert. '*' dient als Wildcard.");

        skBox.add(schemaKennField);
        outOptInnerPanel.add(skBox);
        outOptBox.add(outOptInnerPanel);

        final JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        for(String label : targetLabels){
            targetPanel.add(targetGuiElems.get(label).selBox);
        }
        outOptBox.add(targetPanel);

        final JPanel geerbEigPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        geerbEigBox = new JCheckBox("Eigenschaften aus Superklassen auch in abgeleiteten Klassen darstellen");
        geerbEigBox.setSelected(geerbEigBool);
        geerbEigBox.addItemListener(this);
        geerbEigPanel.add(geerbEigBox);
        outOptBox.add(geerbEigPanel);

        final JPanel retiredPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        retiredBox = new JCheckBox("Auch stillgelegte Modellelemente (Stereotype <<retired>>) ausgeben");
        retiredBox.setSelected(retiredBool);
        retiredBox.addItemListener(this);
        retiredPanel.add(retiredBox);
        outOptBox.add(retiredPanel);

        final JPanel nutzungsartkennungPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        nutzungsartkennungBox = new JCheckBox("Nutzungsartkennung ausgeben");
        nutzungsartkennungBox.setSelected(nutzungsartkennungBool);
        nutzungsartkennungBox.addItemListener(this);
        nutzungsartkennungPanel.add(nutzungsartkennungBox);
        outOptBox.add(nutzungsartkennungPanel);

        final JPanel outOptPanel = new JPanel();
        outOptPanel.add(outOptBox);
        outOptPanel.setBorder(new TitledBorder(new LineBorder(Color.black), 
                "Ausgabeoptionen", TitledBorder.LEFT, TitledBorder.TOP));

    	// Modellarten und Profile
        Box modProfBox = Box.createVerticalBox();

        final JPanel modProfInnerPanel1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 5));
        
        skBox = Box.createVerticalBox();
        modellartFieldLabel = new JLabel("Ausgewählte Modellarten:");
        modellartField = new JTextField(45);
        modellartField.setText(modellartenStr);
        modellartField.setToolTipText("Mindestens eine Modellart. Auf richtige Schreibweise achten. Mehrere Modellarten jeweils durch ein Komma ohne Leerzeichen trennen.");
        skBox.add(modellartFieldLabel);
        skBox.add(modellartField);
        modProfInnerPanel1.add(skBox);
        modProfBox.add(modProfInnerPanel1);
        final JPanel grundDatPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 8));
        grundDatBox = new JCheckBox("Nur den Grunddatenbestand exportieren");
        grundDatBox.setSelected(grundDatBool);
        grundDatBox.addItemListener(this);
        grundDatPanel.add(grundDatBox);
        modProfBox.add(grundDatPanel);

        final JPanel profPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        profEinschrBox = new JCheckBox("Eingeschränkt auf folgende Profilkennung(en) im Modell:");
        profEinschrBox.setSelected(profEinschrBool);
        profEinschrBox.addItemListener(this);
		profEinschrBox.setToolTipText("Die Profilkennung ist der Name der 3ap-Datei ohne Dateikennung.");
        profPanel.add(profEinschrBox);
        final JPanel profPanel2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 2));
        profileField = new JTextField(45);
        profileField.setText(profileStr);
		profileField.setToolTipText("Die Profilkennung ist der Name der 3ap-Datei ohne Dateikennung.");
		profPanel2.add(profileField);
        final JPanel profPanel3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
        profDateiBox = new JCheckBox("Profil(e) nur aus 3ap-Datei laden und verwenden");
        profDateiBox.setSelected(profDateiBool);
        profDateiBox.addItemListener(this);
        profDateiBox.setToolTipText("Profilangaben im Modell werden ignoriert.");
        profPanel3.add(profDateiBox);
        if(profileStr.length()==0){
        	profileField.setEnabled(false);
        	profileField.setEditable(false);
        	profDateiBox.setEnabled(false);
        }
        
        modProfBox.add(profPanel);
        modProfBox.add(profPanel2);
        modProfBox.add(profPanel3);

        final JPanel modProfPanel = new JPanel();
        modProfPanel.add(modProfBox);
        modProfPanel.setBorder(new TitledBorder(new LineBorder(Color.black), 
                "Auswahl der Modellarten und Profile", TitledBorder.LEFT, TitledBorder.TOP));

        // Pfadangaben
        Box pfadBox = Box.createVerticalBox();
        final JPanel pfadInnerPanel = new JPanel();
        skBox = Box.createVerticalBox();
        xsltpfadFieldLabel = new JLabel("Pfad in dem die XSLT-Skripte liegen:");
        skBox.add(xsltpfadFieldLabel);
        xsltpfadField = new JTextField(45);
        xsltpfadField.setText(xsltPfadStr);
        skBox.add(xsltpfadField);
        outDirFieldLabel = new JLabel("Pfad in den die Kataloge geschrieben werden:");
        skBox.add(outDirFieldLabel);
        outDirField = new JTextField(45);
        outDirField.setText(outDirStr);
        skBox.add(outDirField);
        mdlDirFieldLabel = new JLabel("Pfad zum Modell:");
        skBox.add(mdlDirFieldLabel);
        mdlDirField = new JTextField(45);
        mdlDirField.setText(mdlDirStr);
        skBox.add(mdlDirField);
        pfadInnerPanel.add(skBox);
        pfadBox.add(pfadInnerPanel);

        final JPanel pfadPanel = new JPanel();
        pfadPanel.add(pfadBox);
        pfadPanel.setBorder(new TitledBorder(new LineBorder(Color.black), 
                "Pfadangaben", TitledBorder.LEFT, TitledBorder.TOP));
        
        // Zusammenstellung
        Box fileBox = Box.createVerticalBox();
        fileBox.add(appSchemaPanel);
        fileBox.add(outOptPanel);
        fileBox.add(modProfPanel);
        fileBox.add(pfadPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(fileBox, BorderLayout.NORTH);
        
        return panel;
    };

    private Component createStartPanel() {

        final JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 
                20, 20));
        //startPanel.sets
        startButton = new JButton("Process Model");
        startButton.setActionCommand("START");
        startButton.addActionListener(this);
        startPanel.add(startButton);
        viewLogButton = new JButton("View Log");
        viewLogButton.setActionCommand("LOG");
        viewLogButton.addActionListener(this);
        viewLogButton.setEnabled(false);
        startPanel.add(viewLogButton);
        exitButton = new JButton("Exit");
        exitButton.setActionCommand("EXIT");
        exitButton.addActionListener(this);
        exitButton.setEnabled(true);
        startPanel.add(exitButton);
        
        startPanel.setPreferredSize(new Dimension(10,60));
        
        return startPanel;
    };

    protected void closeDialogWithError(String errmsg){
    	try{
    		errmsg = "The application has encountered a fatal error.\nError message:\n" + errmsg;
    		JOptionPane.showMessageDialog(this, errmsg, "Fatal error", JOptionPane.ERROR_MESSAGE);
    		result.addFatalError(errmsg);

	    	if(model!=null)
	    		model.shutdown();
	    	
	 	//	System.exit(0);
    	}
    	catch(Exception e){
    		System.out.println("closeDialog - Exception: " + e.toString());
	 		//System.exit(1);
    	}
 	}

    protected void closeDialog(){
    	try{
	    	String msg = null;
	    	if(transformationRunning)
	    		msg = "Eine Katalogerzeugung läuft derzeit.\n";// Meldung
	   		
	    	if(msg!=null){
	    		msg += "Soll die Anwendung beendet werden?";
	    		Object[] options = {"Exit", "Cancel"};
	    		int val = JOptionPane.showOptionDialog(null, msg, "Confirmation", JOptionPane.OK_CANCEL_OPTION,
	    			    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
	    		if(val == 1)
	    			return;
	    	}
	    	
	    	if(model!=null)
	    		model.shutdown();
	    	
	 		System.exit(0);
    	}
    	catch(Exception e){
    		System.out.println("closeDialog - Exception: " + e.toString());
	 		//System.exit(1);
    	}
 	}

    public void threadInitialised(){
    }
    
    public void threadFinished(String errmsg){
    	if(errmsg!=null){
    		closeDialogWithError(errmsg);
    	}
    	
    	if(transformationRunning){
        	modelTransformed = true;
    		transformationRunning = false;
    		
    		StatusBoard.getStatusBoard().statusChanged(STATUS_FINISHED);
        	
        	enableElements();
    	}
    }
    
    private synchronized void startTransformation(){
	// finish options
	
	String ignoreTVsParamValue = StringUtils.stripToEmpty(
		options.getInputConfig().getParameters().get("ignoreTaggedValues"));
	SortedSet<String> tagsToIgnore = new TreeSet<>();
	tagsToIgnore.addAll(Arrays.asList(StringUtils.split(ignoreTVsParamValue, ", ")));
	
	if(nutzungsartkennungBox.isSelected()) {	
	    if(tagsToIgnore.contains("AAA:Nutzungsartkennung")) {
		tagsToIgnore.remove("AAA:Nutzungsartkennung");		
	    }
	} else {
	    tagsToIgnore.add("AAA:Nutzungsartkennung");
	}
	options.getInputConfig().setParameter("ignoreTaggedValues", 
		tagsToIgnore.isEmpty() ? "" : StringUtils.join(tagsToIgnore, ", "));
		
	// RESET FIELDS IN ORDER FOR THE INPUT CONFIG CHANGE TO TAKE (PERMANENT) EFFECT
	options.resetFields();
	
	    
        modelTransformed = false;
        transformationRunning = true;

		String opt = "";
		for(String label : targetLabels){
			if(targetGuiElems.get(label).selBox.isSelected()){
				opt += label + ",";
			}
		}
		options.setParameter(paramKatalogClass,"ausgabeformat", opt);
		
		options.setParameter("appSchemaName", appSchemaField.getText());
		options.setParameter(paramKatalogClass,"schemakennungen", schemaKennField.getText());
		if(geerbEigBox.isSelected())
			options.setParameter(paramKatalogClass,"geerbteEigenschaften","true");
		else
			options.setParameter(paramKatalogClass,"geerbteEigenschaften","false");
		if(retiredBox.isSelected())
			options.setParameter(paramKatalogClass,"stillgelegteElemente","true");
		else
			options.setParameter(paramKatalogClass,"stillgelegteElemente","false");
		options.setParameter(paramKatalogClass,"modellarten", modellartField.getText());
		if(grundDatBox.isSelected())
			options.setParameter(paramKatalogClass,"nurGrunddatenbestand","true");
		else
			options.setParameter(paramKatalogClass,"nurGrunddatenbestand","false");
		if(profEinschrBox.isSelected()) {
			options.setParameter(paramKatalogClass,"profile", profileField.getText());
			if(profDateiBox.isSelected())
				options.setParameter(paramKatalogClass,"profilquelle", "Datei");
			else
				options.setParameter(paramKatalogClass,"profilquelle", "Modell");
		} else
			options.setParameter(paramKatalogClass,"profile", "");
		if(pkgBox.isSelected())
			options.setParameter(paramKatalogClass,"paket", pkgField.getText());
		else
			options.setParameter(paramKatalogClass,"paket", "");
		options.setParameter(paramKatalogClass,"xsltPfad", xsltpfadField.getText());
		options.setParameter(paramKatalogClass,"Verzeichnis", outDirField.getText());
		String mdl = mdlDirField.getText();
		options.setParameter("inputFile", mdl);
		
		if (mdl.toLowerCase().endsWith(".zip")||mdl.toLowerCase().endsWith(".xml"))
			options.setParameter("inputModelType", "SCXML");

		else if (mdl.toLowerCase().endsWith(".xmi"))
			options.setParameter("inputModelType", "XMI10");
		else if (mdl.toLowerCase().endsWith(".eap")||mdl.toLowerCase().endsWith(".eapx"))
			options.setParameter("inputModelType", "EA7");

		// update target config from dialog also in the target configurations (strictly this is the only place where they need to be updated)
		for (TargetConfiguration cfg : options.getInputTargetConfigs()) {
			if (cfg.getClassName().equalsIgnoreCase(paramKatalogClass)) {
				Map<String, String> m = cfg.getParameters();
				m.put("ausgabeformat", opt);
				m.put("schemakennungen", schemaKennField.getText());
				if(geerbEigBox.isSelected())
					m.put("geerbteEigenschaften","true");
				else
					m.put("geerbteEigenschaften","false");
				if(retiredBox.isSelected())
					m.put("stillgelegteElemente","true");
				else
					m.put("stillgelegteElemente","false");
				m.put("modellarten", modellartField.getText());
				if(grundDatBox.isSelected())
					m.put("nurGrunddatenbestand","true");
				else
					m.put("nurGrunddatenbestand","false");
				if(profEinschrBox.isSelected()) {
					m.put("profile", profileField.getText());
					if(profDateiBox.isSelected())
						m.put("profilquelle", "Datei");
					else
						m.put("profilquelle", "Modell");
				} else
					m.put("profile", "");
				if(pkgBox.isSelected())
					m.put("paket", pkgField.getText());
				else
					m.put("paket", "");
				m.put("xsltPfad", xsltpfadField.getText());
				m.put("Verzeichnis", outDirField.getText());
			}
		}

		
		// start transformation
		if(!reinitMessageText("model transformation process")){
			// cancel action
			transformationRunning = false;
		}
		enableElements();

		startConvertThread();
    }

    public void startConvertThread(){
    	startConvertThread(false);
    }
    
    public void startConvertThread(boolean onlyInit){
		if(ct == null){
			ct = new ConvertThread(converter, options, result, mdlDirField.getText(), this);
			ct.setOnlyInitialise(onlyInit);
			ct.start();
		}
		else{
			ct.setOnlyInitialise(onlyInit);
			ct.restart();
		}
    }
    
    public Model getModel(){
    	return model;
    }
    
	public void enableElements(){
    	if(transformationRunning){
			for(String label : targetLabels){
				targetGuiElems.get(label).selBox.setEnabled(false);
			}

			appSchemaField.setEnabled(false); 
			schemaKennField.setEnabled(false); 
			geerbEigBox.setEnabled(false);
			retiredBox.setEnabled(false);
			nutzungsartkennungBox.setEnabled(false);
			modellartField.setEnabled(false); 
			grundDatBox.setEnabled(false);
			profEinschrBox.setEnabled(false);
			profileField.setEnabled(false); 
			profDateiBox.setEnabled(false); 
			pkgBox.setEnabled(false);
			pkgField.setEnabled(false); 
			xsltpfadField.setEnabled(false); 
			outDirField.setEnabled(false);
			mdlDirField.setEnabled(false);
			appSchemaFieldLabel.setEnabled(false);
			schemaKennFieldLabel.setEnabled(false);
			modellartFieldLabel.setEnabled(false);
			xsltpfadFieldLabel.setEnabled(false);
			outDirFieldLabel.setEnabled(false);
			mdlDirFieldLabel.setEnabled(false);
			
			startButton.setEnabled(false);
			viewLogButton.setEnabled(false);
	        exitButton.setEnabled(true);

	        setConfigDirSettings(false);
	        
    	}
    	else{    		
			for(String label : targetLabels){
				targetGuiElems.get(label).selBox.setEnabled(true);
			}
			appSchemaField.setEnabled(true); 
			schemaKennField.setEnabled(true); 
			geerbEigBox.setEnabled(true);
			retiredBox.setEnabled(true);
			nutzungsartkennungBox.setEnabled(true);
			modellartField.setEnabled(true); 
			grundDatBox.setEnabled(true);
			profEinschrBox.setEnabled(true);
			profileField.setEnabled(true); 
			profDateiBox.setEnabled(true); 
			pkgBox.setEnabled(true);
			pkgField.setEnabled(true); 
			xsltpfadField.setEnabled(true); 
			outDirField.setEnabled(true);
			mdlDirField.setEnabled(true);
			appSchemaFieldLabel.setEnabled(true);
			schemaKennFieldLabel.setEnabled(true);
			modellartFieldLabel.setEnabled(true);
			xsltpfadFieldLabel.setEnabled(true);
			outDirFieldLabel.setEnabled(true);
			mdlDirFieldLabel.setEnabled(true);

			if(startButton!=null){
				boolean setStart = false;
				for(String label : targetLabels){
					if(targetGuiElems.get(label).selBox!=null && targetGuiElems.get(label).selBox.isSelected()){
						setStart = true;
					}
				}
				if(setStart && !blocked){
		        	startButton.setEnabled(true);
		    		StatusBoard.getStatusBoard().statusChanged(STATUS_READY);
		        }
		        else{	        	
		        	startButton.setEnabled(false);
		    		StatusBoard.getStatusBoard().statusChanged(0);
		        }
			}
			if(viewLogButton!=null && modelTransformed) {
				 logfile = new File(options.parameter("logFile").replace(".xml", ".html"));
				 if (logfile!=null && logfile.canRead())
					 viewLogButton.setEnabled(true);
				 else {
					 logfile = new File(options.parameter("logFile"));
					 if (logfile!=null && logfile.canRead())
						 viewLogButton.setEnabled(true);
				 }
				viewLogButton.setEnabled(true);
			}
			if(exitButton!=null)
				exitButton.setEnabled(true);
		
			setConfigDirSettings(true);

    	}
    }

    public boolean reinitMessageText(String action){
		return true;
    }
    
    public void showMessageText (String msg){
    	if(saveModelMsgText){
    		modelMsgText += msg;
    	}
    }
    
    public void actionPerformed(ActionEvent e) {
	   	if(startButton == e.getSource()) {
	   		startTransformation();
	   	} else if (e.getSource() == viewLogButton) {
	   		try {
	   			 if (Desktop.isDesktopSupported())
	    			 Desktop.getDesktop().open(logfile);
				 else if (SystemUtils.IS_OS_WINDOWS)
					 Runtime.getRuntime().exec("cmd /c start "+logfile.getPath());
				 else 
					 Runtime.getRuntime().exec("open "+logfile.getPath());
			} catch (Exception e1) {
				e1.printStackTrace();
				System.exit(1);
			}
	   	} else if (e.getSource() == exitButton) {
	   		closeDialog();
	   	}
    }

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == profEinschrBox){
			if(profEinschrBox!=null && profEinschrBox.isSelected()==false && profileField!=null){
				profileField.setEnabled(false);
				profileField.setEditable(false);
				profDateiBox.setEnabled(false);
			}
			else{
				profileField.setEnabled(true);
				profileField.setEditable(true);
				profDateiBox.setEnabled(true);
			}
		}
		if(e.getSource() == pkgBox){
			if(pkgBox!=null && pkgBox.isSelected()==false
					&& pkgField!=null){
				pkgField.setEnabled(false);
				pkgField.setEditable(false);
			}
			else{
				pkgField.setEnabled(true);
				pkgField.setEditable(true);
			}
		}
	}
	
	public void setConfigDirSettings(boolean allSelConfig){
		boolean selConfig;

		for(String label : targetLabels){
			TargetGuiElements t = targetGuiElems.get(label);
			
			if (t.dirLabel!=null && t.dirSelButton!=null && t.dirTextField!=null && t.selBox!=null){
				if(allSelConfig)
					selConfig = t.selBox.isSelected();
				else
					selConfig = false;
				t.dirLabel.setEnabled(selConfig);
				t.dirSelButton.setEnabled(selConfig);
				t.dirTextField.setEnabled(selConfig);
				t.dirTextField.setEditable(selConfig);
			}
		}
	}
	

	public void statusChanged(int status){
		String msg = " ";
		switch(status){
		// Dialog
		case STATUS_READY:
			msg += "Ready";
			break;
			// Model EADocument
		// TODO - Status messages in other model loaders?
		case Model.STATUS_EADOCUMENT_INITSTART:
			msg += "Lesen des Modells: Initialisierung";
			break;
		case Model.STATUS_EADOCUMENT_READMODEL:
			msg += "Lesen des Modells: Paketinformationen";
			break;
		case Model.STATUS_EADOCUMENT_ESTABLISHCLASSES:
			msg += "Lesen des Modells: Objektarten und deren Eigenschaften";
			break;
		case Model.STATUS_EADOCUMENT_READCONSTARINTS:
			msg += "Lesen des Modells: Konsistenzbedingungen";
			break;
		// Targets
		case Converter.STATUS_TARGET_INITSTART:
			msg += "AAA-Katalogtool: Initialisierung der Katalogerzeugung";
			break;			
		case Converter.STATUS_TARGET_PROCESS:
			msg += "AAA-Katalogtool: Erzeugung des Katalogs";
			break;
		case Converter.STATUS_TARGET_WRITE:
			msg += "AAA-Katalogtool: Schreiben des Katalogs";
			break;
		case Katalog.STATUS_WRITE_XML:
			msg += "AAA-Katalogtool: Schreiben des Katalogs";
			if(targetLabels!=null && targetLabels.size()>=1)
				msg += " - " + targetLabels.get(0);
			break;
		case Katalog.STATUS_WRITE_HTML:
			msg += "AAA-Katalogtool: Schreiben des Katalogs";
			if(targetLabels!=null && targetLabels.size()>=2)
				msg += " - " + targetLabels.get(1);
			break;
		case Katalog.STATUS_WRITE_DOCX:
			msg += "AAA-Katalogtool: Schreiben des Katalogs";
			if(targetLabels!=null && targetLabels.size()>=3)
				msg += " - " + targetLabels.get(2);
			break;		
		case Katalog.STATUS_WRITE_CSV:
			msg += "AAA-Katalogtool: Schreiben des Katalogs";
			if(targetLabels!=null && targetLabels.size()>=6)
				msg += " - " + targetLabels.get(5);
			break;
		// default: delete status bar text
		default:
			msg = null;
			statusBar.delText();
		}
		
		if(msg!=null)
			statusBar.setText(msg);
	}
}
