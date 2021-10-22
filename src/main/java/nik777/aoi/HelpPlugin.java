/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nik777.aoi;

import artofillusion.DefaultPluginImplementation;
import artofillusion.LayoutWindow;
import artofillusion.PluginRegistry;

import artofillusion.PluginRegistry.PluginResource;
import artofillusion.SplashScreen;
import artofillusion.keystroke.KeystrokeManager;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import buoy.event.CommandEvent;
import buoy.widget.BFrame;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import buoy.widget.BTabbedPane;
import buoy.widget.Widget;
import buoyx.docking.DockableWidget;
import buoyx.docking.DockingContainer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.CSH;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.event.HelpSetEvent;
import javax.help.event.HelpSetListener;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 *
 * @author MaksK
 */

public class HelpPlugin extends DefaultPluginImplementation implements HelpSetListener {
    
    private static final Logger logger = Logger.getLogger(HelpPlugin.class.getName());
    
    private final ActionEvent cshAction = new ActionEvent(new JLabel(), ActionEvent.ACTION_FIRST, null);
    private BFrame currentView;
    
    private Optional<HelpBroker> broker = Optional.empty();

    private final Map<String, HelpSet> mapIdToHelpSet = new HashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean();
    
    @Override
    protected void onApplicationStarting() {
        String script = "PluginRegistry.invokeExportedMethod(\"artofillusion.HelpPlugin.huh\", null);";        
        KeystrokeManager.addRecord(new KeystrokeRecord(KeyEvent.VK_F3, 0, "Huh Focus (HelpPlugin)", script));
        script = "PluginRegistry.invokeExportedMethod(\"artofillusion.HelpPlugin.show\", null);";
        KeystrokeManager.addRecord(new KeystrokeRecord(KeyEvent.VK_F2, 0, "Help (HelpPlugin)", script));
        script = "PluginRegistry.invokeExportedMethod(\"artofillusion.HelpPlugin.what\", null);";
        KeystrokeManager.addRecord(new KeystrokeRecord(KeyEvent.VK_F1, 0, "What's This? (HelpPlugin)", script));
    }
    
