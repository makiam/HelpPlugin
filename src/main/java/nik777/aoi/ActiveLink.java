/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nik777.aoi;

import artofillusion.script.ScriptRunner;
import com.sun.java.help.impl.ViewAwareComponent;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.text.View;


/**
 *
 * @author MaksK
 */

public class ActiveLink extends JButton implements ViewAwareComponent {

    private static final Logger logger = Logger.getLogger(ActiveLink.class.getName());
    
    public ActiveLink() {
        initComponent();
    }

    private void initComponent() {
        setMargin(new Insets(0,0,0,0));
        setForeground(java.awt.Color.BLUE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {                
                String lang = ScriptRunner.getLanguageForFilename(ActiveLink.this.script);                
                ScriptRunner.executeScript(lang, loadScript(), Map.of("link", this, "event", event, "arg", arg));
            }
            
        }); 
    }
    
    private String loadScript() {
        if (this.script.charAt(0) != '@') return script;
        
        try(InputStream in = getClass().getClassLoader().getResourceAsStream(this.script.substring(1))) {
            return new String(in.readAllBytes());
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error reading script", ioe);
        }
        return "";
    }
    
    private String script = "";

    public void setScript(String script) {;
        this.script = script;
    }
    
    private String arg = "";

    public void setArg(String arg) {
        this.arg = arg;
    }


    @Override
    public void setText(String text) {
        super.setText(text);
    }

    @Override
    public void setViewData(View v) {
        
    }

}
