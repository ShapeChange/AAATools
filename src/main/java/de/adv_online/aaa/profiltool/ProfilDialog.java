/**
 * AAA-Profiltool
 *
 * The class in this file implements the ShapeChange Target interface to 
 * generate and load the 3AP files.
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

package de.adv_online.aaa.profiltool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import java.util.Enumeration;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.SystemUtils;

import de.interactive_instruments.ShapeChange.Converter;
import de.interactive_instruments.ShapeChange.Options;
import de.interactive_instruments.ShapeChange.ShapeChangeAbortException;
import de.interactive_instruments.ShapeChange.ShapeChangeResult;
import de.interactive_instruments.ShapeChange.TargetConfiguration;
import de.interactive_instruments.ShapeChange.Model.Model;
import de.interactive_instruments.ShapeChange.Model.EA.EADocument;
import de.interactive_instruments.ShapeChange.UI.Dialog;
import de.interactive_instruments.ShapeChange.UI.StatusBoard;
import de.interactive_instruments.ShapeChange.UI.StatusReader;

public class ProfilDialog extends JFrame implements ActionListener, ItemListener, Dialog, StatusReader {
	/**
	 * 
	 */
	public static final String VERSION_TEXT = "1.0.0";
	private static final String title = "AAA-Profiltool";
	
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
		protected String eaRepositoryPath = null;
		protected ProfilDialog dialog = null;
		protected boolean onlyInitialise; 

		ConvertThread(Converter c, Options o, ShapeChangeResult r, String e, ProfilDialog d) {
            this.converter = c;
            this.options = o;
            this.result = r;
            this.eaRepositoryPath = e;
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
            	dialog.model = new EADocument(result, options, eaRepositoryPath);
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
        
    private StatusBar statusBar;
    
	JLabel appSchemaFieldLabel;
	JLabel modellartFieldLabel;
	JLabel profilFieldLabel;
	JLabel pfadFieldLabel;
	JLabel mdlDirFieldLabel;

	JTextField appSchemaField = null; 
	JTextField modellartField = null; 
	JTextField profilField = null; 
	JTextField pfadField = null; 
	JTextField mdlDirField = null; 

	private boolean setModellartOnly = false;

	private ButtonGroup quelleGroup;
	private ButtonGroup zielGroup;
	
	//commands
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
	protected String eaRepositoryPath = null;
	
	private Model model = null;
	private String modelMsgText = "";
	private boolean saveModelMsgText = false;
	private JComponent newContentPane;
	
	private boolean blocked = false;
	
	private String paramProfilClass = "de.adv_online.aaa.profiltool.Profil";
	private TitledBorder quelleBorder;
	private TitledBorder zielBorder;
	private JRadioButton rbq3ap;
	private JRadioButton rbqtv;
	private JRadioButton rbqmin;
	private JRadioButton rbqmax;
	private JRadioButton rbz3ap;
	private JRadioButton rbztv;
	private JRadioButton rbzbeide;
	private JRadioButton rbzdel;
	
	public ProfilDialog(){
		super(title);
     	converter = null;
    	result = null;
    	options = null;
    	eaRepositoryPath = null;
    	model = null;
    	blocked = false;
	}
	
	public ProfilDialog(Converter c, Options o, ShapeChangeResult r, String xmi) throws ShapeChangeAbortException{
		super(title);
     	converter = null;
    	result = null;
    	options = null;
    	eaRepositoryPath = null;
    	model = null;
    	blocked = false;
		initialise(c, o, r, xmi);
	}
	
	public void initialise(Converter c, Options o, ShapeChangeResult r, String xmi) throws ShapeChangeAbortException{

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

		File eaRepositoryFile = new File(xmi);
        try{
        	eaRepositoryPath = eaRepositoryFile.getCanonicalFile().getAbsolutePath();
        } catch(IOException e){
        	eaRepositoryPath = "ERROR.qea";
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

        newContentPane.add(createMainTab(), BorderLayout.CENTER);

        statusBar = new StatusBar();

        Box fileBox = Box.createVerticalBox();
        fileBox.add(createStartPanel());
        fileBox.add(statusBar);

        newContentPane.add(fileBox, BorderLayout.SOUTH);

        int height = 610;
        int width = 560;

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

    	String s;
    	
    	String appSchemaStr;
		s = options.parameter("appSchemaName");
		if (s!=null && s.trim().length()>0)
			appSchemaStr = s.trim();
		else
			appSchemaStr = "";

        String mart;
		s = options.parameter(paramProfilClass,"Modellart");
		if (s!=null && s.trim().length()>0)
			mart = s.trim();
		else
			mart = "";
        
        String profil;
		s = options.parameter(paramProfilClass,"Profil");
		if (s!=null && s.trim().length()>0)
			profil = s.trim();
		else
			profil = "";
        
        String quelle;
		s = options.parameter(paramProfilClass,"Quelle");
		if (s!=null && s.trim().length()>0)
			quelle = s.trim();
		else
			quelle = "Neu_Minimal";
        
        String ziel;
		s = options.parameter(paramProfilClass,"Ziel");
		if (s!=null && s.trim().length()>0)
			ziel = s.trim();
		else
			ziel = "Datei";
        
        String pfadStr;
		s = options.parameter(paramProfilClass,"Verzeichnis");
		if (s==null || s.trim().length()==0)
			pfadStr = "";
		else{
			File f = new File(s.trim());
			if(f.exists())
				pfadStr = f.getAbsolutePath();
			else
				pfadStr = "";
		}

        String mdlDirStr = eaRepositoryPath;

		final JPanel topPanel = new JPanel();
        final JPanel topInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 30, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15,20,15,10));

        // Anwendungsschema

        appSchemaField = new JTextField(35);
        appSchemaField.setText(appSchemaStr);
        appSchemaFieldLabel = new JLabel("Name des zu prozessierenden Anwendungsschemas:");

        Box asBox = Box.createVerticalBox();
        asBox.add(appSchemaFieldLabel);
        asBox.add(appSchemaField);
        
        modellartField = new JTextField(10);
        modellartField.setText(mart);
        modellartFieldLabel = new JLabel("Modellart:");

        asBox.add(modellartFieldLabel);
        asBox.add(modellartField);
        
        profilField = new JTextField(10);
        profilField.setText(profil);
        profilFieldLabel = new JLabel("Profilkennung:");
        
        asBox.add(profilFieldLabel);
        asBox.add(profilField);
        
        topInnerPanel.add(asBox);        
        topPanel.add(topInnerPanel);

    	// Quelle
       
        Box quelleBox = Box.createVerticalBox();

        final JPanel quellePanel = new JPanel(new GridLayout(4, 1));
        quelleGroup = new ButtonGroup();
        rbq3ap = new JRadioButton("3ap-Datei");
    	quellePanel.add(rbq3ap);
        if (quelle.equals("Datei"))
        	rbq3ap.setSelected(true);
        rbq3ap.setActionCommand("Datei");
        quelleGroup.add(rbq3ap);
        rbqtv = new JRadioButton("'AAA:Profile' Tagged Values in Modell");
    	quellePanel.add(rbqtv);        	
        if (quelle.equals("Modell"))
        	rbqtv.setSelected(true);
        rbqtv.setActionCommand("Modell");
        quelleGroup.add(rbqtv);
        rbqmin = new JRadioButton("Neues Minimalprofil erzeugen");
    	quellePanel.add(rbqmin);
        if (quelle.equals("Neu_Minimal"))
        	rbqmin.setSelected(true);
        rbqmin.setActionCommand("Neu_Minimal");
        quelleGroup.add(rbqmin);
        rbqmax = new JRadioButton("Neues Maximalprofil erzeugen");
    	quellePanel.add(rbqmax);
        if (quelle.equals("Neu_Maximal"))
        	rbqmax.setSelected(true);
        rbqmax.setActionCommand("Neu_Maximal");
        quelleGroup.add(rbqmax);
        quelleBorder = new TitledBorder(new LineBorder(Color.black), "Quelle der Profildefinition", TitledBorder.LEFT, TitledBorder.TOP);
        quellePanel.setBorder(quelleBorder);        
        
        quelleBox.add(quellePanel);
        
        Box zielBox = Box.createVerticalBox();

        final JPanel zielPanel = new JPanel(new GridLayout(4, 1));
        zielGroup = new ButtonGroup();
        rbz3ap = new JRadioButton("3ap-Datei");
    	zielPanel.add(rbz3ap);
        if (ziel.equals("Datei"))
        	rbz3ap.setSelected(true);
        rbz3ap.setActionCommand("Datei");
        zielGroup.add(rbz3ap);
        rbztv = new JRadioButton("'AAA:Profile' Tagged Values in Modell");
    	zielPanel.add(rbztv);
        if (ziel.equals("Modell"))
        	rbztv.setSelected(true);
        rbztv.setActionCommand("Modell");
        zielGroup.add(rbztv);
        rbzbeide = new JRadioButton("Beides");
    	zielPanel.add(rbzbeide);
        if (ziel.equals("DateiModell"))
        	rbzbeide.setSelected(true);
        rbzbeide.setActionCommand("DateiModell");
        zielGroup.add(rbzbeide);
        rbzdel = new JRadioButton("Profilkennung wird aus Modell entfernt");
    	zielPanel.add(rbzdel);
        if (ziel.equals("Ohne"))
        	rbzdel.setSelected(true);
        rbzdel.setActionCommand("Ohne");
        zielGroup.add(rbzdel);
        zielBorder = new TitledBorder(new LineBorder(Color.black), "Ziel der Profildefinition", TitledBorder.LEFT, TitledBorder.TOP);
        zielPanel.setBorder(zielBorder);        
        
        zielBox.add(zielPanel);
        
        // Pfadangaben

        Box pfadBox = Box.createVerticalBox();
        final JPanel pfadInnerPanel = new JPanel();
        Box skBox = Box.createVerticalBox();

        pfadFieldLabel = new JLabel("Pfad in dem 3ap-Dateien liegen/geschrieben werden:");
        skBox.add(pfadFieldLabel);
        pfadField = new JTextField(40);
        pfadField.setText(pfadStr);
        skBox.add(pfadField);
        
        mdlDirFieldLabel = new JLabel("Pfad zum Modell:");
        skBox.add(mdlDirFieldLabel);
        mdlDirField = new JTextField(40);
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
        fileBox.add(topPanel);
        fileBox.add(quellePanel);
        fileBox.add(zielPanel);
        fileBox.add(pfadPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(fileBox, BorderLayout.NORTH);

        if (profil.isEmpty()) {
        	setModellartOnly = true;
        	disableProfileElements();
        }
        
        // Listen for changes in the profilkennung
        profilField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent e) {
            upd();
          }
          public void removeUpdate(DocumentEvent e) {
            upd();
          }
          public void insertUpdate(DocumentEvent e) {
            upd();
          }

          public void upd() {
             if (!setModellartOnly && profilField.getText().isEmpty()){
            	 setModellartOnly = true;
            	 disableProfileElements();
             } else if (setModellartOnly && !profilField.getText().isEmpty()){
            	 setModellartOnly = false;
            	 enableProfileElements();
             }
          }
        });        
        
        return panel;
    };

    private void disableProfileElements() {
    	rbqmin.setSelected(false);
    	rbqmin.setEnabled(false);
    	rbqmax.setSelected(false);
    	rbqmax.setEnabled(false);
    	rbqtv.setText("'AAA:Modellart' Tagged Valued in Modell");
    	rbztv.setText("'AAA:Modellart' Tagged Valued in Modell");
    	rbzdel.setText("Modellart wird aus Modell entfernt");
    	quelleBorder.setTitle("Quelle der Modellartdefinition");
    	zielBorder.setTitle("Ziel der Modellartdefinition");
    }
    
    private void enableProfileElements() {
    	rbqmin.setEnabled(true);
    	rbqmax.setEnabled(true);
    	rbqtv.setText("'AAA:Profil' Tagged Valued in Modell");
    	rbztv.setText("'AAA:Profil' Tagged Valued in Modell");
    	rbzdel.setText("Profilkennung wird aus Modell entfernt");
    	quelleBorder.setTitle("Quelle der Profildefinition");
    	zielBorder.setTitle("Ziel der Profildefinition");	
    }

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
	    		msg = "Eine Profilerzeugung läuft derzeit.\n";// Meldung
	   		
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
        modelTransformed = false;
        transformationRunning = true;

		options.setParameter("appSchemaName", appSchemaField.getText());
		
		options.setParameter(paramProfilClass, "Modellart", modellartField.getText());
		options.setParameter(paramProfilClass, "Profil", profilField.getText());

		options.setParameter(paramProfilClass, "Quelle", quelleGroup.getSelection().getActionCommand()); 
		options.setParameter(paramProfilClass, "Ziel", zielGroup.getSelection().getActionCommand()); 

		options.setParameter(paramProfilClass, "Verzeichnis", pfadField.getText());
		options.setParameter("inputFile", mdlDirField.getText());
		
		// update target config from dialog also in the target configurations (strictly this is the only place where they need to be updated)
		for (TargetConfiguration cfg : options.getInputTargetConfigs()) {
			if (cfg.getClassName().equalsIgnoreCase(paramProfilClass)) {
				Map<String, String> m = cfg.getParameters();
				m.put("Modellart", modellartField.getText());
				m.put("Profil", profilField.getText());
				m.put("Quelle", quelleGroup.getSelection().getActionCommand()); 
				m.put("Ziel", zielGroup.getSelection().getActionCommand()); 
				m.put("Verzeichnis", pfadField.getText());
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
			ct = new ConvertThread(converter, options, result, eaRepositoryPath, this);
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
		@SuppressWarnings("rawtypes")
		Enumeration e;
		if(transformationRunning){

			appSchemaField.setEnabled(false); 
			modellartField.setEnabled(false); 
			profilField.setEnabled(false); 
			pfadField.setEnabled(false); 
			mdlDirField.setEnabled(false); 
			appSchemaFieldLabel.setEnabled(false);
			modellartFieldLabel.setEnabled(false);
			profilFieldLabel.setEnabled(false);
			pfadFieldLabel.setEnabled(false);
			mdlDirFieldLabel.setEnabled(false);

    		for (e = quelleGroup.getElements() ; e.hasMoreElements() ;) {
    			JRadioButton b = (JRadioButton)e.nextElement();
    			if(b!=null)
    				b.setEnabled(false);
    	     }
    		for (e = zielGroup.getElements() ; e.hasMoreElements() ;) {
    			JRadioButton b = (JRadioButton)e.nextElement();
    			if(b!=null)
    				b.setEnabled(false);
    	     }
			
			startButton.setEnabled(false);
			viewLogButton.setEnabled(false);
	        exitButton.setEnabled(true);

	        setConfigDirSettings(false);
	        
    	}
    	else{    		
			appSchemaField.setEnabled(true); 
			modellartField.setEnabled(true); 
			profilField.setEnabled(true); 
			pfadField.setEnabled(true); 
			mdlDirField.setEnabled(true);
			appSchemaFieldLabel.setEnabled(true);
			modellartFieldLabel.setEnabled(true);
			profilFieldLabel.setEnabled(true);
			pfadFieldLabel.setEnabled(true);
			mdlDirFieldLabel.setEnabled(true);
			
    		for (e = quelleGroup.getElements() ; e.hasMoreElements() ;) {
    			JRadioButton b = (JRadioButton)e.nextElement();
    			if(b!=null)
    				b.setEnabled(true);
    	     }
    		for (e = zielGroup.getElements() ; e.hasMoreElements() ;) {
    			JRadioButton b = (JRadioButton)e.nextElement();
    			if(b!=null)
    				b.setEnabled(true);
    	     }

    		if(startButton!=null){
				boolean setStart = true;
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
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
	   	} else if (e.getSource() == exitButton) {
	   		closeDialog();
	   	}
    }

	public void itemStateChanged(ItemEvent e) {
	}
	
	public void setConfigDirSettings(boolean allSelConfig){
	}
	
	public void statusChanged(int status){
		String msg = " ";
		switch(status){
		// Dialog
		case STATUS_READY:
			msg += "Bereit";
			break;
		// Model EADocument
		case EADocument.STATUS_EADOCUMENT_INITSTART:
			msg += "Lesen des Modells: Initialisierung";
			break;
		case EADocument.STATUS_EADOCUMENT_READMODEL:
			msg += "Lesen des Modells: Paketinformationen";
			break;
		case EADocument.STATUS_EADOCUMENT_ESTABLISHCLASSES:
			msg += "Lesen des Modells: Objektarten und deren Eigenschaften";
			break;
		case EADocument.STATUS_EADOCUMENT_READCONSTARINTS:
			msg += "Lesen des Modells: Konsistenzbedingungen";
			break;

		// Targets
		case Converter.STATUS_TARGET_INITSTART:
			msg += "AAA-Profiltool: Initialisierung der Profilverarbeitung";
			break;
			
		case Converter.STATUS_TARGET_PROCESS:
			msg += "AAA-Profiltool: Lesen/Erzeugen des Profils";
			break;
			
		case Converter.STATUS_TARGET_WRITE:
			msg += "AAA-Profiltool: Schreiben des Profils";
			break;

		case Profil.STATUS_WRITE_3AP:
			msg += "AAA-Profiltool: Schreiben des Profils als 3ap-Datei";
			break;
		
		case Profil.STATUS_WRITE_MODEL:
			msg += "AAA-Profiltool: Schreiben des Profils ins Modell";
			break;
		
		case Profil.STATUS_CLEAN_MODEL:
			msg += "AAA-Profiltool: Entfernen des Profils aus dem Modell";
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