    @Override
    protected void onSceneWindowCreated(LayoutWindow view) {
        logger.info(() -> "Locale: " + Translate.getLocale());
        Locale.setDefault(Translate.getLocale());
        
        if(!initialized.get()) {
            initialized.set(true);
            try {
                PluginResource rh = PluginRegistry.getResource("help", "AOIHelp");
                HelpSet hs = new HelpSet(null, HelpSet.findHelpSet(rh.getClassLoader(), rh.getName()));
                hs.addHelpSetListener(this);
                mapIdToHelpSet.put("AOIHelp", hs);
                broker = Optional.of(hs.createHelpBroker());
            } catch (HelpSetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            ClassLoader cl = this.getClass().getClassLoader();
            broker.ifPresent(hb -> {
                for(PluginResource resource: artofillusion.PluginRegistry.getResources("help")) {
                    if(resource.getId().equals("AOIHelp")) continue;
                    try {
                        HelpSet add = new HelpSet(cl, HelpSet.findHelpSet(resource.getClassLoader(), resource.getName()));
                        mapIdToHelpSet.put(resource.getId(), add);
                        hb.getHelpSet().add(add);
                    } catch (HelpSetException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
   
        logger.info("Installing help menu...");
        String hm = Translate.text("HelpPlugin:menu.Help");      
        
        BMenu helpMenu = view.getMenuBar().getChildren().stream().
                map((Widget t) -> { return (BMenu)t; }).
                filter(menu -> menu.getText().equals(hm)).findFirst().or(() -> {
            BMenu menu = Translate.menu("HelpPlugin:Help");
            view.getMenuBar().add(menu);
            return Optional.of(menu);
        }).get();
        

        BMenuItem help = new BMenuItem();
        help.addEventLink(CommandEvent.class, this, "onCommand");
        help.setEnabled(false);
        broker.ifPresent(hb -> {
            help.setEnabled(true);
            help.getComponent().setAction(actionShowHelp);
        });
        help.setText(Translate.text("HelpPlugin:menu.AOIHelp"));
        
        BMenuItem whatThis = new BMenuItem(Translate.text("HelpPlugin:menu.WhatsThis"));
        whatThis.setEnabled(false);
        broker.ifPresent(hb -> {
            whatThis.setEnabled(true);
            whatThis.getComponent().addActionListener(new CSH.DisplayHelpAfterTracking(hb));            
        });
        
        
        BMenuItem ref = new BMenuItem("Command Reference");
        ref.addEventLink(CommandEvent.class, this, "onCommand");
        ref.setEnabled(false);
        CSH.setHelpIDString(ref.getComponent(), "CmdReference.CmdReference");
        broker.ifPresent(hb -> {
            ref.setEnabled(true);
            ref.getComponent().addActionListener(new CSH.DisplayHelpFromSource(hb));
        });
        
        helpMenu.add(help);
        helpMenu.add(whatThis);
        helpMenu.add(ref);
        helpMenu.addSeparator();
        
        BMenuItem about = new BMenuItem();
        JMenuItem mc = about.getComponent();
        mc.putClientProperty("view", view.getComponent());
        mc.setAction(aboutAction);
        mc.setText(Translate.text("HelpPlugin:menu.About"));
        helpMenu.add(about);
        
        
        broker.ifPresent(hb -> {
            final HelpSet hs = hb.getHelpSet();
            for (int i = 0; i < 4; i++) {
                DockingContainer dock;
                switch (i) {
                    case 0:
                        dock = view.getDockingContainer(BTabbedPane.TOP);
                        break;
                    case 1:
                        dock = view.getDockingContainer(BTabbedPane.BOTTOM);
                        break;
                    case 2:
                        dock = view.getDockingContainer(BTabbedPane.LEFT);
                        break;
                    case 3:
                        dock = view.getDockingContainer(BTabbedPane.RIGHT);
                        break;
                    default:
                        return;
                }

                dock.getChildren().stream().filter(widget -> widget instanceof DockableWidget).map((Widget ww) -> (DockableWidget)ww).forEach(dockable -> {
                    Component cc = dockable.getComponent();
                    if(CSH.getHelpIDString(cc) == null) {
                        CSH.setHelpIDString(cc, "Docking." + dockable.getLabel());
                        CSH.setHelpSet(cc, hs);
                    }
                });
            }

            CSH.setHelpIDString(view.getComponent(), "LayoutWindow");
            CSH.setHelpSet(view.getComponent(), hs);
        });
        

        
    }

    public void onCommand(CommandEvent event) {
        logger.fine(() -> "On Command triggered...");
        currentView = UIUtilities.findFrame(event.getWidget());
    }
    
    /*
    * Plugin exported method!
    */
    public BFrame getContext() {
        logger.info(() -> "get context...");
        return currentView;
    }
    
    /*
    * Plugin exported method!
    */    
    public void what() {
        logger.info(() -> "call what...");
        broker.ifPresent(hb -> {
            new CSH.DisplayHelpAfterTracking(hb).actionPerformed(cshAction);
        });
    }
    
    /*
    * Plugin exported method!
    */
    public void show() {
        logger.info(() -> "call show...");
        broker.ifPresent(hb -> {            
            new CSH.DisplayHelpFromSource(hb).actionPerformed(cshAction);
        });
    }
    
    /*
    * Plugin exported method!
    */
    public void huh() {
        logger.info(() -> "call huh...");
        broker.ifPresent(hb -> {
            new CSH.DisplayHelpFromFocus(hb).actionPerformed(cshAction);
        });
    }
    

    /*
    * Plugin exported method!
    */
    public static void unregister(Widget target) {
        logger.info(() -> "call unregister...");
        unregister(target.getComponent());
    }

    /*
    * Plugin exported method!
    */
    public static void unregister(Component target) {
        CSH.setHelpIDString(target, null);
        CSH.setHelpSet(target, null);
    }    

    /*
    * Plugin exported method!
    */
    public void register(Widget target, String resourceId, String id) {
        register(target.getComponent(), resourceId, id);
    }
    
    /*
    * Plugin exported method!
    */    
    public void register(Component target, String resourceId, String helpId) {        
        CSH.setHelpIDString(target, helpId);
        CSH.setHelpSet(target, mapIdToHelpSet.getOrDefault(resourceId, null));
    }
    
    @Override
    protected void onSceneClosing(LayoutWindow view) {
        CSH.setHelpIDString(view.getComponent(), null);
        CSH.setHelpSet(view.getComponent(), null);

    }
    
    private final AbstractAction aboutAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent event) {
            final JFrame owner = (JFrame)((JMenuItem)event.getSource()).getClientProperty("view");
            SwingUtilities.invokeLater(() -> {
                new SplashScreen(owner).setVisible(true);
            });
        }
    };
    
    private final AbstractAction actionShowHelp = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            show();
        }
        
    };
    
    @Override
    public void helpSetAdded(HelpSetEvent event) {
        logger.info(() -> "Added: " + event.getHelpSet().getHelpSetURL() + "->" + event.getHelpSet().getLocale());
    }

    @Override
    public void helpSetRemoved(HelpSetEvent event) {
        logger.info(() -> "Removed: " + event.getHelpSet().getHelpSetURL() + "->" + event.getHelpSet().getLocale());
        
    }
}
