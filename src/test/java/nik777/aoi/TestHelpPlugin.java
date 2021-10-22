package nik777.aoi;


import artofillusion.PluginRegistry;
import buoy.widget.BMenu;
import buoy.widget.Widget;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JLabel;
import org.junit.Test;



public class TestHelpPlugin {
    
    @Test
    public void test() throws NoSuchMethodException, InvocationTargetException {
        HelpPlugin plugin = new HelpPlugin();
                
        PluginRegistry.registerPlugin(plugin);
        PluginRegistry.registerExportedMethod(plugin, "getContext", "artofillusion.HelpPlugin.getContext");
        PluginRegistry.registerExportedMethod(plugin, "what", "artofillusion.HelpPlugin.what");
        
        //Object context = PluginRegistry.invokeExportedMethod("artofillusion.HelpPlugin.getContext", new Object[0]);
        
        PluginRegistry.invokeExportedMethod("artofillusion.HelpPlugin.what");
         
         PluginRegistry.registerExportedMethod(plugin, "unregister", "artofillusion.HelpPlugin.unregister");
         
         Component cc = new JLabel();
         Widget ww = new BMenu();
         PluginRegistry.invokeExportedMethod("artofillusion.HelpPlugin.unregister", cc);
         PluginRegistry.invokeExportedMethod("artofillusion.HelpPlugin.unregister", ww);
         
    }
}
